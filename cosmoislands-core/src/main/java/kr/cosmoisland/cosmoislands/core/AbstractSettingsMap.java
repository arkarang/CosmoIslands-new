package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public abstract class AbstractSettingsMap implements IslandSettingsMap {

    @Getter
    protected final ConcurrentHashMap<IslandSetting, String> map;

    public AbstractSettingsMap(Map<IslandSetting, String> origin){
        map = new ConcurrentHashMap<>(origin);
    }

    protected AbstractSettingsMap(IslandSettingsMap map) throws ExecutionException, InterruptedException {
        this(map.asMap().get());
    }

    @Override
    public CompletableFuture<String> getSettingAsync(IslandSetting setting) {
        return CompletableFuture.completedFuture(map.get(setting));
    }

    @Override
    public CompletableFuture<Map<IslandSetting, String>> asMap() {
        return CompletableFuture.completedFuture(getMap());
    }
}
