package kr.cosmoisland.cosmoislands.level;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.cosmoisland.cosmoislands.api.level.IslandRewardData;
import kr.cosmoisland.cosmoislands.api.level.IslandRewardsRegistry;
import kr.cosmoisland.cosmoislands.core.Database;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RewardDataRegistry implements IslandRewardsRegistry {

    private final IslandRewardDataModel model;
    @Getter
    private final RewardDataFactory factory = new RewardDataFactory();
    private final LoadingCache<Integer, CompletableFuture<IslandRewardData>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<Integer, CompletableFuture<IslandRewardData>>() {
                @Override
                public CompletableFuture<IslandRewardData> load(Integer id) throws Exception {
                    CompletableFuture<IslandRewardData> future = model.get(id);
                    future.thenAccept(data-> lastUpdated.put(id, data));
                    return future;
                }
            });
    private final ConcurrentHashMap<Integer, IslandRewardData> lastUpdated = new ConcurrentHashMap<>();

    RewardDataRegistry(Database database){
        this.model = new IslandRewardDataModel(database, "cosmoislands_reward_data", factory);
    }

    void cacheAll(){
        model.getAll().thenAccept(list->{
            for (IslandRewardData data : list) {
                cache.put(data.getId(), CompletableFuture.completedFuture(data));
                lastUpdated.put(data.getId(), data);
            }
        });
    }

    @Override
    @SneakyThrows
    public IslandRewardData getRewardData(int id) {
        CompletableFuture<IslandRewardData> future = cache.get(id);
        if(future.isDone()){
            return future.get();
        }else{
            return lastUpdated.get(id);
        }
    }

    @Override
    public CompletableFuture<Void> insertRewardData(IslandRewardData data) {
        cache.put(data.getId(), CompletableFuture.completedFuture(data));
        lastUpdated.put(data.getId(), data);
        return model.insert(data);
    }

    @Override
    public CompletableFuture<Void> setRequiredLevel(int id, int level) {
        return model.setRequiredLevel(id, level);
    }

    @Override
    public CompletableFuture<List<IslandRewardData>> getAll() {
        return model.getAll();
    }

    public void sync(int id){
        cache.invalidate(id);
    }
}
