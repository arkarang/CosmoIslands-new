package kr.cosmoislands.cosmoislands.api.upgrade;

import java.util.concurrent.CompletableFuture;

public interface IslandUpgradeCondition {

    enum Result{
        SUCCESSFUL, NOT_ENOUGH_MONEY, REACHED_MAX_LEVEL, ALREADY_REACHED, FAILED;
    }

    IslandUpgradeType getType();

    CompletableFuture<IslandUpgradeSettings> getSettings();

    CompletableFuture<Boolean> canUpgrade();

    CompletableFuture<Boolean> hasCost();

    CompletableFuture<Boolean> isReachedMaxLevel();

    CompletableFuture<Result> upgrade();
}
