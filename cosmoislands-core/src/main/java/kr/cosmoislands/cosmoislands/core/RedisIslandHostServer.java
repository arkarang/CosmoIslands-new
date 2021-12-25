package kr.cosmoislands.cosmoislands.core;

import com.minepalm.hellobungee.api.HelloSender;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.output.KeyValueStreamingChannel;
import kr.cosmoislands.cosmoislands.api.IslandCloud;
import kr.cosmoislands.cosmoislands.api.IslandHostServer;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
            CompletableFuture<Void> future1 = CompletableFuture.completedFuture(null);
            if(!list.isEmpty()) {
                future1 = cloud.getStatusRegistry().reset(list);
            }
            val future2 = resetIslandLocations();
            return CompletableFuture.allOf(future1, future2);
        }).thenCompose(ignored->{
            return cloud.updateOnline(this.name, true);
        });
    }

    private CompletableFuture<Void> resetIslandLocations(){
        final List<String> list = new ArrayList<>();
        return async.hscan((key, value) -> {
            if(value.equalsIgnoreCase(this.name)){
                list.add(key);
            }
        }, islandLocationKey).toCompletableFuture().thenCompose(cursor->{
            if(!list.isEmpty()) {
                return async.hdel(islandLocationKey, list.toArray(new String[0])).toCompletableFuture().thenRun(() -> { });
            }else{
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        val future1 = this.getIslands().thenCompose(list->{
            return cloud.getStatusRegistry().reset(list);
        });
        val future2 = future1.thenCompose(ignored->this.async.del(redisKey).toCompletableFuture());
        val future3 = resetIslandLocations();
        return CompletableFuture.allOf(future1, future2, future3);
    }
}
