package kr.cosmoisland.cosmoislands.api.player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandPlayerRegistry {

    IslandPlayer get(UUID uuid);

    CompletableFuture<Integer> getIslandId(UUID uuid);

    void load(UUID uuid);

    void unload(UUID uuid);
}
