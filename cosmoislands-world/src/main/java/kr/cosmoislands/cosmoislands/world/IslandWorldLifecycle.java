package kr.cosmoislands.cosmoislands.world;

import com.minepalm.manyworlds.api.ManyWorld;
import com.minepalm.manyworlds.api.WorldService;
import com.minepalm.manyworlds.api.bukkit.WorldCategory;
import com.minepalm.manyworlds.api.entity.WorldInform;
import com.minepalm.manyworlds.core.WorldToken;
import com.minepalm.manyworlds.core.WorldTokens;
import kr.cosmoislands.cosmoislands.api.ComponentLifecycle;
import kr.cosmoislands.cosmoislands.api.IslandContext;
import kr.cosmoislands.cosmoislands.api.ModulePriority;
import kr.cosmoislands.cosmoislands.api.world.IslandWorld;
import kr.cosmoislands.cosmoislands.world.minecraft.MinecraftWorldHandler;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandWorldLifecycle implements ComponentLifecycle {

    private final IslandWorldModule module;
    private final WorldService service;
    private final WorldCategory category = WorldToken.get("ISLAND");
    private final WorldInform defaultWorld = new WorldInform(WorldTokens.TYPE, "ISLAND");

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.HIGH;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        if(island.isLocal()) {
            CompletableFuture<ManyWorld> worldFuture = service.loadWorld(toInform(island));
            CompletableFuture<IslandWorld> world = worldFuture.thenApply(mw -> module.create(island.getIslandId(), mw));
            module.register(island.getIslandId(), world);
            return world.thenCompose(islandWorld -> {
                island.register(IslandWorld.class, islandWorld);
                return islandWorld.getWorldHandler().init();
            });
        }else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        if(island.isLocal()) {
            CompletableFuture<ManyWorld> worldFuture = service.createNewWorld(defaultWorld, toInform(island));
            CompletableFuture<IslandWorld> world = worldFuture.thenApply(mw -> module.create(island.getIslandId(), mw));
            module.register(island.getIslandId(), world);
            return world.thenCompose(islandWorld -> {
                island.register(IslandWorld.class, islandWorld);
                return islandWorld.getWorldHandler().init();
            });
        }else{
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> onUnload(IslandContext island) {
        if(island.isLocal()) {
            IslandManyWorld imw = (IslandManyWorld) (module.get(island.getIslandId()));
            module.invalidate(island.getIslandId());
            return imw.getManyWorld().unload().thenRun(() -> {});
        }else{
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletableFuture<Void> onDelete(IslandContext island) {
        if(island.isLocal()) {
            module.invalidate(island.getIslandId());
            return service.unload(toInform(island)).thenRun(() -> {});
        }else{
            return CompletableFuture.completedFuture(null);
        }
    }

    private WorldInform toInform(IslandContext island){
        return new WorldInform(category, "island_"+island.getIslandId());
    }
}
