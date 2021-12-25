package kr.cosmoislands.cosmoislands.core;

import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.IslandStatus;
import kr.cosmoislands.cosmoislands.api.IslandStatusRegistry;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class RedisIslandStatusRegistry implements IslandStatusRegistry {

    private static final String statusKey = "cosmoislands:status";

    private final RedisAsyncCommands<String, String> async;

    @Override
    public CompletableFuture<IslandStatus> getStatus(int islandId){
        return async.hget(statusKey, islandId+"").thenApply(value->{
            if(value != null) {
                return IslandStatus.byCode(Byte.parseByte(value));
            }else{
                return IslandStatus.OFFLINE;
            }
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> setStatus(int islandId, IslandStatus status){
        if(status == IslandStatus.OFFLINE){
            return async.hdel(statusKey, islandId+"")
                    .thenRun(() -> {})
                    .toCompletableFuture();
        }else {
            return async.hset(statusKey, islandId + "", status.code() + "")
                    .thenRun(() -> {})
                    .toCompletableFuture();
        }
    }

    @Override
    public CompletableFuture<Void> reset(List<Integer> idList){
        return async.hdel(statusKey, idList.stream().map(id -> Integer.toString(id)).toArray(String[]::new))
                .thenRun(()->{})
                .toCompletableFuture();
    }

}
