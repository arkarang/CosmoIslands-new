package kr.cosmoisland.cosmoislands.api;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// Service(호출) -> Factory(생성) -> Registry(적재) -> Controller? 서비스간 커뮤니케이션?
public interface IslandRegistry {

    int getAllocatedMaxSize();

    Island getIsland(int islandId);

    ImmutableMap<Integer, Island> getLocals();

    Island getLocalIsland(int islandId);

    void registerIsland(Island island);

    Island unregisterIsland(int islandId);

    void clear();


}