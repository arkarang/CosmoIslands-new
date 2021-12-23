package kr.cosmoislands.cosmoislands.api.upgrade;

import kr.cosmoislands.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandUpgrade extends IslandComponent {

    byte COMPONENT_ID = 11;

    IslandUpgradeCondition getCondition(IslandUpgradeType type);

    CompletableFuture<Integer> getLevel(IslandUpgradeType type);

    CompletableFuture<Void> setLevel(IslandUpgradeType type, int level);

}
