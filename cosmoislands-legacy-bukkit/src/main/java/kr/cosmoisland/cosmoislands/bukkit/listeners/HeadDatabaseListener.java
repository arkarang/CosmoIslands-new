package kr.cosmoisland.cosmoislands.bukkit.listeners;

import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.bukkit.config.YamlIslandConfig;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HeadDatabaseListener implements Listener {

    @EventHandler
    public void onHDBLoad(DatabaseLoadEvent event){
        YamlIslandConfig.setAPI(new HeadDatabaseAPI());
        CosmoIslandsBukkitBootstrap.getInst().getYamlIslandConfig().setHeads();
    }

}
