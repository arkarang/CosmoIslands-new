package kr.cosmoisland.cosmoislands.api.level;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IslandAchievements extends IslandComponent {

    byte COMPONENT_ID = 4;

    CompletableFuture<Boolean> isAchieved(int id);

    CompletableFuture<Void> setAchieved(int id, boolean b);

    CompletableFuture<Map<Integer, Boolean>> asMap();
}
