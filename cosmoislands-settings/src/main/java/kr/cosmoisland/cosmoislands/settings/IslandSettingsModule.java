package kr.cosmoisland.cosmoislands.settings;

import com.google.common.cache.*;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettings;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.core.Database;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class IslandSettingsModule implements IslandModule<IslandSettingsMap> {

    private final RedisAsyncCommands<String, String> async;
    private final IslandSettingsDataModel model;
    @Getter
    private final Logger logger;
    private final LoadingCache<Integer, IslandSettingsMap> proxiedCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<Integer, IslandSettingsMap>() {
                @Override
                public IslandSettingsMap load(Integer integer) throws Exception {
                    MySQLIslandSettingsMap mysql = new MySQLIslandSettingsMap(integer, model);
                    RedisIslandSettingsMap redis = new RedisIslandSettingsMap(integer, async);
                    return new CosmoIslandSettingsMap(mysql, redis);
                }
            });
    private final LoadingCache<Integer, CompletableFuture<IslandSettingsMap>> localCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Integer, CompletableFuture<IslandSettingsMap>>() {
                @Override
                public CompletableFuture<IslandSettingsMap> load(Integer integer) throws Exception {
                    MySQLIslandSettingsMap mysql = new MySQLIslandSettingsMap(integer, model);
                    RedisIslandSettingsMap redis = new RedisIslandSettingsMap(integer, async);
                    CosmoIslandSettingsMap cosmo = new CosmoIslandSettingsMap(mysql, redis);
                    return cosmo.migrate().thenApply(ignored->cosmo);
                }
            });

    public IslandSettingsModule(Database database, RedisAsyncCommands<String, String> async, Map<IslandSettings, String> map, Logger logger){
        this.async = async;
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
