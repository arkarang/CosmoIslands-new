package kr.cosmoisland.cosmoislands.api.warp;

import java.util.concurrent.CompletableFuture;

public interface IslandUserWarp {

    String getName();

    CompletableFuture<IslandLocation> getLocation();

    CompletableFuture<Void> setLocation(IslandLocation loc);

    CompletableFuture<Void> delete();
}
