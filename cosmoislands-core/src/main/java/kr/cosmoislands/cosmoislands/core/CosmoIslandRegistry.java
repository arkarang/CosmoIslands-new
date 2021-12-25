package kr.cosmoislands.cosmoislands.core;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.api.*;
import lombok.Getter;
import lombok.val;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CosmoIslandRegistry implements IslandRegistry {

    @Getter
    private final int allocatedMaxSize;
    private final IslandService service;

    private final HashBiMap<Class<? extends IslandComponent>, Byte> componentIds;
    private final ConcurrentHashMap<Integer, Island> localIslands;
    private final ConcurrentHashMap<Integer, Island> remotedIslands;

    CosmoIslandRegistry(int maxSize, IslandService service){
        this.allocatedMaxSize = maxSize;
        this.service = service;
        this.localIslands = new ConcurrentHashMap<>();
        this.remotedIslands = new ConcurrentHashMap<>();
        this.componentIds = HashBiMap.create();
        this.componentIds.put(Island.class, Island.COMPONENT_ID);
    }

    @Override
    public CompletableFuture<Island> getIsland(int islandId) {
        DebugLogger.log("island registry: get island: "+islandId);
        if(islandId == Island.NIL_ID){
            return CompletableFuture.completedFuture(null);
        }

        if(localIslands.containsKey(islandId)){
            return CompletableFuture.completedFuture(localIslands.get(islandId));
        }else if(remotedIslands.containsKey(islandId)) {
            return CompletableFuture.completedFuture(remotedIslands.get(islandId));
        }else{
            val future = service.loadIsland(islandId, false);
            future.thenAccept(island->{
                if(island != null){
                    remotedIslands.put(island.getId(), island);
                }
            });
            return future;
        }
    }

    @Override
    public ImmutableMap<Integer, Island> getLocals() {
        return ImmutableMap.copyOf(localIslands);
    }

    @Override
    public Island getLocalIsland(int islandId) {
        return localIslands.get(islandId);
    }

    @Override
    public void registerIsland(Island island) {
        if(island.isLocal()) {
            localIslands.put(island.getId(), island);
        }else {
            remotedIslands.put(island.getId(), island);
        }
    }

    @Override
    public Island unregisterIsland(int islandId) {
        return localIslands.remove(islandId);
    }

    @Override
    public void registerComponentId(Class<? extends IslandComponent> clazz, byte id) throws IllegalArgumentException {
        if(componentIds.containsKey(clazz)){
            throw new IllegalArgumentException("component already registered: "+clazz.getSimpleName());
        }else{
            componentIds.put(clazz, id);
        }
    }

    @Override
    public byte getComponentId(Class<? extends IslandComponent> clazz) {
        return componentIds.getOrDefault(clazz, (byte)-1);
    }

    @Override
    public Class<? extends IslandComponent> getComponentClass(byte id) {
        return componentIds.inverse().get(id);
    }

    @Override
    public void clear() {
        localIslands.clear();
        remotedIslands.clear();
    }
}
