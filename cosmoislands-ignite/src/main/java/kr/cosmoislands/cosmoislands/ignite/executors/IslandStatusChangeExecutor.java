package kr.cosmoislands.cosmoislands.ignite.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoislands.cosmoislands.ignite.CosmoIslandsBungee;
import kr.cosmoisland.cosmoislands.core.packet.IslandStatusChangePacket;

public class IslandStatusChangeExecutor implements HelloExecutor<IslandStatusChangePacket> {
    @Override
    public String getIdentifier() {
        return IslandStatusChangePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandStatusChangePacket packet) {
        if(packet.getDestination().equals(CosmoIslandsBungee.getInst().getName())){
            if(packet.isLoad()) {
                CosmoIslandsBungee.getInst().loadIsland(packet.getIslandID());
            }else {
                CosmoIslandsBungee.getInst().unloadIsland(packet.getIslandID());
            }
        }else {
            if (packet.isLoad()) {
                CosmoIslandsBungee.getInst().sendData(packet.getDestination(), packet);
            } else {
                CosmoIslandsBungee.getInst().sendData(packet.getDestination(), packet);
            }
        }
    }
}
