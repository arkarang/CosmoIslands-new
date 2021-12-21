package kr.cosmoisland.cosmoislands.api.upgrade;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandUpgrade extends IslandComponent {

    IslandUpgradeCondition getCondition(IslandUpgradeType type);

    CompletableFuture<Integer> getLevel(IslandUpgradeType type);

    CompletableFuture<Void> setLevel(IslandUpgradeType type, int level);

}
