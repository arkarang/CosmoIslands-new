package kr.cosmoisland.cosmoislands.core;

import com.google.common.collect.ImmutableMap;
import com.minepalm.hellobungee.api.HelloEveryone;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.*;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.core.thread.IslandThreadFactory;
import kr.cosmoisland.cosmoislands.players.RedisIslandPlayerRegistry;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class CosmoIslands implements IslandService {

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

    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private final ConcurrentHashMap<Class<? extends IslandComponent>, IslandModule<? extends IslandComponent>> modules;

    public CosmoIslands( HelloEveryone network,
                         RedisClient client,
                         MySQLDatabase msMySQLDatabase,
                         Logger logger) throws ExecutionException, InterruptedException {
        StatefulRedisConnection<String, String> connection = client.connect();
        RedisAsyncCommands<String, String> async = connection.async();
        this.externalRepository = new CosmoExternalRepository();
        this.threadFactory = new IslandThreadFactory("cosmoislands");
        this.database = new Database(msMySQLDatabase, "cosmoislands_islands");
        IslandRegistrationDataModel registrationDataModel = new IslandRegistrationDataModel("cosmoislands_islands", database);
        this.factory = new CosmoIslandFactory(Executors.newScheduledThreadPool(4, this.threadFactory), registrationDataModel);
        this.cloud = new CosmoIslandCloud(network, this, database, async, logger);
        this.registry = new CosmoIslandRegistry(100, this, this.cloud);
        this.playerRegistry = RedisIslandPlayerRegistry
                .build("cosmoislands_players", "cosmoislands_islands", msMySQLDatabase, async, registry);
        this.garbageCollector = new CosmoIslandGarbageCollector(this, 1000*30*60L);
        this.modules = new ConcurrentHashMap<>();
        this.pacemaker = new CosmoIslandPacemaker(this.registry, this.garbageCollector, this.threadFactory, 1000L);
        OperationPrecondition.init(playerRegistry, cloud);
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
            //todo: ModulePriority 구현
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
            this.getCloud().getHostServer().registerServer();
            isInitialized.set(true);
        }
    }

    @Override
    public void shutdown() throws ExecutionException, InterruptedException {
        pacemaker.shutdown();
        this.cloud.getHostServer().shutdown();
        ImmutableMap<Integer, Island> islands = registry.getLocals();
        List<CompletableFuture<?>> futures = new ArrayList<>(islands.size());
        for (int id : islands.keySet()) {
            futures.add(unloadIsland(id));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        modules.values().forEach(module-> module.onDisable(this));
    }

    @Override
    public CompletableFuture<Island> loadIsland(int id, boolean isLocal) {
        return OperationPrecondition.canUpdate(id, true).thenCompose(canExecute->{
            if (canExecute) {
                return factory.fireLoad(id, isLocal)
                        .thenApply(context-> new CosmoIsland(context, cloud))
                        .thenApply(island->{
                            this.registry.registerIsland(island);
                            return island;
                        });
            }else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> unloadIsland(int id) {
        return OperationPrecondition.canUpdate(id, false).thenCompose(canExecute->{
            if(canExecute) {
                Island island = registry.getLocalIsland(id);
                if (island != null) {
                    return factory.fireUnload(island).thenApply(context -> {
                        if (context != null) {
                            registry.unregisterIsland(id);
                            return true;
                        } else
                            return false;
                    });
                }
            }
            return CompletableFuture.completedFuture(false);
        });
    }

    @Override
    public CompletableFuture<Island> createIsland(UUID uuid) {
        return OperationPrecondition.canCreate(uuid).thenCompose(canExecute->{
            if(canExecute){
                return factory.fireCreate(uuid)
                        .thenApply(context-> new CosmoIsland(context, cloud))
                        .thenApply(island -> {
                            this.registry.registerIsland(island);
                            return island;
                        });
            }else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteIsland(int id) {
        return OperationPrecondition.canDelete(id).thenCompose(canExecute->{
            if(canExecute){
                Island island = this.registry.getLocalIsland(id);
                if(island != null){
                    return factory.fireDelete(island).thenApply(ignored->true);
                }
            }
            return CompletableFuture.completedFuture(false);
        });
    }
}
