package kr.cosmoislands.cosmoislands.world;

import kr.cosmoislands.cosmoislands.api.world.IslandWorldHandler;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

//todo: 나중에 remoted island 를 위해 WorldHandler 작업해두기
@RequiredArgsConstructor
public class RemotedWorldHandler implements IslandWorldHandler {

    private final int islandId;
    private final WorldHandlerController controller;

    @Override
    public CompletableFuture<Void> init() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> runOperation(String key) {
        return null;
    }
}
