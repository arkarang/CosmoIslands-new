package kr.cosmoisland.cosmoislands.api.upgrade;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandUpgrade extends IslandComponent {

    byte COMPONENT_ID = 11;

    IslandUpgradeCondition getCondition(IslandUpgradeType type);

    CompletableFuture<Integer> getLevel(IslandUpgradeType type);

    CompletableFuture<Void> setLevel(IslandUpgradeType type, int level);

}
