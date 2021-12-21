package kr.cosmoisland.cosmoislands.api.upgrade;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IslandUpgradeSettings {

    @Data
    @RequiredArgsConstructor
    class PairData{
        final int value;
        final int cost;
    }

    IslandUpgradeType getType();

    int getValue(int level);

    int getRequiredCost(int level);

    Map<Integer, PairData> asMap();

    int getMaxLevel();
}
