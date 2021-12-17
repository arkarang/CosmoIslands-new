package kr.cosmoisland.cosmoislands.bukkit.hellobungee;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.core.packet.IslandDeletePacket;

public class IslandDeleteExecutor implements HelloExecutor<IslandDeletePacket> {

    @Override
    public String getIdentifier() {
        return IslandDeletePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandDeletePacket packet) {
        CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().deleteIsland(packet.getId());
    }
}
