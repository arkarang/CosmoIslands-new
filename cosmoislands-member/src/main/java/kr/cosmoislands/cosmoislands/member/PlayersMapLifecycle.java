package kr.cosmoislands.cosmoislands.member;

import kr.cosmoislands.cosmoislands.api.ComponentLifecycle;
import kr.cosmoislands.cosmoislands.api.IslandContext;
import kr.cosmoislands.cosmoislands.api.ModulePriority;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class PlayersMapLifecycle implements ComponentLifecycle {

    final PlayersMapModule module;
    final IslandPlayerRegistry playerRegistry;
    final PlayersMapRegistry registry;

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.HIGH;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        island.register(IslandPlayersMap.class, module.get(island.getIslandId()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        CosmoIslandPlayersMap playersMap = (CosmoIslandPlayersMap)module.get(island.getIslandId());
        island.register(IslandPlayersMap.class, playersMap);
        return module.create(island.getIslandId(), owner)
                .thenCompose(ignored-> playersMap.getRedis().migrate(playersMap.getMysql()));
    }

    @Override
    public CompletableFuture<Void> onUnload(IslandContext island) {
        registry.invalidate(island.getIslandId());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onDelete(IslandContext island) {
        IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
        return island.getComponent(IslandPlayersMap.class).getMembers().thenCompose(map -> {
            map.keySet().forEach(playerRegistry::unload);
            return playersMap.invalidate();
        });
    }
}
