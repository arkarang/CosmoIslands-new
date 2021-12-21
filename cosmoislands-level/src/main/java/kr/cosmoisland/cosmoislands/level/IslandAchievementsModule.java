package kr.cosmoisland.cosmoislands.level;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.level.IslandAchievements;
import kr.cosmoisland.cosmoislands.core.Database;
import kr.cosmoisland.cosmoislands.level.bukkit.MinecraftItemRewardData;
import kr.cosmoisland.cosmoislands.level.bukkit.MinecraftItemRewardDataAdapter;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class IslandAchievementsModule implements IslandModule<IslandAchievements> {

    @Getter
    private final Logger logger;
    private final IslandAchievementsDataModel model;
    @Getter
    private final RewardDataRegistry rewardDataRegistry;
    private final LoadingCache<Integer, IslandAchievements> cache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, IslandAchievements>() {
        @Override
        public IslandAchievements load(Integer integer) throws Exception {
            return new MySQLIslandAchievements(integer, model);
        }
    });

    public IslandAchievementsModule(Database database, Logger logger){
        this.logger = logger;
        this.model = new IslandAchievementsDataModel("cosmoislands_island_achievements", "cosmoislands_islands", database);
        this.rewardDataRegistry = new RewardDataRegistry(database);
        this.rewardDataRegistry.cacheAll();
        this.rewardDataRegistry.getFactory().registerAdapter(MinecraftItemRewardData.class, new MinecraftItemRewardDataAdapter(this.logger));
    }

    @Override
    public CompletableFuture<IslandAchievements> getAsync(int islandId) {
        return CompletableFuture.completedFuture(get(islandId));
    }

    @Override
    @SneakyThrows
    public IslandAchievements get(int islandId) {
        return cache.get(islandId);
    }

    @Override
    public void invalidate(int islandId) {
        cache.invalidate(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        this.model.init();
        service.getFactory().addLast("achievements", new IslandAchievementsLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
