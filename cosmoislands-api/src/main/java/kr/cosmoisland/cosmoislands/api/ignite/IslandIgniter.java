package kr.cosmoisland.cosmoislands.api.ignite;

import kr.cosmoisland.cosmoislands.api.IslandServer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandIgniter {

    CompletableFuture<IslandServer> loadAtLeast(int islandId);

    CompletableFuture<IslandServer> createAtLeast(UUID uuid);

}
