package kr.cosmoislands.cosmoislands.world.minecraft;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.world.WorldOperationRegistry;
import kr.cosmoislands.cosmoislands.settings.IslandSettingsModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;

@RequiredArgsConstructor
public class MinecraftWorldHandlerBuilder {

    private final Server minecraftServer;
    private final BukkitExecutor executor;

    public MinecraftWorldHandler build(int islandId, WorldOperationRegistry operationRegistry, IslandSettingsModule module){
        IslandSettingsMap settingsMap = module.get(islandId);
        return new MinecraftWorldHandler(islandId, settingsMap, minecraftServer, operationRegistry);
    }
}
