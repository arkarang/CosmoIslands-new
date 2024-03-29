package kr.cosmoislands.cosmoislands.core;

import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.api.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CosmoIsland implements Island {

    @Getter
    private final int id;
    @Getter
    private final boolean isLocal;
    private final IslandCloud cloud;
    private final ImmutableMap<Class<? extends IslandComponent>, IslandComponent> components;

    CosmoIsland(IslandContext context, IslandCloud cloud){
        this.id = context.getIslandId();
        this.isLocal = context.isLocal();
        this.cloud = cloud;
        HashMap<Class<? extends IslandComponent>, IslandComponent> map = new HashMap<>();
        context.getApplied().forEach(clazz-> map.put(clazz, context.getComponent(clazz)));
        this.components = ImmutableMap.copyOf(map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> T getComponent(Class<T> clazz) {
        return (T) components.get(clazz);
    }

    @Override
    public Map<Class<? extends IslandComponent>, ? extends IslandComponent> getComponents() {
        return ImmutableMap.copyOf(components);
    }

    @Override
    public CompletableFuture<IslandStatus> getStatus() {
        return cloud.getStatus(this.id);
    }

    @Override
    public CompletableFuture<IslandServer> getLocated() {
        return cloud.getLocated(this.id);
    }

    @Override
    public <T extends IslandComponent> CompletableFuture<T> sync(Class<T> clazz) {
        return components.get(clazz).sync();
    }

    @Override
    public CompletableFuture<Island> sync() {
        return CompletableFuture.allOf(components.values().stream().map(IslandComponent::sync).toArray(CompletableFuture[]::new)).thenApply(ignored-> this);
    }

    @Override
    public CompletableFuture<Long> getUptime() {
        //todo: implements this.
        return CompletableFuture.completedFuture(0L);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.allOf(components.values().stream().map(IslandComponent::invalidate).toArray(CompletableFuture[]::new));
    }

    @Override
    public boolean validate() {
        for (IslandComponent component : components.values()) {
            if(!component.validate()){
                return false;
            }
        }
        return true;
    }
}
