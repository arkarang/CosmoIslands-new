package kr.cosmoisland.cosmoislands.bukkit.hellobungee;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.core.packet.IslandCreatePacket;
import org.bukkit.Bukkit;

public class IslandCreateExecutor implements HelloExecutor<IslandCreatePacket> {

    @Override
    public String getIdentifier() {
        return IslandCreatePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandCreatePacket packet) {
        Bukkit.getLogger().info("bukkit: 0");
        if(packet.getUuid() != null)
            CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().createIsland(packet.getUuid());
    }

}
