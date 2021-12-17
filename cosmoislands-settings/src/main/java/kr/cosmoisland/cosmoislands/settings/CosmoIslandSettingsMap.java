package kr.cosmoisland.cosmoislands.settings;

import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.generic.IslandSettings;
import kr.cosmoisland.cosmoislands.api.generic.IslandSettingsMap;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CosmoIslandSettingsMap implements IslandSettingsMap {

    final MySQLIslandSettingsMap mysql;
    final RedisIslandSettingsMap redis;

    CompletableFuture<Void> migrate(){
        return mysql.asMap().thenAccept(redis::migrate);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return mysql.asMap().thenCompose(redis::migrate).thenApply(ignored->(T)this);
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
        return getSetting(IslandSettings.DISPLAY_NAME);
    }

    @Override
    public CompletableFuture<Void> setDisplayname(String name) throws IllegalArgumentException {
        return setSetting(IslandSettings.DISPLAY_NAME, name);
    }

    @Override
    public CompletableFuture<String> getSetting(IslandSettings setting) {
        return redis.getSetting(setting).thenCompose(value -> {
            if(value == null){
                CompletableFuture<String> future = mysql.getSetting(setting);
                future.thenAccept(mysqlValue->redis.setSetting(setting, mysqlValue));
                return future;
            }else
                return CompletableFuture.completedFuture(value);
        });
    }

    @Override
    public CompletableFuture<Void> setSetting(IslandSettings setting, String value) {
        CompletableFuture<Void> mysqlFuture = mysql.setSetting(setting, value);
        CompletableFuture<Void> redisFuture = redis.setSetting(setting, value);
        return CompletableFuture.allOf(mysqlFuture, redisFuture);
    }

    @Override
    public CompletableFuture<Map<IslandSettings, String>> asMap() {
        return redis.asMap();
    }
}
