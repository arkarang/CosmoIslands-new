package kr.cosmoislands.cosmoislands.players.internship;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.member.IslandInternship;
import kr.cosmoislands.cosmoislands.api.member.IslandInternshipRegistry;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CosmoIslandInternshipRegistry implements IslandInternshipRegistry {

    private static final String key = "cosmoislands:player:";

    private final IslandRegistry registry;
    private final MySQLIslandInternshipDataModel model;
    private final RedisAsyncCommands<String, String> async;

    public static CosmoIslandInternshipRegistry build(String playersTable,
                                                      String islandTable,
                                                      MySQLDatabase database,
                                                      RedisAsyncCommands<String, String> async,
                                                      IslandRegistry islandRegistry){
        MySQLIslandInternshipDataModel model = new MySQLIslandInternshipDataModel(database,
                playersTable,
                "cosmoislands_players_max_interns",
                islandTable);
        model.init();
        return new CosmoIslandInternshipRegistry(islandRegistry, model, async);
    }

    LoadingCache<UUID, IslandInternship> cache = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<UUID, IslandInternship>() {
                @Override
                public IslandInternship load(UUID uuid) throws Exception {
                    return new CosmoIslandInternship(uuid, registry, CosmoIslandInternshipRegistry.this);
                }
            });

    @Override
    @SneakyThrows
    public IslandInternship get(UUID uuid) {
        return cache.get(uuid);
    }

    @Override
    public CompletableFuture<List<Integer>> getHiredIslandIds(UUID uuid) {
        return async.smembers(islandsKey(uuid)).thenCompose(values->{
            if(values == null){
                return model.getInternships(uuid).thenApply(list->{
                    async.sadd(islandsKey(uuid), list.stream().map(number -> Integer.toString(number)).toArray(String[]::new));
                    return list;
                });
            }else {
                List<Integer> list = new ArrayList<>();
                for (String value : values) {
                    try {
                        int id = Integer.parseInt(value);
                        list.add(id);
                    }catch (IllegalArgumentException ignored){

                    }
                }
                return CompletableFuture.completedFuture(list);
            }
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Integer> getMaxInternships(UUID uuid) {
        return model.getMaxInternships(uuid);
    }

    @Override
    public CompletableFuture<Void> update(UUID uuid) {
        return async.del(islandsKey(uuid)).thenCompose(ignored -> {
            return model.getInternships(uuid).thenAccept(list -> {
                async.sadd(islandsKey(uuid), list.stream().map(Object::toString).toArray(String[]::new));
            });
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> unload(UUID uuid) {
        return null;
    }

    private static String islandsKey(UUID uuid){
        return key+uuid+":interns";
    }
}
