package kr.cosmoisland.cosmoislands.api.level;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IslandRewardsRegistry {

    IslandRewardData getRewardData(int id);

    CompletableFuture<Void> insertRewardData(IslandRewardData data);

    CompletableFuture<Void> setRequiredLevel(int id, int level);

    CompletableFuture<List<IslandRewardData>> getAll();
}
