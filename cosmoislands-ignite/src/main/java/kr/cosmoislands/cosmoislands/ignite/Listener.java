package kr.cosmoislands.cosmoislands.ignite;

import kr.cosmoisland.cosmoislands.api.Island;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Listener implements Listener {

    Database database;

    public Listener(Database database) {
        this.database = database;
    }

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
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            if(markProxyJoin.contains(uuid)) {
                ServerInfo info = event.getServer().getInfo();
                IslandPlayer ip = database.getLoader(IslandPlayerLoader.class).get(uuid).get();
                if (ip != null && ip.getIslandId() != Island.NIL_ID) {
                    boolean success = CosmoIslandsBungee.getInst().loadIsland(ip.getIslandId()).get();
                }
                markProxyJoin.remove(uuid);
            }
        }catch (ExecutionException | InterruptedException e){
            e.printStackTrace();
        }
    }

}
