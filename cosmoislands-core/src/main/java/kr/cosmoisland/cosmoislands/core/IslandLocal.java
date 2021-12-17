package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.IslandServer;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class IslandLocal implements Island {

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public <T extends IslandComponent> T getComponent(Class<T> clazz) {
        return null;
    }

    @Override
    public Map<Class<? extends IslandComponent>, ? extends IslandComponent> getComponents() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> isLoaded() {
        return null;
    }

    @Override
    public CompletableFuture<IslandServer> getLocated() {
        return null;
    }

    @Override
    public CompletableFuture<Island> sync() {
        return null;
    }

    @Override
    public <T extends IslandComponent> CompletableFuture<T> sync(Class<T> clazz) {
        return null;
    }

    @Override
    public CompletableFuture<Long> getUptime() {
        return null;
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return null;
    }

    @Override
    public boolean validate() {
        return false;
    }
}
