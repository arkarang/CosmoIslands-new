package kr.cosmoislands.cosmoislands.api;

import java.util.concurrent.CompletableFuture;

public interface IslandHostServer extends IslandServer{

    CompletableFuture<Void> registerServer();

    CompletableFuture<Void> shutdown();
}
