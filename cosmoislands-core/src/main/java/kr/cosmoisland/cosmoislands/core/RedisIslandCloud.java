package kr.cosmoisland.cosmoislands.core;

import com.minepalm.hellobungee.api.HelloEveryone;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.IslandCloud;
import kr.cosmoisland.cosmoislands.api.IslandServer;
import kr.cosmoisland.cosmoislands.api.IslandService;

import java.util.concurrent.CompletableFuture;

public class RedisIslandCloud implements IslandCloud {

    private static final String islandKey = "cosmoislands:island_locations";

    final HelloEveryone networkModule;
    final IslandService service;
    final IslandServer localServer;
    final RedisAsyncCommands<String, String> async;

    RedisIslandCloud(HelloEveryone networkModule, IslandService service, RedisAsyncCommands<String, String> async){
        this.networkModule = networkModule;
        this.service = service;
        this.async = async;
        this.localServer = new RedisIslandServer(networkModule.getName(), islandKey, networkModule.sender(networkModule.getName()), async);
    }

    @Override
    public IslandServer getServer(String name) {
        if(this.networkModule.getConnections().getClient(name) == null){
            return null;
        }else {
            //todo: 인스턴스 매번 새로 만들지 말기
            return new RedisIslandServer(name, islandKey, networkModule.sender(name), async);
        }
    }

    @Override
    public IslandServer getLocalServer() {
        return localServer;
    }

    @Override
    public CompletableFuture<IslandServer> getLocated(int islandId) {
        return async.hget(islandKey, islandId+"").thenApply(server-> server == null ? null : getServer(server)).toCompletableFuture();
    }
}
