package kr.cosmoisland.cosmoislands.protection;

import kr.cosmoisland.cosmoislands.api.ComponentLifecycle;
import kr.cosmoisland.cosmoislands.api.IslandContext;
import kr.cosmoisland.cosmoislands.api.ModulePriority;
import kr.cosmoisland.cosmoislands.api.protection.IslandProtection;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandProtectionLifecycle implements ComponentLifecycle {

    private final IslandProtectionModule module;

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.LOW;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        IslandProtection protection;
        if (island.isLocal()) {
            protection = module.createLocalProtection(island.getIslandId());
        }else {
            protection = module.createRemoteProtection(island.getIslandId());
        }
        module.register(island.getIslandId(), protection);
        island.register(IslandProtection.class, protection);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        return onLoad(island);
    }

    @Override
    public CompletableFuture<Void> onUnload(IslandContext island) {
        island.getComponent(IslandProtection.class).invalidate();
        module.invalidate(island.getIslandId());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> onDelete(IslandContext island) {
        island.getComponent(IslandProtection.class).invalidate();
        module.invalidate(island.getIslandId());
        return CompletableFuture.completedFuture(null);
    }
}
