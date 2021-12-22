package kr.cosmoisland.cosmoislands.core.packet.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoisland.cosmoislands.core.packet.IslandDeletePacket;

public class IslandDeletePacketExecutor implements HelloExecutor<IslandDeletePacket> {

    @Override
    public String getIdentifier() {
        return IslandDeletePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandDeletePacket packet) {
        //do nothing.
    }
}
