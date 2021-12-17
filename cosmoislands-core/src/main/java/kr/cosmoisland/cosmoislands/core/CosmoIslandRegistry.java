package kr.cosmoisland.cosmoislands.core;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.*;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

public class CosmoIslandRegistry implements IslandRegistry {

    @Getter
    private final int allocatedMaxSize;
    private final IslandServer localServer;
    private final ConcurrentHashMap<Integer, IslandLocal> localIslands;

    CosmoIslandRegistry(int maxSize, IslandServer localServer){
        this.allocatedMaxSize = maxSize;
        this.localServer = localServer;
        this.localIslands = new ConcurrentHashMap<>();
    }

    @Override
    public Island getIsland(int islandId) {
        //todo: implements this.
        return localIslands.getOrDefault(islandId, /* todo: create island remote */null);
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
        if(island instanceof IslandLocal) {
            localIslands.put(island.getId(), (IslandLocal) island);
            localServer.registerIsland(island, System.currentTimeMillis());
        }else {
            throw new IllegalArgumentException("only Local island can register");
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
    }
}
