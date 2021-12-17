package kr.cosmoisland.cosmoislands.api.player;


import kr.cosmoisland.cosmoislands.api.Island;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandPlayer {

    UUID getUniqueId();

    CompletableFuture<Island> getIsland();

    CompletableFuture<Integer> getIslandId();

    void invalidate();

}
