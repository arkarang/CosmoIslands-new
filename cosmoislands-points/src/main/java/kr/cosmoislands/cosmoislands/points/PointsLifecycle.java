package kr.cosmoislands.cosmoislands.points;

import kr.cosmoislands.cosmoislands.api.ComponentLifecycle;
import kr.cosmoislands.cosmoislands.api.IslandContext;
import kr.cosmoislands.cosmoislands.api.ModulePriority;
import kr.cosmoislands.cosmoislands.api.points.IslandPoints;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class PointsLifecycle implements ComponentLifecycle {

    final IslandPointsModule module;

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.MEDIUM;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        Supplier<Void> sup = ()->{
            island.register(IslandPoints.class, module.get(island.getIslandId()));
            return (Void)null;
        };
        return CompletableFuture.completedFuture(sup.get());
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        island.register(IslandPoints.class, module.get(island.getIslandId()));
        return module.create(island.getIslandId(), owner);
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
