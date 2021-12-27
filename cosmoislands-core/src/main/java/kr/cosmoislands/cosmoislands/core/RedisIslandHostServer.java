package kr.cosmoislands.cosmoislands.core;

import com.minepalm.hellobungee.api.HelloSender;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.IslandCloud;
import kr.cosmoislands.cosmoislands.api.IslandHostServer;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RedisIslandHostServer extends RedisIslandServer implements IslandHostServer {

    RedisIslandHostServer(String name,
                          Type type,
                          String islandLocationKey,
                          IslandRegistry registry,
                          IslandCloud cloud,
                          HelloSender sender,
                          RedisAsyncCommands<String, String> async) {
        super(name, type, islandLocationKey, registry, cloud, sender, async);
    }

    @Override
    public CompletableFuture<Void> registerServer() {
        return this.getIslands().thenCompose(list->{
            val future2 = resetRedisData();
            return CompletableFuture.allOf(future2);
        }).thenCompose(ignored->{
            return cloud.updateOnline(this.name, true);
        });
    }

    private CompletableFuture<Void> resetRedisData(){
        final List<String> list = new ArrayList<>();
        return async.hscan((key, value) -> {
            if(value.equalsIgnoreCase(this.name)){
                list.add(key);
            }
        }, islandLocationKey).toCompletableFuture().thenCompose(cursor->{
            if(!list.isEmpty()) {
                DebugLogger.log("resetRedisData: "+ Arrays.toString(list.toArray(new String[0])));
                val resetStatus = cloud.getStatusRegistry().reset(list.stream().map(Integer::parseInt).collect(Collectors.toList()));
                val locationReset = async.hdel(islandLocationKey, list.toArray(new String[0])).toCompletableFuture();
                val resetLoaded = async.del(redisKey).toCompletableFuture();
                return resetStatus.thenCompose(ignored-> CompletableFuture.allOf(locationReset, resetLoaded));
            }else{
                DebugLogger.log("resetRedisData: is empty");
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        val future = resetRedisData();
        val updateOfflineFuture = cloud.updateOnline(this.name, false);
        return CompletableFuture.allOf(future, updateOfflineFuture);
    }
}
