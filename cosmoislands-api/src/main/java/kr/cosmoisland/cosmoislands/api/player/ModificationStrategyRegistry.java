package kr.cosmoisland.cosmoislands.api.player;

import com.google.common.collect.ImmutableMap;

public interface ModificationStrategyRegistry {

    void addStrategy(String tag, PlayerModificationStrategy strategy);

    ImmutableMap<String, PlayerModificationStrategy> getStrategies();
}
