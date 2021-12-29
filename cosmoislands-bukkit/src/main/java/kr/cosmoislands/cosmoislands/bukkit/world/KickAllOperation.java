package kr.cosmoislands.cosmoislands.bukkit.world;

import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.world.WorldOperation;
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
public class KickAllOperation implements WorldOperation<MinecraftWorldHandler> {

    private final CosmoTeleport cosmoTeleport;

    @Override
    public CompletableFuture<Boolean> execute(MinecraftWorldHandler handler, IslandSettingsMap settingsMap) {
        DebugLogger.log("execute kickall Operation start");
        Optional<World> optional = handler.world();
        if(optional.isPresent()){
            DebugLogger.log("execute kickall :1");
            World world = optional.get();
            List<CompletableFuture<?>> list = new ArrayList<>();
            val spawnFuture = cosmoTeleport.getWarpService().getSpawnLocation();
            for (Player player : world.getPlayers()) {
                DebugLogger.log("execute kickall :2 "+player.getName());
                val future = spawnFuture.thenAccept(spawn->{
                    cosmoTeleport.warpPlayer(player.getUniqueId(), spawn);
                });
                list.add(future);
            }
            return CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).thenApply(ignored->true);
        }else{
            DebugLogger.log("execute kickall :3 ");
            return CompletableFuture.completedFuture(false);
        }
    }

}
