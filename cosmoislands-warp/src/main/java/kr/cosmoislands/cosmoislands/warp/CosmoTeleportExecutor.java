package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.warp.IslandLocation;
import kr.cosmoisland.cosmoislands.api.warp.TeleportExecutor;
import kr.cosmoisland.cosmoislands.api.warp.WarpResult;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import kr.cosmoislands.cosmoteleport.entity.TeleportLocation;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CosmoTeleportExecutor implements TeleportExecutor {

    private final IslandRegistry registry;
    private final CosmoTeleport teleportService;

    @Override
    public CompletableFuture<WarpResult> teleportPlayer(UUID uuid, IslandLocation location) {
        Island island = this.registry.getIsland(location.getIslandID());
        return island.getLocated().thenApply(islandServer -> {
            String serverName = islandServer.getName();
            if(serverName != null) {
                teleportService.teleportLocation(uuid, serverName, toTeleportLocation(location));
                return new WarpResult(true, null);
            }else{
                return new WarpResult(false, new IllegalStateException("island is not loaded"));
            }
        });
    }

    private TeleportLocation toTeleportLocation(IslandLocation location){
        return new TeleportLocation(
                "island_"+location.getIslandID(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }
}
