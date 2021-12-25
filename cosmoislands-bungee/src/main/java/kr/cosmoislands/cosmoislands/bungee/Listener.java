package kr.cosmoislands.cosmoislands.bungee;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    public void onPlayerLeft(PlayerDisconnectEvent event){
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        cosmoIslands.getPlayerRegistry().unload(uuid);
    }

    @EventHandler
    public void onConnect(ServerConnectedEvent event){
        ProxiedPlayer player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(markProxyJoin.contains(uuid)) {
            DebugLogger.log("onConnect: 1");
            IslandPlayer ip = cosmoIslands.getPlayerRegistry().get(uuid);
            val updateFuture = cosmoIslands.getPlayerRegistry().update(uuid);
            ip.getIslandId().thenCombine(updateFuture, (id, ignored)->{
                DebugLogger.log("onConnect: 2: "+id);
                if (id != Island.NIL_ID) {
                    return cosmoIslands.getCloud().getLeastLoadedServer(maxIslands).thenAccept(server->{
                        DebugLogger.log("onConnect: 3: "+server.getName()+", "+server.getType().name());
                        val future = server.load(id);
                        DebugLogger.timeout("onConnect timeout", future, 30000L);
                        DebugLogger.handle("onConnect: 4", future);
                    });
                }else
                    return CompletableFuture.completedFuture(null);
            }).thenCompose(future->future);
        }
    }

}
