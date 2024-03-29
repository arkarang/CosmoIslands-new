package kr.cosmoislands.cosmoislands.upgrade;

import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
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

    public UpgradeSettingImpl(IslandUpgradeType type, Map<Integer, Integer> values, Map<Integer, Integer> requiredCosts){
        this.type = type;
        this.valueMap = ImmutableMap.copyOf(values);
        this.requiredCostMap = ImmutableMap.copyOf(requiredCosts);
        int max = 0;
        for (Integer key : valueMap.keySet()) {
            max = Math.max(key, max);
        }
        this.maxLevel = max;

        DebugLogger.log("type: "+type.name());
        for (Map.Entry<Integer, Integer> entry : valueMap.entrySet()) {
            DebugLogger.log("valueMap: "+entry.getKey()+", "+entry.getValue());
        }

        for (Map.Entry<Integer, Integer> entry : requiredCostMap.entrySet()) {
            DebugLogger.log("valueMap: "+entry.getKey()+", "+entry.getValue());
        }
    }

    public UpgradeSettingImpl(IslandUpgradeType type, Map<Integer, PairData> map){
        Map<Integer, Integer> costMap, valueMap;
        costMap = new HashMap<>();
        valueMap = new HashMap<>();

        int max = 0;
        for (Integer key : map.keySet()) {
            max = Math.max(key, max);
        }

        this.maxLevel = max;
        this.type = type;

        for(int i = 1; i <= maxLevel; i++){
            costMap.put(i, map.get(i).getCost());
            valueMap.put(i, map.get(i).getValue());
        }

        this.valueMap = ImmutableMap.copyOf(valueMap);
        this.requiredCostMap = ImmutableMap.copyOf(costMap);
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
