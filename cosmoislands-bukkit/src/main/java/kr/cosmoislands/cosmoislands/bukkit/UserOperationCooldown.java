package kr.cosmoislands.cosmoislands.bukkit;

import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class UserOperationCooldown {

    private final long CREATE_COOLDOWN = 1000*60*10, DELETE_COOLDOWN = 1000*60*10;

    private final RedisAsyncCommands<String, String> async;

    public CompletableFuture<Boolean> canExecute(Player player, String type){
        if(player.isOp()){
            return CompletableFuture.completedFuture(true);
        }else{
            return getLastExecuted(player.getUniqueId(), type).thenApply(lastIssued->{

                switch (type){
                    default:
                    case "create":
                        return lastIssued + CREATE_COOLDOWN < System.currentTimeMillis();
                    case "delete":
                        return lastIssued + DELETE_COOLDOWN < System.currentTimeMillis();
                }
            }).toCompletableFuture();
        }
    }

    private String redisKey(UUID uuid, String type){
        return "cosmoislands:command:"+type+":"+uuid.toString();
    }

    public CompletableFuture<Long> getLastExecuted(UUID uuid, String type){
        return async.get(redisKey(uuid, type)).thenApply(value-> {
            long lastIssued = 0;
            if (value != null) {
                lastIssued = Long.parseLong(value);
            }
            return lastIssued;
        }).toCompletableFuture();
    }

    public void submit(UUID uuid, String type){
        if(type.equalsIgnoreCase("create")){
            val f1 = async.set(redisKey(uuid, type), Long.toString(System.currentTimeMillis()+CREATE_COOLDOWN));
            val f2 = async.set(redisKey(uuid, "delete"), Long.toString(System.currentTimeMillis()+DELETE_COOLDOWN));
        }else if(type.equalsIgnoreCase("delete")){
            val f2 = async.set(redisKey(uuid, "delete"), Long.toString(System.currentTimeMillis()+DELETE_COOLDOWN));
        }
    }
}
