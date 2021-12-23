package kr.cosmoislands.cosmoislands.api.member;

import kr.cosmoislands.cosmoislands.api.IslandModule;

public interface IslandPlayersMapModule extends IslandModule<IslandPlayersMap> {

    ModificationStrategyRegistry getStrategyRegistry();

}
