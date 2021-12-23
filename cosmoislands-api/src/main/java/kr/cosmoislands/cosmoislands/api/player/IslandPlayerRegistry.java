package kr.cosmoislands.cosmoislands.api.player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandPlayerRegistry {

    IslandPlayer get(UUID uuid);

    CompletableFuture<Integer> getIslandId(UUID uuid);

    void load(UUID uuid);

    void unload(UUID uuid);
}
