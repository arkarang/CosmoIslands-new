package kr.cosmoislands.cosmoislands.upgrade;

import kr.cosmoislands.cosmoislands.api.AbstractLocation;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.bank.IslandInventory;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeCondition;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoislands.cosmoislands.api.world.IslandWorld;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;


@RequiredArgsConstructor
public class UpgradeConditionFactory {

    final IslandUpgradeSettingRegistry registry;

    IslandUpgradeCondition buildCondition(IslandUpgradeType type, Island island){
        CompletableFuture<IslandUpgradeSettings> setting = registry.getSetting(type);
        IslandUpgradeCondition condition = null;
        switch (type){
            case BORDER_SIZE:
                condition = new EconomyIslandUpgradeCondition(type, setting, island) {
                    @Override
                    protected CompletableFuture<Void> executeUpgrade(Island island, int value) {
                        IslandWorld world = island.getComponent(IslandWorld.class);
                        int minX, maxX;
                        int minZ, maxZ;
                        minX = -(value/2);
                        maxX = value/2;
                        minZ = -(value/2);
                        maxZ = value/2;
                        return world.setBorder(new AbstractLocation(minX, 0, minZ), new AbstractLocation(maxX, 0, maxZ))
                                .thenCombine(world.sync(), (ignored, ignored2)->null);
                    }
                };
                break;
            case INVENTORY_SIZE:
                condition = new EconomyIslandUpgradeCondition(type, setting, island) {
                    @Override
                    protected CompletableFuture<Void> executeUpgrade(Island island, int value) {
                        IslandInventory bank = island.getComponent(IslandInventory.class);
                        return bank.setLevel(value);
                    }
                };
                break;
            case MAX_PLAYERS:
                condition = new EconomyIslandUpgradeCondition(type, setting, island) {
                    @Override
                    protected CompletableFuture<Void> executeUpgrade(Island island, int value) {
                        IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                        return playersMap.setMaxPlayers(value);
                    }
                };
                break;
            case MAX_INTERNS:
                condition = new EconomyIslandUpgradeCondition(type, setting, island) {
                    @Override
                    protected CompletableFuture<Void> executeUpgrade(Island island, int value) {
                        IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                        return playersMap.setMaxInterns(value);
                    }
                };
                break;
        }
        return condition;
    }

}
