package kr.cosmoisland.cosmoislands.core.packet.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoisland.cosmoislands.core.packet.IslandUpdatePacket;

public class IslandLoadPacketExecutor implements HelloExecutor<IslandUpdatePacket> {
    @Override
    public String getIdentifier() {
        return IslandUpdatePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandUpdatePacket packet) {
        //do nothing.
    }
}
