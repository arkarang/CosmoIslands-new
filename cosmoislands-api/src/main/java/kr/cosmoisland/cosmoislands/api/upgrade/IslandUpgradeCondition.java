package kr.cosmoisland.cosmoislands.api.upgrade;

import kr.cosmoisland.cosmoislands.api.Island;

public interface IslandUpgradeCondition {

    IslandUpgradeType getType();

    IslandUpgradeSettings getSettings();

    boolean canUpgrade(Island island);

    void upgrade(Island island);
}
