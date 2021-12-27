package kr.cosmoislands.cosmoislands.api.world;

import java.util.concurrent.CompletableFuture;

//todo: implements this.
public interface IslandWorldHandler {

    CompletableFuture<Void> setWeather(boolean isStorm);

    CompletableFuture<Void> setTime(int ticks);

}
