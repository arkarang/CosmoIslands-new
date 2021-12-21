package kr.cosmoisland.cosmoislands.protection;

import kr.cosmoisland.cosmoislands.api.ComponentLifecycle;
import kr.cosmoisland.cosmoislands.api.IslandContext;
import kr.cosmoisland.cosmoislands.api.ModulePriority;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissionsMap;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandPermissionsLifecycle implements ComponentLifecycle {

    private final IslandPermissionsMapModule module;

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.MEDIUM;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        island.register(IslandPermissionsMap.class, module.get(island.getIslandId()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        island.register(IslandPermissionsMap.class, module.get(island.getIslandId()));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onUnload(IslandContext island) {
        island.getComponent(IslandPermissionsMap.class).invalidate();
        module.invalidate(island.getIslandId());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onDelete(IslandContext island) {
        island.getComponent(IslandPermissionsMap.class).invalidate();
        module.invalidate(island.getIslandId());
        return CompletableFuture.completedFuture(null);
    }
}
