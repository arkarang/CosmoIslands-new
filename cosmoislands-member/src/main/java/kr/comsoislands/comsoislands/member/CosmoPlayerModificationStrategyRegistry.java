package kr.comsoislands.comsoislands.member;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.player.ModificationStrategyRegistry;
import kr.cosmoisland.cosmoislands.api.player.PlayerModificationStrategy;

import java.util.concurrent.ConcurrentHashMap;

public class CosmoPlayerModificationStrategyRegistry implements ModificationStrategyRegistry {

    final ConcurrentHashMap<String, PlayerModificationStrategy> strategies = new ConcurrentHashMap<>();

    @Override
    public void addStrategy(String tag, PlayerModificationStrategy strategy) {
        if(strategies.containsKey(tag))
            throw new IllegalArgumentException("tag "+tag+" is already exist");
        else
            strategies.put(tag, strategy);
    }

    @Override
    public ImmutableMap<String, PlayerModificationStrategy> getStrategies() {
        return ImmutableMap.copyOf(strategies);
    }
}
