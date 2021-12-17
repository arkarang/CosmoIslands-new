package kr.comsoisland.cosmoislands.points;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.points.IslandPoints;
import kr.cosmoisland.cosmoislands.api.points.IslandVoter;
import kr.cosmoisland.cosmoislands.core.Database;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class IslandPointsModule implements IslandModule<IslandPoints> {

    @Getter
    private final Logger logger;
    private final IslandPointDataModel model;
    private final VoteLogDataModel voteModel;
    private final LoadingCache<Integer, IslandPoints> pointsCache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, IslandPoints>() {
        @Override
        public IslandPoints load(Integer id) throws Exception {
            return new MySQLIslandPoints(id, model);
        }
    });
    private final LoadingCache<UUID, IslandVoter> voterCache = CacheBuilder.newBuilder().build(new CacheLoader<UUID, IslandVoter>() {
        @Override
        public IslandVoter load(UUID uuid) throws Exception {
            return new CosmoIslandVoter(uuid, voteModel);
        }
    });

    IslandPointsModule(Database database, Logger logger){
        this.model = new IslandPointDataModel("cosmoislands_points", "cosmoislands_islands", database);
        this.voteModel = new VoteLogDataModel("cosmoislands_vote_log", database);
        this.logger = logger;
    }

    @Override
    public CompletableFuture<IslandPoints> getAsync(int islandId) {
        return CompletableFuture.completedFuture(get(islandId));
    }

    @Override
    public IslandPoints get(int islandId) {
        try {
            return pointsCache.get(islandId);
        }catch (ExecutionException e){
            return null;
        }
    }

    public CosmoIslandVoter getVoter(UUID uuid){
        return new CosmoIslandVoter(uuid, voteModel);
    }

    @Override
    public void onEnable(IslandService service) {
        this.model.init();
        this.voteModel.init();
        service.getFactory().addLast("settings", new PointsLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
