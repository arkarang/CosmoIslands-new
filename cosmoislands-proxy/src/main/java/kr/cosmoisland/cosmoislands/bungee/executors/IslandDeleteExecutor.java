package kr.cosmoisland.cosmoislands.bungee.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoisland.cosmoislands.bungee.CosmoIslandsBungee;
import kr.cosmoisland.cosmoislands.core.packet.IslandDeletePacket;

public class IslandDeleteExecutor implements HelloExecutor<IslandDeletePacket> {
    @Override
    public String getIdentifier() {
        return IslandDeletePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandDeletePacket packet) {
        if(!packet.getDestination().equals(CosmoIslandsBungee.getInst().getName()))
            CosmoIslandsBungee.getInst().sendData(packet.getDestination(), packet);
        //todo: invalidate cached island users.
    }
}
