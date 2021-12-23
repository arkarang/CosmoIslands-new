package kr.cosmoislands.cosmoislands.level;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.level.IslandLevel;
import kr.cosmoislands.cosmoislands.api.level.IslandRewardData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public abstract class AbstractRewardData implements IslandRewardData {

    @Getter
    private final int id;
    @Getter
    private final int requiredLevel;

    @Override
    public CompletableFuture<Boolean> canReceive(Island island, UUID uuid) {
        return island.getComponent(IslandLevel.class).getLevel().thenApply(level -> level >= requiredLevel);
    }

}
