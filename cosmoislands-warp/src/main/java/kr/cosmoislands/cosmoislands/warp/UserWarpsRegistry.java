package kr.cosmoislands.cosmoislands.warp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.cosmoisland.cosmoislands.api.warp.IslandUserWarp;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.UUID;

@RequiredArgsConstructor
public class UserWarpsRegistry {

    private final MySQLUserWarpsModel userWarpsModel;
    private final LoadingCache<UUID, IslandUserWarp> cache = CacheBuilder.newBuilder()
            .build(new CacheLoader<UUID, IslandUserWarp>() {
                @Override
                public IslandUserWarp load(UUID uuid) throws Exception {
                    return new MySQLUserWarp(uuid, userWarpsModel);
                }
            });

    @SneakyThrows
    public IslandUserWarp getUserWarp(UUID uuid){
        return cache.get(uuid);
    }
}
