package kr.cosmoislands.cosmoislands.settings;

import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class RedisIslandSettingsMap implements IslandSettingsMap {

    private final int islandId;
    private final String redisKey;
    private final RedisAsyncCommands<String, String> async;

    RedisIslandSettingsMap(int islandId, RedisAsyncCommands<String, String> async){
        this.islandId = islandId;
        this.async = async;
        this.redisKey = "cosmoislands:island:settings:"+islandId;
    }

    CompletableFuture<Void> migrate(Map<IslandSetting, String> map){
        Map<String, String> hashMap = new HashMap<>();
        for (IslandSetting key : map.keySet()) {
            hashMap.put(key.name(), map.get(key));
        }
        return async.hmset(redisKey, hashMap).thenRun(()->{}).toCompletableFuture();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return async.del(redisKey).toCompletableFuture().thenRun(()->{});
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public CompletableFuture<String> getDisplayname() {
        return async.hget(redisKey, IslandSetting.DISPLAY_NAME.name()).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> setDisplayname(String name) throws IllegalArgumentException {
        return async.hset(redisKey, IslandSetting.DISPLAY_NAME.name(), name).toCompletableFuture().thenRun(()->{});
    }

    @Override
    public CompletableFuture<String> getSettingAsync(IslandSetting setting) {
        return async.hget(redisKey, setting.name()).toCompletableFuture();
    }

    @Override
    public String getSetting(IslandSetting settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> setSetting(IslandSetting setting, String value) {
        return async.hset(redisKey, setting.name(), value).toCompletableFuture().thenRun(()->{});
    }

    @Override
    public CompletableFuture<Map<IslandSetting, String>> asMap() {
        return async.hgetall(redisKey).thenApply(map->{
            Map<IslandSetting, String> hashMap = new HashMap<>();
            for (String key : map.keySet()) {
                try {
                    hashMap.put(IslandSetting.valueOf(key), map.get(key));
                }catch (IllegalArgumentException e){
                    continue;
                }
            }
            return hashMap;
        }).toCompletableFuture();
    }
}
