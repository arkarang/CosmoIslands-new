package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandCloud;
import kr.cosmoisland.cosmoislands.api.IslandStatus;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OperationPrecondition {

    static IslandPlayerRegistry playerRegistry;
    static IslandCloud cloud;

    public static void init(IslandPlayerRegistry playerReg, IslandCloud islandCloud){
        playerRegistry = playerReg;
        cloud = islandCloud;
    }

    public static CompletableFuture<Boolean> canCreate(UUID uuid){
        return playerRegistry.getIslandId(uuid).thenApply(id->{
            return id == Island.NIL_ID;
        });
    }

    public static CompletableFuture<Boolean> canUpdate(int islandId, boolean load){
        return cloud.getStatus(islandId).thenApply(status->{
            if(load){
                return status == IslandStatus.OFFLINE;
            }else{
                return status == IslandStatus.ONLINE;
            }
        });
    }

    public static CompletableFuture<Boolean> canDelete(int islandId){
        return CompletableFuture.completedFuture(true);
    }

    public static CompletableFuture<Boolean> shouldSync(int islandId){
        return cloud.getLocated(islandId).thenApply(Objects::nonNull);
    }

}
