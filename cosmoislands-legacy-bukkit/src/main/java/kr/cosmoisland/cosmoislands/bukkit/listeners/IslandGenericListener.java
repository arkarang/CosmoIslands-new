package kr.cosmoisland.cosmoislands.bukkit.listeners;

import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.core.AdminLoader;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.ExecutionException;

public class IslandGenericListener implements org.bukkit.event.Listener {

    final AdminLoader adminLoader;

    public IslandGenericListener(){
        adminLoader = CosmoIslandsBukkitBootstrap.getInst().getDatabase().getLoader(AdminLoader.class);
    }

    @EventHandler
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event){
        if(event.isAsynchronous()) {
            CosmoIslandsBukkitBootstrap.getInst().getPlayersCache().update(event.getUniqueId());
        }else{
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                CosmoIslandsBukkitBootstrap.getInst().getPlayersCache().update(event.getUniqueId());
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
            try {
                IslandPlayer player = CosmoIslandsBukkitBootstrap.getInst().getPlayersCache().get(event.getPlayer().getUniqueId());
                CosmoIslandsBukkitBootstrap.getInst().getIslandGC().tryUnload(player.getIslandId());
            } catch (ExecutionException e) {

            }
            CosmoIslandsBukkitBootstrap.getInst().getPlayersCache().remove(event.getPlayer().getUniqueId());
        });
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent event){
        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()-> CosmoIslandsBukkitBootstrap.getInst().getPlayersCache().update(event.getPlayer().getUniqueId()));
    }
}
