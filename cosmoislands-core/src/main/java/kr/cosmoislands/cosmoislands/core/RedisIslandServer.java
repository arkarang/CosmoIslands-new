package kr.cosmoislands.cosmoislands.core;

import com.minepalm.hellobungee.api.HelloSender;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.*;
import kr.cosmoislands.cosmoislands.core.packet.IslandCreatePacket;
import kr.cosmoislands.cosmoislands.core.packet.IslandDeletePacket;
import kr.cosmoislands.cosmoislands.core.packet.IslandSyncPacket;
import kr.cosmoislands.cosmoislands.core.packet.IslandUpdatePacket;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RedisIslandServer implements IslandServer {

    @Getter
    final String name;
    @Getter
    final IslandServer.Type type;
    final String islandLocationKey;
    final HelloSender sender;
    final IslandRegistry registry;
    final IslandCloud cloud;
    final RedisAsyncCommands<String, String> async;
    final String redisKey;

    RedisIslandServer(String name,
                      IslandServer.Type type,
                      String islandKey,
                      IslandRegistry registry,
                      IslandCloud cloud,
                      HelloSender sender,
                      RedisAsyncCommands<String, String> async){
        this.name = name;
        this.type = type;
        this.islandLocationKey = islandKey;
        this.registry = registry;
        this.sender = sender;
        this.async = async;
        this.cloud = cloud;
        this.redisKey = "cosmoislands:server:"+name;
    }

    @Override
    public CompletableFuture<Boolean> isOnline() {
        return cloud.isOnline(this.name);
    }

    @Override
    public CompletableFuture<Boolean> registerIsland(Island island, long uptime) {
        return async.hexists(islandLocationKey, island.getId()+"").thenCompose(exist->{
            if(!exist) {
                CompletableFuture<Long> future = async.sadd(redisKey, island.getId()+"").toCompletableFuture();
                this.registry.registerIsland(island);
                this.cloud.setStatus(island.getId(), IslandStatus.ONLINE);
                return async.hset(islandLocationKey, island.getId() + "", name).thenCombine(future, (l, str) -> true);
            }else
                return CompletableFuture.completedFuture(false);
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Boolean> unregisterIsland(Island island) {
        return async.hexists(islandLocationKey, island.getId()+"").thenCompose(exist->{
            if(exist) {
                CompletableFuture<Long> future = async.srem(redisKey, island.getId()+"").toCompletableFuture();
                this.registry.unregisterIsland(island.getId());
                this.cloud.setStatus(island.getId(), IslandStatus.OFFLINE);
                return async.hdel(islandLocationKey, island.getId() + "").thenCombine(future, (l, str) -> true);
            }else
                return CompletableFuture.completedFuture(false);
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<List<Integer>> getIslands() {
        return async.smembers(redisKey).thenApply(list->list.stream().map(Integer::parseInt).collect(Collectors.toList()))
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Integer> getLoadedCount() {
        return async.smembers(redisKey).thenApply(Set::size).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Island> create(UUID uuid) {
        return OperationPrecondition.canCreate(uuid).thenCompose(canCreate->{
            if(canCreate){
                return sender.callback(new IslandCreatePacket(this.name, uuid), Integer.class).timeout(30000L).async().thenCompose(islandId->{
                    if(islandId != null){
                        return registry.getIsland(islandId);
                    }else{
                        return CompletableFuture.completedFuture(null);
                    }
                });
            }else{
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public CompletableFuture<Island> load(int islandId) {
        return OperationPrecondition.canUpdate(islandId, true).thenCompose(canUpdate->{
            if(canUpdate){
                return sender.callback(new IslandUpdatePacket(this.name, islandId, true), Integer.class).async().thenCompose(id->{
                    if(id != null){
                        return registry.getIsland(id);
                    }else
                        return CompletableFuture.completedFuture(null);
                });
            }else{
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> unload(int islandId) {
        return OperationPrecondition.canUpdate(islandId, false).thenCompose(canUpdate->{
            if(canUpdate){
                return sender.callback(new IslandUpdatePacket(this.name, islandId, false), Integer.class)
                        .async()
                        .thenApply(Objects::nonNull);

            }else{
                return CompletableFuture.completedFuture(false);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(int islandId) {
        return sender.callback(new IslandDeletePacket(this.name, islandId), Boolean.class).async();
    }

    @Override
    public <T extends IslandComponent> CompletableFuture<Boolean> sync(int islandId, Class<T> component) {
        return OperationPrecondition.shouldSync(islandId).thenCompose(shouldSync->{
            if(shouldSync){
                byte componentId = registry.getComponentId(component);
                if(componentId != -1) {
                    return sender.callback(new IslandSyncPacket(islandId, componentId), Boolean.class)
                            .async();
                }else{
                    return CompletableFuture.completedFuture(false);
                }
            }
            return CompletableFuture.completedFuture(true);
        });
    }

    @Override
    public CompletableFuture<Boolean> syncIsland(int islandId) {
        return OperationPrecondition.shouldSync(islandId).thenCompose(shouldSync->{
            if(shouldSync){
                byte componentId = registry.getComponentId(Island.class);
                return sender.callback(new IslandSyncPacket(islandId, componentId), Boolean.class)
                        .async();
            }else
                return CompletableFuture.completedFuture(true);
        });
    }
}
