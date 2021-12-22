package kr.cosmoisland.cosmoislands.core;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.*;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CosmoIslandRegistry implements IslandRegistry {

    @Getter
    private final int allocatedMaxSize;
    private final IslandService service;
    private final IslandCloud cloud;
    private final IslandHostServer localServer;
    private final ConcurrentHashMap<Integer, Island> localIslands;
    private final ConcurrentHashMap<Integer, Island> remotedIslands;

    CosmoIslandRegistry(int maxSize, IslandService service, IslandCloud cloud){
        this.allocatedMaxSize = maxSize;
        this.cloud = cloud;
        this.localServer = cloud.getHostServer();
        this.service = service;
        this.localIslands = new ConcurrentHashMap<>();
        this.remotedIslands = new ConcurrentHashMap<>();
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
    public void clear() {
        localIslands.clear();
        remotedIslands.clear();
    }
}
