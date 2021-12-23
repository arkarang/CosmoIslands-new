package kr.cosmoislands.cosmoislands.core;

import com.minepalm.hellobungee.api.HelloSender;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.IslandCloud;
import kr.cosmoislands.cosmoislands.api.IslandHostServer;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import lombok.val;

import java.util.concurrent.CompletableFuture;

public class RedisIslandHostServer extends RedisIslandServer implements IslandHostServer {
    RedisIslandHostServer(String name,
                          Type type,
                          String islandKey,
                          IslandRegistry registry,
                          IslandCloud cloud,
                          HelloSender sender,
                          RedisAsyncCommands<String, String> async) {
        super(name, type, islandKey, registry, cloud, sender, async);
    }

    @Override
    public CompletableFuture<Void> registerServer() {
        return this.getIslands().thenAccept(list->{
            cloud.getStatusRegistry().reset(list);
        }).thenCompose(ignored->{
            return cloud.updateOnline(this.name, true);
        });
    }


    @Override
    public CompletableFuture<Void> shutdown() {
        val future = this.async.del(redisKey).toCompletableFuture();
        val future2 = this.getIslands().thenCompose(list->{
            return cloud.getStatusRegistry().reset(list);
        });
        return CompletableFuture.allOf(future, future2);
    }
}
