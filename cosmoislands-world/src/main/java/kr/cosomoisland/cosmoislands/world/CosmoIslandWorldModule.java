package kr.cosomoisland.cosmoislands.world;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.manyworlds.api.ManyWorld;
import com.minepalm.manyworlds.api.WorldService;
import com.minepalm.manyworlds.api.entity.WorldInform;
import com.minepalm.manyworlds.bukkit.mysql.MySQLWorldDatabase;
import com.minepalm.manyworlds.bukkit.swm.SWMWorldFactory;
import com.minepalm.manyworlds.core.WorldToken;
import com.minepalm.manyworlds.core.database.MySQLDatabase;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.world.IslandWorld;
import kr.cosmoisland.cosmoislands.core.Database;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CosmoIslandWorldModule implements IslandModule<IslandWorld> {

    @Getter
    private final Logger logger;
    private final WorldService worldService;
    private final MySQLIslandWorldDataModel model;
    private final ImmutableMap<String, Integer> defaultValues;
    private final ConcurrentHashMap<Integer, CompletableFuture<IslandWorld>> map = new ConcurrentHashMap<>();
    private final LoadingCache<Integer, IslandManyWorld> proxiedCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<Integer, IslandManyWorld>() {
                @Override
                public IslandManyWorld load(Integer integer) throws Exception {
                    return create(integer, worldService.get(new WorldInform(WorldToken.get("ISLAND"), "island_"+integer)));
                }
            });

    public CosmoIslandWorldModule(WorldService worldService,
                                  Database islandDatabase,
                                  Properties properties,
                                  Map<String, Integer> defaultValues,
                                  BukkitExecutor executor,
                                  Logger logger){
        this.worldService = worldService;
        this.logger = logger;
        this.defaultValues = ImmutableMap.copyOf(defaultValues);
        this.model = new MySQLIslandWorldDataModel("cosmoislands_world_data", "cosmoislands_islands", islandDatabase, this.defaultValues);
        MySQLDatabase manyWorldDatabase = new MySQLDatabase(properties, Executors.newFixedThreadPool(4));
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
        if(map.containsKey(islandId))
            return map.get(islandId);
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
            if(map.containsKey(islandId))
                return map.get(islandId).get();
            else
                return proxiedCache.get(islandId);
        }catch (InterruptedException | ExecutionException e){
            return null;
        }
    }

    void register(int islandId, CompletableFuture<IslandWorld> future){
        map.put(islandId, future);
    }

    void unregister(int islandId){
        map.remove(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        this.model.init();
        service.getFactory().addFirst("players", new IslandWorldLifecycle(this, this.worldService));
    }

    @Override
    public void onDisable(IslandService service) {
        map.clear();
        proxiedCache.invalidateAll();
    }

}
