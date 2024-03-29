package kr.cosmoislands.cosmoislands.level;

import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.level.IslandLevel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandLevel implements IslandLevel {

    private final int islandId;
    private final IslandLevelDataModel model;

    @Override
    public CompletableFuture<Integer> getLevel() {
        return model.getLevel(islandId);
    }

    @Override
    public CompletableFuture<Void> addLevel(int value) {
        return model.addLevel(islandId, value);
    }

    @Override
    public CompletableFuture<Void> setLevel(int value) {
        return model.setLevel(islandId, value);
    }

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
}
