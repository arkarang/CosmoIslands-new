package kr.cosmoislands.cosmoislands.bukkit.world;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.world.WorldOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MinecraftChangeWeatherOperation implements WorldOperation<MinecraftWorldHandler> {

    @Getter
    private final BukkitExecutor executor;

    @Override
    public CompletableFuture<Boolean> execute(MinecraftWorldHandler handler, IslandSettingsMap settingsMap) {
        Optional<World> optional = handler.world();
        return settingsMap.getSettingAsync(IslandSetting.SUNNY).thenApply(value->{
            try{
                boolean sunny = Boolean.parseBoolean(value);
                if(optional.isPresent()){
                    World world = optional.get();
                    executor.sync(()->world.setStorm(!sunny));
                }
                return optional.isPresent();
            }catch (IllegalArgumentException e){
                return false;
            }
        });
    }
}
