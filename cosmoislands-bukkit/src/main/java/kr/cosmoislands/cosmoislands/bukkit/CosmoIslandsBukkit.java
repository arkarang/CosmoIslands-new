package kr.cosmoislands.cosmoislands.bukkit;

import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class CosmoIslandsBukkit extends JavaPlugin {

    @Getter
    static CosmoIslands cosmoIslands;

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
