package kr.cosmoisland.cosmoislands.api;

import kr.cosmoisland.cosmoislands.api.player.PlayerModificationStrategy;

import java.util.List;
import java.util.Map;

public interface IslandContext {

    int getIslandId();

    boolean isLocal();

    List<Class<? extends IslandComponent>> getApplied();

    <T extends IslandComponent> void register(Class<T> clazz, IslandComponent component);

    void register(String tag, PlayerModificationStrategy strategy);

    Map<String, PlayerModificationStrategy> getStrategies();

    <T extends IslandComponent> T getComponent(Class<T> clazz);

}
