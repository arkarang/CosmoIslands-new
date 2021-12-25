package kr.cosmoislands.cosmoislands.api.player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandPlayerRegistry {

    IslandPlayer get(UUID uuid);

    CompletableFuture<Integer> getIslandId(UUID uuid);

    CompletableFuture<Void> load(UUID uuid);

    CompletableFuture<Void> unload(UUID uuid);

    CompletableFuture<Void> update(UUID uuid);
}
