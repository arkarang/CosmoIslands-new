package kr.cosmoisland.cosmoislands.settings;

import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.core.utils.Cached;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public abstract class CosmoIslandSettingsMap implements IslandSettingsMap {

    final MySQLIslandSettingsMap mysql;
    final RedisIslandSettingsMap redis;

    CompletableFuture<Void> migrate(){
        return mysql.asMap().thenAccept(redis::migrate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return this.migrate().thenApply(ignored->(T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.allOf(mysql.invalidate(), redis.invalidate());
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public CompletableFuture<String> getDisplayname() {
        return getSettingAsync(IslandSetting.DISPLAY_NAME);
    }

    @Override
    public CompletableFuture<Void> setDisplayname(String name) throws IllegalArgumentException {
        return setSetting(IslandSetting.DISPLAY_NAME, name);
    }

    @Override
    public CompletableFuture<String> getSettingAsync(IslandSetting setting) {
        return redis.getSettingAsync(setting).thenCompose(value -> {
            if(value == null){
                CompletableFuture<String> future = mysql.getSettingAsync(setting);
                future.thenAccept(mysqlValue->redis.setSetting(setting, mysqlValue));
                return future;
            }else
                return CompletableFuture.completedFuture(value);
        });
    }

    @Override
    public CompletableFuture<Void> setSetting(IslandSetting setting, String value) {
        CompletableFuture<Void> mysqlFuture = mysql.setSetting(setting, value);
        CompletableFuture<Void> redisFuture = redis.setSetting(setting, value);
        return CompletableFuture.allOf(mysqlFuture, redisFuture);
    }

    @Override
    public CompletableFuture<Map<IslandSetting, String>> asMap() {
        return redis.asMap();
    }
}
