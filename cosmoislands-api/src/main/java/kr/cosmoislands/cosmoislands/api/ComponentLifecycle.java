package kr.cosmoislands.cosmoislands.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ComponentLifecycle {

    ModulePriority getPriority();

    CompletableFuture<Void> onLoad(IslandContext island);

    CompletableFuture<Void> onCreate(UUID owner, IslandContext island);

    CompletableFuture<Void> onUnload(IslandContext island);

    CompletableFuture<Void> onDelete(IslandContext island);

}
