package kr.cosmoislands.cosmoislands.bukkit.protection;

import kr.cosmoislands.cosmoislands.api.protection.IslandProtection;
import kr.cosmoislands.cosmoislands.bukkit.IslandPreconditions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class CacheUpdateListener implements Listener {

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event){
        IslandPreconditions preconditions;
        try{
            preconditions = IslandPreconditions.of(event.getPlayer().getWorld());
            preconditions.getIsland().getComponent(IslandProtection.class).update(event.getPlayer().getUniqueId());
        }catch (IllegalArgumentException ignored){

        }
    }
}
