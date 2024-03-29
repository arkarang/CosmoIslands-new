package kr.cosmoislands.cosmoislands.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IslandCloud {

    ServerRegistration getServerRegistration();

    IslandStatusRegistry getStatusRegistry();

    CompletableFuture<List<IslandServer>> getIslandServers();

    IslandServer getServer(String name);

    IslandHostServer getHostServer();

    //todo: IslandRegistry로 옮기기
    CompletableFuture<IslandStatus> getStatus(int islandId);

    //todo: IslandRegistry로 옮기기
    CompletableFuture<Void> setStatus(int islandId, IslandStatus status);

    CompletableFuture<IslandServer> getLocated(int islandId);

    CompletableFuture<Void> updateOnline(String serverName, boolean online);

    CompletableFuture<Boolean> isOnline(String serverName);

    CompletableFuture<List<IslandServer>> getOnlineServers();

    CompletableFuture<List<IslandServer>> getOnlineServers(IslandServer.Type type);

    CompletableFuture<IslandServer> getLeastLoadedServer(int maximumLoaded);
}
