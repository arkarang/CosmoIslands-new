package kr.cosmoislands.cosmoislands.upgrade;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class UpgradeSettingImpl implements IslandUpgradeSettings {

    @Getter
    final IslandUpgradeType type;
    @Getter
    final int maxLevel;
    final ImmutableMap<Integer, Integer> valueMap;
    final ImmutableMap<Integer, Integer> requiredCostMap;

    UpgradeSettingImpl(IslandUpgradeType type, Map<Integer, Integer> values, Map<Integer, Integer> requiredCosts){
        this.type = type;
        this.valueMap = ImmutableMap.copyOf(values);
        this.requiredCostMap = ImmutableMap.copyOf(requiredCosts);
        int max = 0;
        for (Integer key : valueMap.keySet()) {
            max = Math.max(key, max);
        }
        this.maxLevel = max;
    }

    @Override
    public int getValue(int level) {
        if(valueMap.containsKey(level)) {
            return valueMap.get(level);
        }else{
            throw new IllegalArgumentException("exceeded level: "+level);
        }
    }

    @Override
    public int getRequiredCost(int level) {
        if(requiredCostMap.containsKey(level)) {
            return requiredCostMap.get(level);
        }else{
            throw new IllegalArgumentException("could not found cost: "+level);
        }
    }

    @Override
    public Map<Integer, PairData> asMap() {
        Map<Integer, PairData> map = new HashMap<>();
        for(int i = 1; i <= maxLevel; i++){
            int value = valueMap.get(i);
            int requiredCost = requiredCostMap.get(i);
            map.put(i, new PairData(value, requiredCost));
        }
        return map;
    }

}
