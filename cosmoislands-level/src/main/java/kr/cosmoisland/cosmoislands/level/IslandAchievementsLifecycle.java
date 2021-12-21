package kr.cosmoisland.cosmoislands.level;

import kr.cosmoisland.cosmoislands.api.ComponentLifecycle;
import kr.cosmoisland.cosmoislands.api.IslandContext;
import kr.cosmoisland.cosmoislands.api.ModulePriority;
import kr.cosmoisland.cosmoislands.api.level.IslandAchievements;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandAchievementsLifecycle implements ComponentLifecycle {

    private final IslandAchievementsModule module;

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.MEDIUM;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        island.register(IslandAchievements.class, module.get(island.getIslandId()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        island.register(IslandAchievements.class, module.get(island.getIslandId()));
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
