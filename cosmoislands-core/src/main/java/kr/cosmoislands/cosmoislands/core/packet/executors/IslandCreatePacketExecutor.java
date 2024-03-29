package kr.cosmoislands.cosmoislands.core.packet.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoislands.cosmoislands.core.packet.IslandCreatePacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IslandCreatePacketExecutor implements HelloExecutor<IslandCreatePacket> {

    @Override
    public String getIdentifier() {
        return IslandCreatePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandCreatePacket packet) {
        //do nothing.
    }
}
