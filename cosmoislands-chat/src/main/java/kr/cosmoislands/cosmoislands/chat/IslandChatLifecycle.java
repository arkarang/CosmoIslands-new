package kr.cosmoislands.cosmoislands.chat;

import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmoislands.api.ComponentLifecycle;
import kr.cosmoislands.cosmoislands.api.IslandContext;
import kr.cosmoislands.cosmoislands.api.ModulePriority;
import kr.cosmoislands.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandChatLifecycle implements ComponentLifecycle {

    private final CosmoChat cosmoChat;
    private final IslandChatModule module;

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.MEDIUM;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        return module.getAsync(island.getIslandId()).thenAccept(component->{
            island.register(IslandChat.class, component);
        });
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        return module.getAsync(island.getIslandId()).thenCompose(component->{
            island.register(IslandChat.class, component);
            return module.create(island.getIslandId(), owner).thenCompose(ignored->{
                val future1 = component.add(owner);
                val future2 = future1.thenCompose(ignored2->component.setOwner(owner));
                return CompletableFuture.allOf(future1, future2);
            });
        });
    }

    @Override
    public CompletableFuture<Void> onUnload(IslandContext island) {
        module.invalidate(island.getIslandId());
        return island.getComponent(IslandChat.class).invalidate();
    }

    @Override
    public CompletableFuture<Void> onDelete(IslandContext island) {
        module.invalidate(island.getIslandId());
        IslandChat islandChat = island.getComponent(IslandChat.class);
        val future1 = islandChat.invalidate();
        val future2 = islandChat.disband();
        return CompletableFuture.allOf(future1, future2);
    }
}
