package kr.cosmoisland.cosmoislands.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ServerRegistration {

    CompletableFuture<Void> registerServer(String serverName, IslandServer.Type type);

    CompletableFuture<IslandServer.Type> getType(String serverName);

    CompletableFuture<Map<String, IslandServer.Type>> getRegisteredServers();

    CompletableFuture<List<String>> getRegisteredServers(IslandServer.Type type);

}
