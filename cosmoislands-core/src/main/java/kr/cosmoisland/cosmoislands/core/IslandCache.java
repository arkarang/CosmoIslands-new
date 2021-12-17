package kr.cosmoisland.cosmoislands.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

//todo: 나중에 카페인캐시로 바꾸기
//https://github.com/ben-manes/caffeine
public class IslandCache<T> {

    private final LoadingCache<UUID, T> cache;

    public IslandCache(CacheLoader<UUID, T> loader){
        cache = CacheBuilder.newBuilder().build(loader);
    }

    public IslandCache(LoadingCache<UUID, T> cache){
        this.cache = cache;
    }

    public T get(UUID uuid) throws ExecutionException {
        return cache.get(uuid);
    }

    public T refreshAndGet(UUID uuid) throws ExecutionException {
        update(uuid);
        return get(uuid);
    }

    public void remove(UUID uuid){
        cache.invalidate(uuid);
    }

    public void update(UUID uuid){
        cache.refresh(uuid);
    }

}
