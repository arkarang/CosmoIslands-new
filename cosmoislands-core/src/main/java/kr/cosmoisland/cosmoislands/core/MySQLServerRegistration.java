package kr.cosmoisland.cosmoislands.core;

import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.IslandServer;
import kr.cosmoisland.cosmoislands.api.ServerRegistration;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLServerRegistration implements ServerRegistration {

    final MySQLServerRegistrationDataModel dataModel;

    @Override
    public CompletableFuture<Void> registerServer(String serverName, IslandServer.Type type) {
        return dataModel.insert(serverName, type);
    }

    @Override
    public CompletableFuture<IslandServer.Type> getType(String serverName) {
        return dataModel.selectType(serverName);
    }

    @Override
    public CompletableFuture<Map<String, IslandServer.Type>> getRegisteredServers() {
        return dataModel.all();
    }

    @Override
    public CompletableFuture<List<String>> getRegisteredServers(IslandServer.Type type) {
        return dataModel.selectServers(type);
    }
}
