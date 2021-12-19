package kr.comsoislands.comsoislands.member;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.ModificationStrategyRegistry;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.settings.IslandSettingsModule;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class PlayersMapRegistry {

    final IslandRegistry islandRegistry;
    final IslandPlayerRegistry registry;
    final PlayersMapDataModel model;
    final IslandSettingsModule settingsModule;
    final RedisAsyncCommands<String, String> async;
    final ModificationStrategyRegistry strategies;

    LoadingCache<Integer, CompletableFuture<IslandPlayersMap>> cache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, CompletableFuture<IslandPlayersMap>>() {
        @Override
        public CompletableFuture<IslandPlayersMap> load(@Nonnull Integer integer) throws Exception {
            CompletableFuture<IslandSettingsMap> future = settingsModule.getAsync(integer);
            MySQLPlayersMap mysql = new MySQLPlayersMap(integer, registry, model);
            RedisPlayersMap redis = new RedisPlayersMap(integer, registry, async);
            return redis.migrate(mysql).thenCombine(future, (ignored, settingsMap)-> {
                return new CosmoIslandPlayersMap(islandRegistry.getIsland(integer), mysql, redis, settingsMap, strategies.getStrategies());
            });
        }
    });

    IslandPlayersMap get(int islandId){
        try {
            return cache.get(islandId).get();
        }catch (InterruptedException | ExecutionException e){
            return null;
        }
    }

    CompletableFuture<IslandPlayersMap> getAsync(int islandId){
        return cache.getIfPresent(islandId);
    }

    void load(int islandId){
        cache.refresh(islandId);
    }

    void invalidate(int islandId){
        CompletableFuture<IslandPlayersMap> future = cache.getIfPresent(islandId);
        if(future != null)
            future.thenApply(IslandComponent::invalidate);
    }

}
