package kr.cosmoisland.cosmoislands.api.player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandInternshipRegistry {

    IslandInternship get(UUID uuid);

    CompletableFuture<List<Integer>> getHiredIslandIds(UUID uuid);

    CompletableFuture<Integer> getMaxInternships(UUID uuid);
}
