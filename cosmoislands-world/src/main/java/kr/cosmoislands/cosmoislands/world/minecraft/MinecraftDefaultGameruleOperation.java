package kr.cosmoislands.cosmoislands.world.minecraft;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.world.WorldOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MinecraftDefaultGameruleOperation implements WorldOperation<MinecraftWorldHandler> {

    @Getter
    private final BukkitExecutor executor;

    @Override
    public CompletableFuture<Boolean> execute(MinecraftWorldHandler handler, IslandSettingsMap settingsMap) {
        Optional<World> optional = handler.world();
        if(optional.isPresent()){
            World world = optional.get();
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
        }
        return CompletableFuture.completedFuture(optional.isPresent());
    }
}
