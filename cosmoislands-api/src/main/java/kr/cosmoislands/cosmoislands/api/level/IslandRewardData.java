package kr.cosmoislands.cosmoislands.api.level;

import kr.cosmoislands.cosmoislands.api.Island;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandRewardData {

    int getId();

    int getRequiredLevel();

    CompletableFuture<Boolean> canReceive(Island island, UUID uuid);

    void provide(UUID uuid);
}
