package kr.cosmoislands.cosmoislands.level;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.cosmoislands.cosmoislands.api.IslandModule;
import kr.cosmoislands.cosmoislands.api.IslandRanking;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.level.IslandLevel;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class IslandLevelModule implements IslandModule<IslandLevel> {

    @Getter
    private final Logger logger;
    @Getter
    private final IslandRanking ranking;
    private final IslandLevelDataModel model;
    private final LoadingCache<Integer, IslandLevel> cache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, IslandLevel>() {
        @Override
        public IslandLevel load(Integer integer) throws Exception {
            return new MySQLIslandLevel(integer, model);
        }
    });

    public IslandLevelModule(Database database, Logger logger){
        this.model = new IslandLevelDataModel("cosmoislands_level", "cosmoislands_islands", database);
        this.ranking = new LevelRanking(model);
        this.logger = logger;
    }

    @Override
    public CompletableFuture<IslandLevel> getAsync(int islandId) {
        return CompletableFuture.completedFuture(get(islandId));
    }

    @Override
    @SneakyThrows
    public IslandLevel get(int islandId) {
        return cache.get(islandId);
    }

    @Override
    public void invalidate(int islandId) {
        cache.invalidate(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        this.model.init();
        service.getFactory().addLast("level", new IslandLevelLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
