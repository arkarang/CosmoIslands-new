package kr.cosmoislands.cosmoislands.world;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.minepalm.manyworlds.api.ManyWorld;
import com.minepalm.manyworlds.api.WorldService;
import com.minepalm.manyworlds.api.entity.WorldInform;
import com.minepalm.manyworlds.bukkit.mysql.MySQLWorldDatabase;
import com.minepalm.manyworlds.bukkit.swm.SWMWorldFactory;
import com.minepalm.manyworlds.core.WorldToken;
import com.minepalm.manyworlds.core.database.MySQLDatabase;
import kr.cosmoislands.cosmoislands.api.IslandModule;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.world.IslandWorld;
import kr.cosmoislands.cosmoislands.core.Database;
import kr.cosmoislands.cosmoislands.core.thread.IslandThreadFactory;
import lombok.Getter;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class IslandWorldModule implements IslandModule<IslandWorld> {

    @Getter
    private final Logger logger;
    private final WorldService worldService;
    private final MySQLIslandWorldDataModel model;
    private final ImmutableMap<String, Integer> defaultValues;
    private final ConcurrentHashMap<Integer, CompletableFuture<IslandWorld>> local = new ConcurrentHashMap<>();
    private final LoadingCache<Integer, IslandManyWorld> proxiedCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<Integer, IslandManyWorld>() {
                @Override
                public IslandManyWorld load(Integer integer) throws Exception {
                    return create(integer, worldService.get(new WorldInform(WorldToken.get("ISLAND"), "island_"+integer)));
                }
            });

    public IslandWorldModule(WorldService worldService,
                             Database islandDatabase,
                             Properties properties,
                             Map<String, Integer> defaultValues,
                             ThreadFactory threadFactory,
                             Logger logger){
        this.worldService = worldService;
        this.logger = logger;
        this.defaultValues = ImmutableMap.copyOf(defaultValues);
        this.model = new MySQLIslandWorldDataModel("cosmoislands_world_data", "cosmoislands_islands", islandDatabase, this.defaultValues);
        MySQLDatabase manyWorldDatabase = new MySQLDatabase(properties, Executors.newFixedThreadPool(4, threadFactory));
        this.worldService.getLoadService().registerWorldFactory(WorldToken.get("ISLAND"), new SWMWorldFactory(this.worldService.getWorldRegistry()));
        this.worldService.getWorldRegistry().registerDatabase(new MySQLWorldDatabase(WorldToken.get("ISLAND"), "cosmoislands_worlds", manyWorldDatabase, logger));
    }

    protected IslandManyWorld create(int islandId, ManyWorld world){
        return new IslandManyWorld(islandId, world, model, defaultValues);
    }

    private WorldInform toInform(int islandId){
        return new WorldInform(WorldToken.get("ISLAND"), "island_"+islandId);
    }

    @Override
    public CompletableFuture<IslandWorld> getAsync(int islandId) {
        if(local.containsKey(islandId))
            return local.get(islandId);
        else {
            try {
                return CompletableFuture.completedFuture(proxiedCache.get(islandId));
            }catch (ExecutionException e){
                return null;
            }
        }
    }

    @Override
    public IslandWorld get(int islandId) {
        try {
            if(local.containsKey(islandId))
                return local.get(islandId).get();
            else
                return proxiedCache.get(islandId);
        }catch (InterruptedException | ExecutionException e){
            return null;
        }
    }

    @Override
    public void invalidate(int islandId) {
        unregister(islandId);
        proxiedCache.invalidate(islandId);
    }

    @Override
    @Deprecated
    public CompletableFuture<Void> create(int islandId, UUID uuid) {
        throw new UnsupportedOperationException("ChuYong will implement this");
    }

    void register(int islandId, CompletableFuture<IslandWorld> future){
        local.put(islandId, future);
    }

    void unregister(int islandId){
        local.remove(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        this.model.init();
        service.getFactory().addFirst("world", new IslandWorldLifecycle(this, this.worldService));
    }

    @Override
    public void onDisable(IslandService service) {
        local.clear();
        proxiedCache.invalidateAll();
    }

}
