package kr.cosmoislands.cosmoislands.bungee;

import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.core.api.ChatPlayer;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.IslandStatus;
import kr.cosmoislands.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.chat.CosmoIslandChat;
import kr.cosmoislands.cosmoislands.chat.IslandChatModule;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class Listener implements net.md_5.bungee.api.plugin.Listener {

    private final CosmoIslands cosmoIslands;
    private final CosmoChat cosmoChat;
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
        IslandPlayer islandPlayer = cosmoIslands.getPlayerRegistry().get(uuid);
        cosmoIslands.getPlayerRegistry().unload(uuid);
        islandPlayer.getIslandId().thenCompose(islandId->{
            if (islandId != Island.NIL_ID) {
                val future1 = removeIslandChatChannel(islandId, islandPlayer.getUniqueId());
                return CompletableFuture.allOf(future1);
            }else
                return CompletableFuture.completedFuture(null);
        });
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
                    val future1 = addIslandChatChannel(id, ip.getUniqueId());
                    val future2 = sendLoadRequest(id);
                    return CompletableFuture.allOf(future1, future2);
                }else
                    return CompletableFuture.completedFuture(null);
            });
        }
    }

    private CompletableFuture<Void> sendLoadRequest(int islandId){
        return cosmoIslands.getCloud().getStatus(islandId).thenCompose(status->{
            DebugLogger.log("id: "+islandId+", status: "+status.name());
            if(status == IslandStatus.OFFLINE){
                return cosmoIslands.getCloud().getLeastLoadedServer(maxIslands).thenAccept(server->{
                    DebugLogger.log("onConnect: 3: "+server.getName()+", "+server.getType().name());
                    val future = server.load(islandId);
                    DebugLogger.timeout("onConnect timeout", future, 30000L);
                    DebugLogger.handle("onConnect: 4", future);
                });
            }else{
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    private CompletableFuture<Void> addIslandChatChannel(int islandId, UUID uuid){
        final IslandChatModule module = (IslandChatModule) cosmoIslands.getModule(IslandChat.class);
        final ChatPlayer chatPlayer = cosmoChat.getChatPlayerRegistry().getPlayer(uuid);
        try {
            return module.getPrivateChatRegistry().get(islandId).thenAccept(chat -> chatPlayer.addListening(chat.getChannel()));
        }catch (ExecutionException ignored){
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> removeIslandChatChannel(int islandId, UUID uuid){
        final IslandChatModule module = (IslandChatModule) cosmoIslands.getModule(IslandChat.class);
        final ChatPlayer chatPlayer = cosmoChat.getChatPlayerRegistry().getPlayer(uuid);
        try {
            return module.getPrivateChatRegistry().get(islandId).thenAccept(chat -> chatPlayer.removeListening(chat.getChannel()));
        }catch (ExecutionException ignored){
            return CompletableFuture.completedFuture(null);
        }
    }

}
