package kr.cosmoislands.cosmoislands.points;

import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.points.IslandPoints;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandPoints implements IslandPoints {

    final int islandId;
    final IslandPointDataModel model;

    @Override
    public CompletableFuture<Integer> getPoints() {
        return model.getPoints(islandId);
    }

    @Override
    public CompletableFuture<Void> addPoint(int value) {
        return model.addPoints(islandId, value);
    }

    @Override
    public CompletableFuture<Void> setPoint(int value) {
        return model.setPoints(islandId, value);
    }

    @Override
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
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
