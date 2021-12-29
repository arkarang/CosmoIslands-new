package kr.cosmoislands.cosmoislands.bukkit.world;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.api.world.WorldOperationRegistry;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;

public class MinecraftWorldHandlerInitializer {

    public static void init(IslandPlayerRegistry playerRegistry,
                            WorldOperationRegistry registry,
                            CosmoTeleport teleport,
                            BukkitExecutor executor){
        registry.registerType(MinecraftWorldHandler.class);
        registry.registerOperation(MinecraftWorldHandler.class, "set-weather", new MinecraftChangeWeatherOperation(executor));
        registry.registerOperation(MinecraftWorldHandler.class, "set-time", new MinecraftSetTimeOperation(executor));
        registry.registerOperation(MinecraftWorldHandler.class, "set-gamerule", new MinecraftDefaultGameruleOperation(executor));
        registry.registerOperation(MinecraftWorldHandler.class, "kick-all", new KickAllOperation(teleport));
        registry.registerOperation(MinecraftWorldHandler.class, "kick-guest", new KickAllGuestOperation(playerRegistry, teleport));
    }
}
