package kr.cosmoislands.cosmoislands.world.minecraft;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.world.WorldOperationRegistry;

public class MinecraftWorldHandlerInitializer {

    public static void init(WorldOperationRegistry registry, BukkitExecutor executor){
        registry.registerOperation(MinecraftWorldHandler.class, "set-weather", new MinecraftChangeWeatherOperation(executor));
        registry.registerOperation(MinecraftWorldHandler.class, "set-time", new MinecraftSetTimeOperation(executor));
        registry.registerOperation(MinecraftWorldHandler.class, "set-gamerule", new MinecraftDefaultGameruleOperation(executor));
    }
}
