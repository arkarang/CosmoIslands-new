package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandProtection;
import kr.cosmoisland.cosmoislands.api.warp.IslandLocation;
import kr.cosmoisland.cosmoislands.api.warp.TeleportExecutor;
import kr.cosmoisland.cosmoislands.api.warp.WarpResult;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import kr.cosmoislands.cosmoteleport.entity.TeleportLocation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CosmoTeleportExecutor implements TeleportExecutor {

    private final IslandRegistry registry;
    private final IslandPlayerRegistry playerRegistry;
    private final CosmoTeleport teleportService;

    @Override
    public CompletableFuture<WarpResult> teleportPlayer(UUID uuid, IslandLocation location) {
        Island island = this.registry.getIsland(location.getIslandID());
        val isPrivateFuture = island.getComponent(IslandProtection.class).isPrivate();
        val rankFuture = island.getComponent(IslandPlayersMap.class).getRank(playerRegistry.get(uuid));
        return island.getLocated().thenCompose(islandServer -> {
            String serverName = islandServer.getName();
            if(serverName != null) {
                return isPrivateFuture.thenCombine(rankFuture, (isPrivate, rank) -> {
                    WarpResult result;
                    if(isPrivate){
                        if(rank.getPriority() < MemberRank.INTERN.getPriority()){
                            return new WarpResult(false, new IllegalStateException("island is private"));
                        }
                    }
                    teleportService.teleportLocation(uuid, serverName, toTeleportLocation(location));
                    result = new WarpResult(true, null);
                    return result;
                });
            }else{
                return CompletableFuture.completedFuture(new WarpResult(false, new IllegalStateException("island is not loaded")));
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
