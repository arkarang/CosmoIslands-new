package kr.cosmoislands.cosmoislands.api.world;

import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface WorldOperation<T extends IslandWorldHandler> {

    CompletableFuture<Boolean> execute(T handler, IslandSettingsMap settingsMap);

}
