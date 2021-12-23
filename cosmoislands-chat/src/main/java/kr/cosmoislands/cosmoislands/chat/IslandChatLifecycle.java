package kr.cosmoislands.cosmoislands.chat;

import kr.cosmoislands.cosmoislands.api.ComponentLifecycle;
import kr.cosmoislands.cosmoislands.api.IslandContext;
import kr.cosmoislands.cosmoislands.api.ModulePriority;
import kr.cosmoislands.cosmoislands.api.chat.IslandChat;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandChatLifecycle implements ComponentLifecycle {

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
        return module.getAsync(island.getIslandId()).thenAccept(component->{
            island.register(IslandChat.class, component);
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
        return island.getComponent(IslandChat.class).invalidate();
    }
}
