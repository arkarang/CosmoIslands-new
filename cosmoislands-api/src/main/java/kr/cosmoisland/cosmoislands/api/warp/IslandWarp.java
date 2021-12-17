package kr.cosmoisland.cosmoislands.api.warp;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandWarp extends IslandComponent {

    CompletableFuture<IslandLocation> getSpawnLocation();

    CompletableFuture<Void> setSpawnLocation(AbstractLocation location);

    CompletableFuture<IslandLocation> getWarp(String name);

    CompletableFuture<Void> deleteWarp(String name);

}
