package kr.cosmoislands.cosmoislands.api.world;

import java.util.concurrent.CompletableFuture;

public interface IslandWorldHandler {

    CompletableFuture<Void> init();

    CompletableFuture<Boolean> runOperation(String key);


}
