package kr.cosmoislands.cosmoislands.upgrade;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeCondition;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public abstract class AbstractIslandUpgradeCondition implements IslandUpgradeCondition {

    final IslandUpgradeType type;
    final CompletableFuture<IslandUpgradeSettings> settings;
    final Island island;

}
