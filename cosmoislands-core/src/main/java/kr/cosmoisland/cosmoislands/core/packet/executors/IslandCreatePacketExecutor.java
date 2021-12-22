package kr.cosmoisland.cosmoislands.core.packet.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoisland.cosmoislands.core.packet.IslandCreatePacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IslandCreatePacketExecutor implements HelloExecutor<IslandCreatePacket> {

    private final CosmoIslands service;

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public void executeReceived(IslandCreatePacket islandCreatePacket) {

    }
}
