package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoisland.cosmoislands.api.ComponentLifecycle;
import kr.cosmoisland.cosmoislands.api.IslandContext;
import kr.cosmoisland.cosmoislands.api.ModulePriority;
import kr.cosmoisland.cosmoislands.api.warp.IslandWarpsMap;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandWarpLifecycle implements ComponentLifecycle {

    private final IslandWarpModule module;

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.MEDIUM;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        island.register(IslandWarpsMap.class, module.get(island.getIslandId()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        island.register(IslandWarpsMap.class, module.get(island.getIslandId()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onUnload(IslandContext island) {
        module.invalidate(island.getIslandId());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onDelete(IslandContext island) {
        module.invalidate(island.getIslandId());
        return CompletableFuture.completedFuture(null);
    }
}
