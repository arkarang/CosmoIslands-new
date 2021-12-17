package kr.cosmoisland.cosmoislands.core;

import com.minepalm.hellobungee.api.HelloSender;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.IslandServer;
import kr.cosmoisland.cosmoislands.core.packet.IslandCreatePacket;
import kr.cosmoisland.cosmoislands.core.packet.IslandDeletePacket;
import kr.cosmoisland.cosmoislands.core.packet.IslandStatusChangePacket;
import kr.cosmoisland.cosmoislands.core.packet.IslandSyncPacket;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RedisIslandServer implements IslandServer {

    @Getter
    final String name;
    final String islandKey;
    final HelloSender sender;
    final RedisAsyncCommands<String, String> async;
    final String redisKey;

    RedisIslandServer(String name, String islandKey, HelloSender sender, RedisAsyncCommands<String, String> async){
        this.name = name;
        this.islandKey = islandKey;
        this.sender = sender;
        this.async = async;
        this.redisKey = "cosmoislands:server:"+name;
    }

    @Override
    public CompletableFuture<Boolean> registerIsland(Island island, long uptime) {
        return async.hexists(islandKey, island.getId()+"").thenCompose(exist->{
            if(exist) {
                CompletableFuture<Long> future = async.sadd(redisKey, island.getId()+"").toCompletableFuture();
                return async.hset(islandKey, island.getId() + "", name).thenCombine(future, (l, str) -> true);
            }else
                return CompletableFuture.completedFuture(false);
        }).toCompletableFuture();

    }

    @Override
    public CompletableFuture<Boolean> unregisterIsland(Island island) {
        return async.hexists(islandKey, island.getId()+"").thenCompose(exist->{
            if(exist) {
                CompletableFuture<Long> future = async.sadd(redisKey, island.getId()+"").toCompletableFuture();
                return async.hdel(islandKey, island.getId() + "").thenCombine(future, (l, str) -> true);
            }else
                return CompletableFuture.completedFuture(false);
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Integer> getLoadedCount() {
        return async.hkeys(redisKey).thenApply(List::size).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Island> create(UUID uuid) {
        return sender.callback(new IslandCreatePacket(this.name, uuid), Island.class).async();
    }

    @Override
    public CompletableFuture<Island> load(int islandId) {
        return sender.callback(new IslandStatusChangePacket(this.name, islandId, true), Island.class).async();
    }

    @Override
    public CompletableFuture<Boolean> unload(int islandId) {
        return sender.callback(new IslandStatusChangePacket(this.name, islandId, false), Island.class).async().thenApply(Objects::nonNull);
    }

    @Override
    public CompletableFuture<Boolean> delete(int islandId) {
        return sender.callback(new IslandDeletePacket(this.name, islandId), Boolean.class).async();
    }

    @Override
    public <T extends IslandComponent> CompletableFuture<Boolean> sync(int islandId, Class<T> component) {
        return sender.callback(new IslandSyncPacket(islandId, /* todo: resolving component id*/ (byte)0), Boolean.class).async();
    }

    @Override
    public CompletableFuture<Boolean> syncIsland(int islandId) {
        return sender.callback(new IslandSyncPacket(islandId, /* todo: resolving component id*/ (byte)0), Boolean.class).async();
    }
}
