package kr.cosmoislands.cosmoislands.bukkit.world;

import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.world.WorldHandlerFactory;
import kr.cosmoislands.cosmoislands.api.world.WorldOperationRegistry;
import kr.cosmoislands.cosmoislands.settings.IslandSettingsModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;

@RequiredArgsConstructor
public class MinecraftWorldHandlerFactory implements WorldHandlerFactory {

    private final Server minecraftServer;
    private final IslandSettingsModule module;

    @Override
    public MinecraftWorldHandler build(int islandId, WorldOperationRegistry operationRegistry){
        IslandSettingsMap settingsMap = module.get(islandId);
        return new MinecraftWorldHandler(islandId, settingsMap, minecraftServer, operationRegistry);
    }

}
