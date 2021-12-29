package kr.cosmoislands.cosmoislands.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

//좋은 이름 있으면 바꿔주세요
public interface ComponentLifecycle {

    ModulePriority getPriority();

    CompletableFuture<Void> onLoad(IslandContext island);

    CompletableFuture<Void> onCreate(UUID owner, IslandContext island);

    CompletableFuture<Void> onUnload(IslandContext island);

    CompletableFuture<Void> onDelete(IslandContext island);

}
