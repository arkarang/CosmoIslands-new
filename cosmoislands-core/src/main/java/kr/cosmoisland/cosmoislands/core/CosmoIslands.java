package kr.cosmoisland.cosmoislands.core;

import com.google.common.collect.ImmutableMap;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.helloplayer.core.HelloPlayers;
import com.minepalm.manyworlds.bukkit.ManyWorlds;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.*;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.core.thread.IslandThreadFactory;
import kr.cosmoisland.cosmoislands.players.MySQLIslandPlayerDatabase;
import kr.cosmoisland.cosmoislands.players.RedisIslandPlayerRegistry;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CosmoIslands implements IslandService {

    private final HelloEveryone networkModule;
    private final HelloPlayers playerModule;
    private final CosmoChat cosmoChat;
    private final RedisClient redis;

    @Getter
    private final Database database;
    @Getter
    private final ExternalRepository externalRepository;
    @Getter
    private final IslandFactory factory;
    @Getter
    private final IslandRegistry registry;
    @Getter
    private final IslandPlayerRegistry playerRegistry;
    @Getter
    private final IslandGarbageCollector garbageCollector;
    @Getter
    private final IslandPacemaker pacemaker;
    @Getter
    private final IslandCloud cloud;
    @Getter
    private final ThreadFactory threadFactory;
    private final ExecutorService executors;

    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private final ConcurrentHashMap<Class<? extends IslandComponent>, IslandModule<? extends IslandComponent>> modules;

    public CosmoIslands( HelloEveryone network,
                  RedisClient client,
                  HelloPlayers players,
                  CosmoChat chatService,
                  Database database,
                  MySQLDatabase msMySQLDatabase ){
        StatefulRedisConnection<String, String> connection = client.connect();
        RedisAsyncCommands<String, String> async = connection.async();
        this.redis = client;
        this.externalRepository = new CosmoExternalRepository();
        this.threadFactory = new IslandThreadFactory("cosmoislands");
        this.networkModule = network;
        this.playerModule = players;
        this.cosmoChat = chatService;
        this.database = database;
        this.factory = new CosmoIslandFactory(Executors.newScheduledThreadPool(4, this.threadFactory), database);
        this.cloud = new RedisIslandCloud(networkModule, this, async);
        this.registry = new CosmoIslandRegistry(100, this.cloud.getLocalServer());
        this.playerRegistry = RedisIslandPlayerRegistry
                .build("cosmoislands_players", "cosmoislands_islands", msMySQLDatabase, async, registry);
        this.garbageCollector = new CosmoIslandGarbageCollector(this, 1000*30*60L);
        this.executors = Executors.newScheduledThreadPool(4, this.threadFactory);
        this.modules = new ConcurrentHashMap<>();
        this.pacemaker = new CosmoIslandPacemaker(this.registry, this.garbageCollector, this.threadFactory, 1000L);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> IslandModule<T> getModule(Class<T> clazz) {
        return (IslandModule<T>) modules.get(clazz);
    }

    @Override
    public <T extends IslandComponent> void registerModule(Class<T> clazz, IslandModule<T> module) {
        if(modules.containsKey(clazz)){
            throw new IllegalArgumentException("already exists module.");
        }else{
            modules.put(clazz, module);
        }
    }

    @Override
    public synchronized void init() {
        if(!isInitialized.get()){
            Map<ModulePriority, List<IslandModule<?>>> map = new HashMap<>();
            for (ModulePriority value : ModulePriority.values()) {
                map.put(value, new ArrayList<>());
            }
            for (IslandModule<? extends IslandComponent> value : modules.values()) {
                try {
                    value.onEnable(this);
                }catch (Throwable e){
                    System.err.println("an exception occurred loading module: "+value.getClass().getSimpleName());
                    e.printStackTrace();
                }
            }
            isInitialized.set(true);
        }
    }

    @Override
    public void shutdown() throws ExecutionException, InterruptedException {
        pacemaker.shutdown();
        ImmutableMap<Integer, Island> islands = registry.getLocals();
        List<CompletableFuture<?>> futures = new ArrayList<>(islands.size());
        for (int id : islands.keySet()) {
            futures.add(unloadIsland(id));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        modules.values().forEach(module-> module.onDisable(this));
    }

    @Override
    public CompletableFuture<Island> loadIsland(int id) {
        return factory.fireLoad(id)
                .thenApply(context-> new CosmoIsland(context, playerRegistry, cloud))
                .thenApply(island->{
                    this.registry.registerIsland(island);
                    return island;
                });
    }

    @Override
    public CompletableFuture<Boolean> unloadIsland(int id) {
        Island island = registry.getLocalIsland(id);
        if(island != null){
            return factory.fireUnload(island).thenApply(context-> {
                if(context != null){
                    registry.unregisterIsland(id);
                    return true;
                }else
                    return false;
        });
        }else
            return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Island> createIsland(UUID uuid) {
        return factory.fireCreate(uuid)
                .thenApply(context-> new CosmoIsland(context, playerRegistry, cloud))
                .thenApply(island -> {
                    this.registry.registerIsland(island);
                    return island;
                });
    }

    @Override
    public CompletableFuture<Boolean> deleteIsland(int id) {
        Island island = this.registry.getIsland(id);
        if(island != null){
            return factory.fireDelete(island).thenApply(ignored->true);
        }else
            return CompletableFuture.completedFuture(false);
    }
}
