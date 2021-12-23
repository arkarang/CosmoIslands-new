package kr.cosmoislands.cosmoislands.core;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.api.*;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CosmoIslandRegistry implements IslandRegistry {

    @Getter
    private final int allocatedMaxSize;
    private final IslandService service;
    private final IslandHostServer localServer;

    private final HashBiMap<Class<? extends IslandComponent>, Byte> componentIds;
    private final ConcurrentHashMap<Integer, Island> localIslands;
    private final ConcurrentHashMap<Integer, Island> remotedIslands;

    CosmoIslandRegistry(int maxSize, IslandService service, IslandHostServer hostServer){
        this.allocatedMaxSize = maxSize;
        this.localServer = hostServer;
        this.service = service;
        this.localIslands = new ConcurrentHashMap<>();
        this.remotedIslands = new ConcurrentHashMap<>();
        this.componentIds = HashBiMap.create();
        this.componentIds.put(Island.class, Island.COMPONENT_ID);
    }

    @Override
    public CompletableFuture<Island> getIsland(int islandId) {
        if(localIslands.containsKey(islandId)){
            return CompletableFuture.completedFuture(localIslands.get(islandId));
        }else if(remotedIslands.containsKey(islandId)) {
            return CompletableFuture.completedFuture(remotedIslands.get(islandId));
        }else{
            return service.loadIsland(islandId, false);
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
            localServer.registerIsland(island, System.currentTimeMillis());
        }else {
            remotedIslands.put(island.getId(), island);
        }
    }

    @Override
    public Island unregisterIsland(int islandId) {
        Island island = localIslands.remove(islandId);
        localServer.unregisterIsland(island);
        return island;
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
