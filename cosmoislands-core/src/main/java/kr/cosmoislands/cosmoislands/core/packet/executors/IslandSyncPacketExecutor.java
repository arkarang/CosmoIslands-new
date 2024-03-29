package kr.cosmoislands.cosmoislands.core.packet.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.core.packet.IslandSyncPacket;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class IslandSyncPacketExecutor implements HelloExecutor<IslandSyncPacket> {

    private final IslandRegistry registry;

    @Override
    public String getIdentifier() {
        return IslandSyncPacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandSyncPacket packet) {
        int islandId = packet.getIslandID();
        Class<? extends IslandComponent> clazz = registry.getComponentClass(packet.getComponentID());
        Optional.ofNullable(registry.getLocalIsland(islandId)).ifPresent(island->island.getComponent(clazz).sync());
    }
}
