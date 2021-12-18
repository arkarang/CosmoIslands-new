package kr.cosomoisland.cosmoislands.world;

import com.minepalm.manyworlds.api.ManyWorld;
import com.minepalm.manyworlds.api.WorldService;
import com.minepalm.manyworlds.api.bukkit.WorldCategory;
import com.minepalm.manyworlds.api.entity.WorldInform;
import com.minepalm.manyworlds.core.WorldToken;
import com.minepalm.manyworlds.core.WorldTokens;
import kr.cosmoisland.cosmoislands.api.ComponentLifecycle;
import kr.cosmoisland.cosmoislands.api.IslandContext;
import kr.cosmoisland.cosmoislands.api.ModulePriority;
import kr.cosmoisland.cosmoislands.api.world.IslandWorld;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandWorldLifecycle implements ComponentLifecycle {

    private final CosmoIslandWorldModule module;
    private final WorldService service;
    private final WorldCategory category = WorldToken.get("ISLAND");
    private final WorldInform defaultWorld = new WorldInform(WorldTokens.TYPE, "ISLAND");

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.HIGH;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        CompletableFuture<ManyWorld> worldFuture = service.loadWorld(toInform(island));
        CompletableFuture<IslandWorld> world = worldFuture.thenApply(mw->module.create(island.getIslandId(), mw));
        module.register(island.getIslandId(), world);
        return world.thenAccept(islandWorld->island.register(IslandWorld.class, islandWorld));
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        CompletableFuture<ManyWorld> worldFuture = service.createNewWorld(defaultWorld, toInform(island));
        CompletableFuture<IslandWorld> world = worldFuture.thenApply(mw->module.create(island.getIslandId(), mw));
        module.register(island.getIslandId(), world);
        return world.thenAccept(islandWorld->island.register(IslandWorld.class, islandWorld));
    }

    @Override
    public CompletableFuture<Void> onUnload(IslandContext island) {
        IslandManyWorld imw = (IslandManyWorld)(module.get(island.getIslandId()));
        module.unregister(island.getIslandId());
        return imw.getManyWorld().unload().thenRun(()->{});
    }

    @Override
    public CompletableFuture<Void> onDelete(IslandContext island) {
        module.unregister(island.getIslandId());
        return service.delete(toInform(island));
    }

    private WorldInform toInform(IslandContext island){
        return new WorldInform(category, "island_"+island.getIslandId());
    }
}