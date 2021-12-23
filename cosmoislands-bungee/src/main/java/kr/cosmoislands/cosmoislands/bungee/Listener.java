package kr.cosmoislands.cosmoislands.bungee;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

@RequiredArgsConstructor
public class Listener implements net.md_5.bungee.api.plugin.Listener {

    private final CosmoIslands cosmoIslands;
    private final int maxIslands;
    HashSet<UUID> markProxyJoin = new HashSet<>();

    @EventHandler
    public void onProxyJoin(ServerConnectEvent event){
        ServerConnectEvent.Reason reason = event.getReason();
        if(reason.equals(ServerConnectEvent.Reason.JOIN_PROXY)){
            markProxyJoin.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onConnect(ServerConnectedEvent event){
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(markProxyJoin.contains(uuid)) {
            IslandPlayer ip = cosmoIslands.getPlayerRegistry().get(uuid);
            ip.getIslandId().thenAccept(id->{
                if (id != Island.NIL_ID) {
                    cosmoIslands.getCloud().getLeastLoadedServer(maxIslands).thenAccept(server->{
                        server.load(id);
                    });
                }
            });
        }
    }

}
