package kr.cosmoisland.cosmoislands.api.player;

import kr.cosmoisland.cosmoislands.api.IslandModule;

public interface IslandPlayersMapModule extends IslandModule<IslandPlayersMap> {

    ModificationStrategyRegistry getStrategyRegistry();

}
