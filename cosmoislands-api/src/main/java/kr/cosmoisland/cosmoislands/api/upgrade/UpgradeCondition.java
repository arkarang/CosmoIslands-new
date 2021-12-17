package kr.cosmoisland.cosmoislands.api.upgrade;

import kr.cosmoisland.cosmoislands.api.Island;

public interface UpgradeCondition {

    UpgradeType getType();

    UpgradeSettings getSettings();

    boolean canUpgrade(Island island);

    void upgrade(Island island);
}
