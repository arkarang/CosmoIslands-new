package kr.cosmoislands.cosmoislands.api.world;

public interface WorldHandlerFactory {

    IslandWorldHandler build(int islandId, WorldOperationRegistry operationRegistry);
}
