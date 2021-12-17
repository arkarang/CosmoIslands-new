package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.generic.IslandSettings;
import kr.cosmoisland.cosmoislands.api.generic.IslandSettingsMap;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public abstract class AbstractSettingsMap implements IslandSettingsMap {

    @Getter
    protected final ConcurrentHashMap<IslandSettings, String> map;

    public AbstractSettingsMap(Map<IslandSettings, String> origin){
        map = new ConcurrentHashMap<>(origin);
    }

    protected AbstractSettingsMap(IslandSettingsMap map) throws ExecutionException, InterruptedException {
        this(map.asMap().get());
    }

    @Override
    public CompletableFuture<String> getSetting(IslandSettings setting) {
        return CompletableFuture.completedFuture(map.get(setting));
    }

    @Override
    public CompletableFuture<Map<IslandSettings, String>> asMap() {
        return CompletableFuture.completedFuture(getMap());
    }
}
