package kr.cosmoisland.cosmoislands.api;

import java.util.concurrent.CompletableFuture;

public interface IslandCloud {

    IslandServer getServer(String name);

    IslandServer getLocalServer();

    CompletableFuture<IslandServer> getLocated(int islandId);
}
