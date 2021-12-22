package kr.cosmoislands.cosmoislands.ignite.executors;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoislands.cosmoislands.ignite.CosmoIslandsBungee;
import kr.cosmoisland.cosmoislands.core.packet.IslandCreatePacket;
import net.md_5.bungee.api.ProxyServer;

public class IslandCreateExecutor implements HelloExecutor<IslandCreatePacket> {
    @Override
    public String getIdentifier() {
        return IslandCreatePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(IslandCreatePacket packet) {
        //todo: remove this after testing.
        ProxyServer.getInstance().getLogger().info("Bungee IslandCreatePacket: destination: " + packet.getDestination() + " uuid: " + packet.getUuid());
        if(packet.getDestination().equals(CosmoIslandsBungee.getInst().getName())){
            CosmoIslandsBungee.getInst().createIsland(packet.getUuid());
        }else
            CosmoIslandsBungee.getInst().sendData(packet.getDestination(), packet);
    }
}
