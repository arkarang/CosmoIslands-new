package kr.cosmoislands.cosmoislands.api;

import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface IslandService {

    ExternalRepository getExternalRepository();

    IslandFactory getFactory();

    IslandPlayerRegistry getPlayerRegistry();

    IslandRegistry getRegistry();

    IslandPacemaker getPacemaker();

    IslandCloud getCloud();

    <T extends IslandComponent> IslandModule<T> getModule(Class<T> clazz);

    <T extends IslandComponent> void registerModule(Class<T> clazz, IslandModule<T> module);

    void init() throws ExecutionException, InterruptedException;

    void shutdown() throws ExecutionException, InterruptedException;

    CompletableFuture<Island> loadIsland(int id, boolean isLocal);

    CompletableFuture<Boolean> unloadIsland(int id);

    CompletableFuture<Island> createIsland(UUID uuid);

    CompletableFuture<Boolean> deleteIsland(int id);

}
