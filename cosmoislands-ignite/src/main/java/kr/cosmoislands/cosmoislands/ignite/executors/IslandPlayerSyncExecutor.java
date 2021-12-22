package kr.cosmoislands.cosmoislands.ignite.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoislands.cosmoislands.ignite.CosmoIslandsBungee;
import kr.cosmoisland.cosmoislands.core.packet.IslandPlayerSyncPacket;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class IslandPlayerSyncExecutor implements HelloExecutor<IslandPlayerSyncPacket> {

    final CosmoIslandsBungee plugin;

    @Override
    public String getIdentifier() {
        return IslandPlayerSyncPacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandPlayerSyncPacket packet) {
        plugin.syncPlayer(packet.getList());
    }
}
