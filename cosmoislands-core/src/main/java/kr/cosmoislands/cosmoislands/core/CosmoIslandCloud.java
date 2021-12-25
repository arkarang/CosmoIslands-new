package kr.cosmoislands.cosmoislands.core;

import com.google.common.collect.ImmutableList;
import com.minepalm.hellobungee.api.HelloEveryone;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.*;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CosmoIslandCloud implements IslandCloud {

    private static final String islandLocationsKey = "cosmoislands:island_locations";
    private static final String onlineKey = "cosmoislands:online_servers";

    @Getter
    final ServerRegistration serverRegistration;
    final HelloEveryone networkModule;
    final IslandService service;
    final IslandHostServer localServer;
    final RedisAsyncCommands<String, String> async;
    final Logger logger;
    @Getter
    final RedisIslandStatusRegistry statusRegistry;

    final ConcurrentHashMap<String, IslandServer> servers = new ConcurrentHashMap<>();

    CosmoIslandCloud(HelloEveryone networkModule,
                     IslandService service,
                     Database database,
                     RedisAsyncCommands<String, String> async,
                     Logger logger) throws ExecutionException, InterruptedException {
        this.networkModule = networkModule;
        this.service = service;
        this.async = async;
        this.logger = logger;
        MySQLServerRegistrationDataModel dataModel = new MySQLServerRegistrationDataModel("cosmoislands_island_servers", database);
        dataModel.init();
        this.serverRegistration = new MySQLServerRegistration(dataModel);
        this.statusRegistry = new RedisIslandStatusRegistry(async);
        Map<String, IslandServer.Type> registeredServers = this.serverRegistration.getRegisteredServers().get();
        this.localServer = new RedisIslandHostServer(networkModule.getName(),
                registeredServers.get(networkModule.getName()),
                islandLocationsKey,
                service.getRegistry(),
                this,
                networkModule.sender(networkModule.getName()), async);
        for (String serverName : registeredServers.keySet()) {
            IslandServer.Type type = registeredServers.get(serverName);
            if(this.networkModule.getConnections().getClient(serverName) != null){
                IslandServer islandServer = new RedisIslandServer(serverName, type, islandLocationsKey, service.getRegistry(), this, networkModule.sender(serverName), async);
                this.servers.put(serverName, islandServer);
            }else{
                logger.warning("island server '"+serverName+"' is not found on HelloBungee settings.");
            }
        }
    }

    @Override
    public CompletableFuture<List<IslandServer>> getIslandServers() {
        return CompletableFuture.completedFuture(ImmutableList.copyOf(servers.values()));
    }

    @Override
    public IslandServer getServer(String name) {
        if(name == null) {
            return null;
        }else{
            return servers.get(name);
        }
    }

    @Override
    public IslandHostServer getHostServer() {
        return localServer;
    }

    @Override
    public CompletableFuture<IslandStatus> getStatus(int islandId) {
        return statusRegistry.getStatus(islandId);
    }

    @Override
    public CompletableFuture<Void> setStatus(int islandId, IslandStatus status) {
        return statusRegistry.setStatus(islandId, status);
    }

    @Override
    public CompletableFuture<IslandServer> getLocated(int islandId) {
        return async.hget(islandLocationsKey, islandId+"")
                .thenApply(server-> server == null ? null : getServer(server)).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> updateOnline(String serverName, boolean online) {
        if(online){
            return onlineServer(serverName);
        }else{
            return offlineServer(serverName);
        }
    }

    @Override
    public CompletableFuture<Boolean> isOnline(String serverName) {
        return async.hexists(onlineKey, serverName)
                .thenApply(Objects::nonNull)
                .toCompletableFuture();
    }

    private CompletableFuture<Void> offlineServer(String serverName){
        return async.hdel(onlineKey, serverName)
                .thenRun(()->{})
                .toCompletableFuture();
    }

    private CompletableFuture<Void> onlineServer(String serverName){
        return serverRegistration.getType(serverName)
                .thenCompose(type-> async.hset(onlineKey, serverName, type.name()))
                .thenRun(()->{});
    }


    public CompletableFuture<List<IslandServer>> getOnlineServers() {
        return async.hkeys(onlineKey).thenApply(list->{
            return list.stream().map(servers::get).collect(Collectors.toList());
        }).toCompletableFuture();
    }

    public CompletableFuture<List<IslandServer>> getOnlineServers(IslandServer.Type type) {
        return getOnlineServers().thenApply(servers->{
            return servers.stream().filter(server-> server.getType().equals(type)).collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<IslandServer> getLeastLoadedServer(int maximumLoaded){
        DebugLogger.log("islandcloud: let found least loaded server");
        Map<String, Integer> counts = new HashMap<>();
        return serverRegistration.getRegisteredServers(IslandServer.Type.ISLAND).thenCompose(list->{
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (String serverName : list) {
                DebugLogger.log("islandcloud: found online island server: "+list.size());
                IslandServer server = servers.get(serverName);
                if(server != null){
                    CompletableFuture<?> future = server.getLoadedCount().thenAccept(count -> {
                        DebugLogger.log("islandcloud: serverName: "+serverName+", count: "+count);
                        counts.put(serverName, count);
                    });
                    futures.add(future);
                }
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(ignored->{
                String serverName = null;
                int min = maximumLoaded;
                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    if(entry.getValue() < min){
                        min = entry.getValue();
                        serverName = entry.getKey();
                    }
                }
                DebugLogger.log("islandcloud: least server: "+serverName);
                return this.getServer(serverName);
            });
        });
    }
}
