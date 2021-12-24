package kr.cosmoislands.cosmoislands.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public interface IslandModule<T> {

    CompletableFuture<T> getAsync(int islandId);

    T get(int islandId);

    void invalidate(int islandId);

    CompletableFuture<Void> create(int islandId, UUID uuid);

    void onEnable(IslandService service);

    void onDisable(IslandService service);

    Logger getLogger();
}
