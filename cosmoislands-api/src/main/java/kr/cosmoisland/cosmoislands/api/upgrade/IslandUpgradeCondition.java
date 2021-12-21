package kr.cosmoisland.cosmoislands.api.upgrade;

import kr.cosmoisland.cosmoislands.api.Island;

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
