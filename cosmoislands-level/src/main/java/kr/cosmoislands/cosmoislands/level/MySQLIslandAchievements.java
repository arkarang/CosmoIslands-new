package kr.cosmoislands.cosmoislands.level;

import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.level.IslandAchievements;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandAchievements implements IslandAchievements {

    private final int islandId;
    private final IslandAchievementsDataModel model;

    @Override
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public CompletableFuture<Boolean> isAchieved(int id) {
        return model.isAchieved(islandId, id);
    }

    @Override
    public CompletableFuture<Void> setAchieved(int id, boolean b) {
        return model.setAchieved(islandId, id, b);
    }

    @Override
    public CompletableFuture<Map<Integer, Boolean>> asMap() {
        return model.getAchievements(islandId);
    }
}
