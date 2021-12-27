package kr.cosmoislands.cosmoislands.bungee.config;

import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoislands.cosmoislands.core.config.AbstractMySQLIslandConfiguration;
import kr.cosmoislands.cosmoislands.core.config.MySQLPropertyDataModel;
import kr.cosmoislands.cosmoislands.upgrade.UpgradeSettingImpl;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MySQLBungeeIslandConfiguration extends AbstractMySQLIslandConfiguration {

    public MySQLBungeeIslandConfiguration(MySQLPropertyDataModel model) {
        super(model);
    }

    @Override
    public Map<IslandUpgradeType, IslandUpgradeSettings> getDefaultUpgradeSettings() {
        Map<IslandUpgradeType, IslandUpgradeSettings> map = new HashMap<>();
        for (IslandUpgradeType value : IslandUpgradeType.values()) {
            map.put(value, getDefaultUpgradeSettings(value));
        }
        return map;
    }

    @Override
    protected IslandUpgradeSettings getDefaultUpgradeSettings(IslandUpgradeType type) {
        String rootKey = upgradeSettingsPrefix+type.name()+".";
        Map<String, String> map = model.getValuesLike(rootKey);
        Map<Integer, Integer> valuesMap = new HashMap<>();
        Map<Integer, Integer> requiredCostMap = new HashMap<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().replace(rootKey, "");
            String[] splited = key.split("\\.");
            int level = Integer.parseInt(splited[0]);
            String subKey = splited[1];
            int intValue = Integer.parseInt(entry.getValue());
            switch (subKey){
                case "requiredCost":
                    requiredCostMap.put(level, intValue);
                    break;
                case "value":
                    valuesMap.put(level, intValue);
                    break;
            }
        }

        return new UpgradeSettingImpl(type, valuesMap, requiredCostMap);
    }

    @Override
    public CompletableFuture<Void> setDefaultUpgradeSettings(Map<IslandUpgradeType, IslandUpgradeSettings> map){
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<IslandUpgradeType, IslandUpgradeSettings> entry : map.entrySet()) {
            futures.add(setDefaultUpgradeSettings(entry.getKey(), entry.getValue()));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    protected CompletableFuture<Void> setDefaultUpgradeSettings(IslandUpgradeType type, IslandUpgradeSettings settings){
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for(int i = 1; i <= settings.getMaxLevel(); i++) {
            final int level = i;
            String requiredCostKey = upgradeSettingsPrefix + type.name() + "." + level + "." + "requiredCost";
            String valueKey = upgradeSettingsPrefix + type.name() + "." + level + "." + "value";
            val future1 = model.setValue(requiredCostKey, settings.getRequiredCost(level) + "");
            val future2 =model.setValue(valueKey, settings.getValue(level) + "");
            futures.add(CompletableFuture.allOf(future1, future2));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

}
