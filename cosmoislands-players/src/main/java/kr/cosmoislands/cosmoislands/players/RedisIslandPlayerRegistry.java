package kr.cosmoislands.cosmoislands.players;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.member.IslandInternship;
import kr.cosmoislands.cosmoislands.api.member.IslandInternshipRegistry;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.players.internship.RedisIslandInternshipRegistry;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class RedisIslandPlayerRegistry implements IslandPlayerRegistry {

    private static final String key = "cosmoislands:player:";

    final IslandRegistry registry;
    final IslandInternshipRegistry internshipRegistry;
    final MySQLIslandPlayerDatabase database;
    final RedisAsyncCommands<String, String> async;

    public static RedisIslandPlayerRegistry build(String playersTable,
                                                  String islandsTable,
                                                  MySQLDatabase database,
                                                  RedisAsyncCommands<String, String> async,
                                                  IslandRegistry islandRegistry){
        RedisIslandInternshipRegistry internshipRegistry
                = RedisIslandInternshipRegistry.build(playersTable, islandsTable, database, async, islandRegistry);
        MySQLIslandPlayerDatabase playerDatabase = new MySQLIslandPlayerDatabase(database, playersTable, islandsTable);
        playerDatabase.init();
        return new RedisIslandPlayerRegistry(islandRegistry, internshipRegistry, playerDatabase, async);
    }

    LoadingCache<UUID, IslandPlayer> cache = CacheBuilder.newBuilder().build(new CacheLoader<UUID, IslandPlayer>() {
        @Override
        public IslandPlayer load(UUID uuid) throws Exception {
            IslandInternship internship = internshipRegistry.get(uuid);
            return new CosmoIslandPlayer(uuid, registry, internship, RedisIslandPlayerRegistry.this);
        }
    });

    @Override
    public IslandPlayer get(UUID uuid) {
        try {
            return cache.get(uuid);
        }catch (ExecutionException e){
            return null;
        }
    }

    @Override
    public CompletableFuture<Integer> getIslandId(UUID uuid) {
        return async.get(redisKey(uuid)).thenCompose(value->{
            if(value == null){
                return database.get(uuid).thenApply(id->{
                    if(id != -1){
                        async.set(redisKey(uuid), id+"");
                    }
                    return id;
                });
            }else {
                try {
                    return CompletableFuture.completedFuture(Integer.parseInt(value));
                } catch (IllegalArgumentException e) {
                    return CompletableFuture.completedFuture(Island.NIL_ID);
                }
            }
        }).toCompletableFuture();
    }

    @Override
    public void load(UUID uuid) {
        cache.refresh(uuid);
    }

    @Override
    public void unload(UUID uuid) {
        async.del(redisKey(uuid));
    }

    private String redisKey(UUID uuid){
        return key+uuid+":island";
    }
}
