package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.settings.IslandData;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@AllArgsConstructor
public abstract class AbstractIslandData implements IslandData {

    protected AtomicReference<String> displayname;

    protected AtomicBoolean isPrivate;

    protected AtomicInteger level;
    protected AtomicInteger points;

    protected AtomicInteger minX;
    protected AtomicInteger minZ;

    protected AtomicInteger weight;
    protected AtomicInteger length;

    protected AtomicInteger maxPlayers;
    protected AtomicInteger maxInterns;

    protected AtomicReference<AbstractLocation> spawnLocation;

    protected AbstractIslandData(IslandData data) throws ExecutionException, InterruptedException {
        this.displayname = new AtomicReference<>(data.getDisplayname().get());
        this.isPrivate = new AtomicBoolean(data.isPrivate().get());
        this.level = new AtomicInteger(data.getLevel().get());
        this.points = new AtomicInteger(data.getPoints().get());
        this.minX = new AtomicInteger(data.getMinX().get());
        this.minZ = new AtomicInteger(data.getMinZ().get());
        this.weight = new AtomicInteger(data.getWeight().get());
        this.length = new AtomicInteger(data.getLength().get());
        this.maxPlayers = new AtomicInteger(data.getMaxPlayers().get());
        this.maxInterns = new AtomicInteger(data.getMaxInterns().get());
        this.spawnLocation = new AtomicReference<>(data.getSpawnLocation().get());
    }

    @Override
    public CompletableFuture<Boolean> isPrivate() {
        return CompletableFuture.completedFuture(isPrivate.get());
    }

    @Override
    public CompletableFuture<Integer> getLevel() {
        return CompletableFuture.completedFuture(level.get());
    }

    @Override
    public CompletableFuture<Integer> getPoints() {
        return CompletableFuture.completedFuture(points.get());
    }

    @Override
    public CompletableFuture<Integer> getMinX() {
        return CompletableFuture.completedFuture(minX.get());
    }

    @Override
    public CompletableFuture<Integer> getMinZ() {
        return CompletableFuture.completedFuture(minZ.get());
    }

    @Override
    public CompletableFuture<Integer> getWeight() {
        return CompletableFuture.completedFuture(weight.get());
    }

    @Override
    public CompletableFuture<Integer> getLength() {
        return CompletableFuture.completedFuture(length.get());
    }

    @Override
    public CompletableFuture<String> getDisplayname() {
        return CompletableFuture.completedFuture(displayname.get());
    }

    @Override
    public CompletableFuture<Integer> getMaxPlayers() {
        return CompletableFuture.completedFuture(maxPlayers.get());
    }

    @Override
    public CompletableFuture<Integer> getMaxInterns() {
        return CompletableFuture.completedFuture(maxInterns.get());
    }
    @Override
    public CompletableFuture<AbstractLocation> getSpawnLocation() {
        return CompletableFuture.completedFuture(spawnLocation.get());
    }

}
