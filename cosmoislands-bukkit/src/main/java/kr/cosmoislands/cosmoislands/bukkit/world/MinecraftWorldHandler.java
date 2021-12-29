package kr.cosmoislands.cosmoislands.bukkit.world;

import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.world.IslandWorldHandler;
import kr.cosmoislands.cosmoislands.api.world.WorldOperation;
import kr.cosmoislands.cosmoislands.api.world.WorldOperationRegistry;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.val;
import org.bukkit.Server;
import org.bukkit.World;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MinecraftWorldHandler implements IslandWorldHandler {

    private final WorldOperationRegistry operationRegistry;
    private final Server minecraftServer;
    private final int islandId;
    private final String worldName;
    private final IslandSettingsMap settingsMap;

    MinecraftWorldHandler(int islandId,
                          IslandSettingsMap settingsMap,
                          Server minecraftServer,
                          WorldOperationRegistry operationRegistry){
        this.islandId = islandId;
        this.worldName = "island_"+islandId;
        this.settingsMap = settingsMap;
        this.minecraftServer = minecraftServer;
        this.operationRegistry = operationRegistry;
    }

    @Override
    public CompletableFuture<Void> init(){
        val future1 = this.runOperation("set-weather");
        val future2 = this.runOperation("set-time");
        val future3 = this.runOperation("set-gamerule");
        return CompletableFuture.allOf(future1, future2, future3);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<Boolean> runOperation(String key) {
        WorldOperation<?> found = operationRegistry.getOperation(this.getClass(), key);
        try {
            if (found != null) {
                WorldOperation<MinecraftWorldHandler> operation = (WorldOperation<MinecraftWorldHandler>) found;
                return operation.execute(this, settingsMap);
            }
        }catch (Throwable e){
            DebugLogger.error(e);
        }
        return CompletableFuture.completedFuture(false);
    }

    protected Optional<World> world(){
        return Optional.ofNullable(minecraftServer.getWorld(worldName));
    }
}
