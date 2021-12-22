package kr.cosmoisland.cosmoislands.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IslandStatusRegistry {

    CompletableFuture<IslandStatus> getStatus(int islandId);

    CompletableFuture<Void> setStatus(int islandId, IslandStatus status);

    CompletableFuture<Void> reset(List<Integer> idList);
}
