package kr.cosmoisland.cosmoislands.api.upgrade;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

public interface IslandUpgrade extends IslandComponent {

    IslandUpgradeCondition getCondition(IslandUpgradeType type);

}
