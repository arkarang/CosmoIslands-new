package kr.cosmoislands.cosmoislands.world.minecraft;

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
public class MinecraftSetTimeOperation implements WorldOperation<MinecraftWorldHandler> {

    @Getter
    private final BukkitExecutor executor;

    @Override
    public CompletableFuture<Boolean> execute(MinecraftWorldHandler handler, IslandSettingsMap settingsMap) {
        Optional<World> optional = handler.world();
        return settingsMap.getSettingAsync(IslandSetting.TIME).thenApply(value->{
            try{
                int time = Integer.parseInt(value);
                if(optional.isPresent()){
                    World world = optional.get();
                    executor.sync(()->world.setTime(time));
                }
                return optional.isPresent();
            }catch (IllegalArgumentException e){
                return false;
            }
        });
    }
}
