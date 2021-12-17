package kr.cosmoisland.cosmoislands.core;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.*;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.PlayerModificationStrategy;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CosmoIsland implements Island {

    @Getter
    private final int id;
    private final IslandPlayerRegistry playerRegistry;
    private final IslandCloud cloud;
    private final ImmutableMap<Class<? extends IslandComponent>, IslandComponent> components;

    CosmoIsland(IslandContext context, IslandPlayerRegistry players, IslandCloud cloud){
        this.id = context.getIslandId();
        this.playerRegistry = players;
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
    public CompletableFuture<Boolean> isLoaded() {
        return getLocated().thenApply(Objects::nonNull);
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
        return null;
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.allOf(components.values().stream().map(IslandComponent::invalidate).toArray(CompletableFuture[]::new));
    }

    @Override
    public boolean validate() {
        for (IslandComponent component : components.values()) {
            if(component.validate()){

            }
        }
        return true;
    }
}
