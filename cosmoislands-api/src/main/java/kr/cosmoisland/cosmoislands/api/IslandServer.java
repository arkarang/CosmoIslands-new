package kr.cosmoisland.cosmoislands.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandServer {

    String getName();

    CompletableFuture<Boolean> registerIsland(Island island, long uptime);

    CompletableFuture<Boolean> unregisterIsland(Island island);

    CompletableFuture<Integer> getLoadedCount();

    CompletableFuture<Island> create(UUID uuid);

    CompletableFuture<Island> load(int islandId);

    CompletableFuture<Boolean> unload(int islandId);

    CompletableFuture<Boolean> delete(int islandId);

    <T extends IslandComponent> CompletableFuture<Boolean> sync(int islandId, Class<T> component);

    CompletableFuture<Boolean> syncIsland(int islandId);

}
