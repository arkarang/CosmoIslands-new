package kr.cosmoislands.cosmoislands.bukkit.world;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMapModule;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.world.WorldOperation;
import kr.cosmoislands.cosmoislands.bukkit.IslandPreconditions;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class KickAllGuestOperation implements WorldOperation<MinecraftWorldHandler> {

    private final IslandPlayerRegistry playerRegistry;
    private final CosmoTeleport cosmoTeleport;

    @Override
    public CompletableFuture<Boolean> execute(MinecraftWorldHandler handler, IslandSettingsMap settingsMap) {
        Optional<World> optional = handler.world();
        DebugLogger.log("execute kickall guest :1 ");
        if(optional.isPresent()){
            World world = optional.get();
            try {
                IslandPreconditions preconditions = IslandPreconditions.of(world);
                Island island = preconditions.getIsland();

                IslandPermissionsMap permissionsMap = island.getComponent(IslandPermissionsMap.class);
                IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                val spawnFuture = cosmoTeleport.getWarpService().getSpawnLocation();

                List<CompletableFuture<?>> list = new ArrayList<>();

                for (Player player : world.getPlayers()) {
                    DebugLogger.log("execute kickall guest :2 "+player.getName());
                    IslandPlayer ip = playerRegistry.get(player.getUniqueId());

                    val future = playersMap.getRank(ip).thenCompose(memberRank -> {
                        DebugLogger.log("execute kickall guest :2 ");
                        return permissionsMap.hasPermission(IslandPermissions.IGNORE_LOCK, memberRank);
                    }).thenCompose(hasPermission -> {
                        DebugLogger.log("execute kickall guest :3 "+(hasPermission == null));
                        if(!hasPermission && !player.isOp()){
                            DebugLogger.log("execute kickall guest true :4 "+player.getName());
                            return spawnFuture.thenAccept(spawn -> {
                                cosmoTeleport.warpPlayer(player.getUniqueId(), spawn);
                            });
                        }else{
                            DebugLogger.log("execute kickall guest false :5 "+player.getName());
                            return CompletableFuture.completedFuture(null);
                        }
                    });
                    DebugLogger.handle("kickall guest", future);
                    list.add(future);
                }

                return CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).thenApply(ignored -> true);
            }catch (IllegalArgumentException ignored){

            }
        }

        return CompletableFuture.completedFuture(false);

    }
}
