package kr.cosmoislands.cosmoislands.ignite;

import kr.cosmoisland.cosmoislands.api.IslandCloud;
import kr.cosmoisland.cosmoislands.api.IslandServer;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CosmoIslandIgniter {

    private final Hashtable<String, Boolean> storageServers = new Hashtable<>();
    private final int maxIslands;
    private final IslandCloud cloud;

    public CosmoIslandIgniter(int maxIslands, IslandCloud cloud, List<String> serverList){
        this.maxIslands = maxIslands;
        this.cloud = cloud;
        serverList.forEach(server->storageServers.put(server, true));
    }

    CompletableFuture<IslandServer> getAtLeast(){
        Map<String, Integer> counts = new HashMap<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (String serverName : storageServers.keySet()) {
            IslandServer server = cloud.getServer(serverName);
            if(server != null){
                CompletableFuture<?> future = server.getLoadedCount().thenAccept(count -> {
                    counts.put(serverName, count);
                });
                futures.add(future);
            }
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(ignored->{
            String serverName = null;
            int min = maxIslands;
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if(entry.getValue() < min){
                    min = entry.getValue();
                    serverName = entry.getKey();
                }
            }
            return cloud.getServer(serverName);
        });
    }
}
