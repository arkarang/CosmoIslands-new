package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.warp.IslandLocation;
import kr.cosmoisland.cosmoislands.api.warp.IslandWarp;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandWarps implements IslandWarp {

    private final int islandId;
    private final MySQLIslandWarpsDataModel model;
    private static final String SPAWN = "!spawn";

    @Override
    public CompletableFuture<IslandLocation> getSpawnLocation() {
        return model.getLocation(islandId, SPAWN).thenApply(this::toIslandLocation);
    }


    @Override
    public CompletableFuture<Void> setSpawnLocation(AbstractLocation location) {
        return model.setLocation(islandId, SPAWN, location);
    }

    @Override
    public CompletableFuture<IslandLocation> getWarp(String name) {
        return model.getLocation(islandId, name).thenApply(this::toIslandLocation);
    }

    @Override
    public CompletableFuture<Void> deleteWarp(String name) {
        return model.delete(islandId, name);
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean validate() {
        return true;
    }

    private IslandLocation toIslandLocation(AbstractLocation location){
        return new IslandLocation(this.islandId, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }
}
