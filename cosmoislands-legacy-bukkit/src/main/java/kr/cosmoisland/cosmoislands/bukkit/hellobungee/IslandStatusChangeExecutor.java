package kr.cosmoisland.cosmoislands.bukkit.hellobungee;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.core.packet.IslandStatusChangePacket;

public class IslandStatusChangeExecutor implements HelloExecutor<IslandStatusChangePacket> {
    @Override
    public String getIdentifier() {
        return IslandStatusChangePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandStatusChangePacket packet) {
        System.out.println("load unload packet: "+packet.isLoad()+", "+packet.getDestination()+", "+packet.getIslandID());
        if(packet.isLoad()){
            CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().loadIsland(packet.getIslandID());
        }else{
            CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().unloadIsland(packet.getIslandID());
        }
    }
}
