package kr.cosmoisland.cosmoislands.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface IslandService {

    ExternalRepository getExternalRepository();

    IslandFactory getFactory();

    IslandRegistry getRegistry();

    IslandPacemaker getPacemaker();

    IslandCloud getCloud();

    <T extends IslandComponent> IslandModule<T> getModule(Class<T> clazz);

    <T extends IslandComponent> void registerModule(Class<T> clazz, IslandModule<T> module);

    void init();

    void shutdown() throws ExecutionException, InterruptedException;

    CompletableFuture<Island> loadIsland(int id);

    CompletableFuture<Boolean> unloadIsland(int id);

    CompletableFuture<Island> createIsland(UUID uuid);

    CompletableFuture<Boolean> deleteIsland(int id);

}
