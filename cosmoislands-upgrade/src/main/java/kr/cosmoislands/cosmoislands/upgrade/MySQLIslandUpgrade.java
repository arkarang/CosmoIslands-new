package kr.cosmoislands.cosmoislands.upgrade;

import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgrade;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeCondition;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeType;

import java.util.concurrent.CompletableFuture;

public class MySQLIslandUpgrade implements IslandUpgrade {

    final int islandId;
    final IslandRegistry registry;
    final MySQLIslandUpgradeDataModel model;
    final UpgradeConditionFactory factory;

    MySQLIslandUpgrade(int islandId, IslandRegistry registry, MySQLIslandUpgradeDataModel model, UpgradeConditionFactory factory){
        this.islandId = islandId;
        this.registry = registry;
        this.model = model;
        this.factory = factory;
    }

    @Override
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public IslandUpgradeCondition getCondition(IslandUpgradeType type) {
        return factory.buildCondition(type, registry.getIsland(islandId));
    }

    @Override
    public CompletableFuture<Integer> getLevel(IslandUpgradeType type) {
        return model.getLevel(islandId, type);
    }

    @Override
    public CompletableFuture<Void> setLevel(IslandUpgradeType type, int level) {
        return model.setLevel(islandId, type, level);
    }
}
