package kr.cosmoislands.cosmoislands.api.member;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandInternshipRegistry {

    IslandInternship get(UUID uuid);

    CompletableFuture<List<Integer>> getHiredIslandIds(UUID uuid);

    CompletableFuture<Integer> getMaxInternships(UUID uuid);

    CompletableFuture<Void> update(UUID uuid);

    CompletableFuture<Void> unload(UUID uuid);
}
