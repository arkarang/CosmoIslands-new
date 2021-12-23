package kr.cosmoislands.cosmoislands.settings;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.IslandCloud;
import kr.cosmoislands.cosmoislands.api.IslandModule;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class IslandSettingsModule implements IslandModule<IslandSettingsMap> {

    private final ImmutableMap<IslandSetting, String> defaultValues;
    private final RedisAsyncCommands<String, String> async;
    private final IslandSettingsDataModel model;
    private final IslandCloud cloud;
    @Getter
    private final Logger logger;
    private final LoadingCache<Integer, IslandSettingsMap> proxiedCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<Integer, IslandSettingsMap>() {
                @Override
                public IslandSettingsMap load(Integer integer) throws Exception {
                    MySQLIslandSettingsMap mysql = new MySQLIslandSettingsMap(integer, model);
                    RedisIslandSettingsMap redis = new RedisIslandSettingsMap(integer, async);
                    return new IslandSettingsMapRemote(integer, mysql, redis, cloud);
                }
            });
    private final LoadingCache<Integer, CompletableFuture<IslandSettingsMap>> localCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Integer, CompletableFuture<IslandSettingsMap>>() {
                @Override
                public CompletableFuture<IslandSettingsMap> load(Integer integer) throws Exception {
                    MySQLIslandSettingsMap mysql = new MySQLIslandSettingsMap(integer, model);
                    RedisIslandSettingsMap redis = new RedisIslandSettingsMap(integer, async);
                    CosmoIslandSettingsMap cached = new CachedIslandSettingsMap(mysql, redis, defaultValues);
                    return cached.migrate().thenApply(ignored->cached);
                }
            });

    public IslandSettingsModule(Database database,
                                IslandCloud cloud,
                                RedisAsyncCommands<String, String> async,
                                Map<IslandSetting, String> map,
                                Logger logger){
        this.async = async;
        this.cloud = cloud;
        this.defaultValues = ImmutableMap.copyOf(map);
        this.model = new IslandSettingsDataModel("cosmoislands_settings", "cosmoislands_islands", database, map);
        this.logger = logger;
    }

    @Override
    public CompletableFuture<IslandSettingsMap> getAsync(int islandId) {
        try {
            CompletableFuture<IslandSettingsMap> future = localCache.getUnchecked(islandId);
            if (future == null) {
                return CompletableFuture.completedFuture(proxiedCache.get(islandId));
            } else
                return future;
        }catch (ExecutionException e){
            return null;
        }
    }

    @Override
    public IslandSettingsMap get(int islandId) {
        try {
            CompletableFuture<IslandSettingsMap> future = localCache.getUnchecked(islandId);
            if (future == null) {
                return proxiedCache.get(islandId);
            } else
                return future.get();
        }catch (InterruptedException | ExecutionException e){
            return null;
        }
    }

    @Override
    public void invalidate(int islandId) {
        this.proxiedCache.invalidate(islandId);
        this.localCache.invalidate(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        this.model.init();
        service.getFactory().addLast("settings", new IslandSettingsLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {
        this.localCache.invalidateAll();
        this.proxiedCache.invalidateAll();
    }

}
