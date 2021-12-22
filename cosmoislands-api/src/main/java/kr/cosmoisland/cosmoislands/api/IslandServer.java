package kr.cosmoisland.cosmoislands.api;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandServer {

    enum Type{
        ISLAND, PROXY, LOBBY;
    }

    String getName();

    Type getType();

    CompletableFuture<Boolean> isOnline();

    CompletableFuture<Boolean> registerIsland(Island island, long uptime);

    CompletableFuture<Boolean> unregisterIsland(Island island);

    CompletableFuture<List<Integer>> getIslands();

    CompletableFuture<Integer> getLoadedCount();

    CompletableFuture<Island> create(UUID uuid);

    CompletableFuture<Island> load(int islandId);

    CompletableFuture<Boolean> unload(int islandId);

    CompletableFuture<Boolean> delete(int islandId);

    <T extends IslandComponent> CompletableFuture<Boolean> sync(int islandId, Class<T> component);

    CompletableFuture<Boolean> syncIsland(int islandId);

}
