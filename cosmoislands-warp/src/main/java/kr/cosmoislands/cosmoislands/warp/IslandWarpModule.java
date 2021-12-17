package kr.cosmoislands.cosmoislands.warp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.warp.IslandWarp;
import kr.cosmoisland.cosmoislands.core.Database;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class IslandWarpModule implements IslandModule<IslandWarp> {

    @Getter
    private final CosmoTeleportExecutor teleportExecutor;
    @Getter
    private final UserWarpsRegistry userWarpsRegistry;
    private final MySQLIslandWarpsDataModel islandWarpsModel;
    @Getter
    private final Logger logger;

    LoadingCache<Integer, IslandWarp> cache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, IslandWarp>() {
        @Override
        public IslandWarp load(Integer integer) throws Exception {
            return new MySQLIslandWarps(integer, islandWarpsModel);
        }
    });

    public IslandWarpModule(Database database, IslandRegistry islandRegistry, CosmoTeleport teleportService, Logger logger){
        this.logger = logger;
        MySQLUserWarpsModel userWarpsModel = new MySQLUserWarpsModel(database, "cosmoislands_userwarps", "cosmoislands_islands");
        this.islandWarpsModel = new MySQLIslandWarpsDataModel(database, "cosmoislands_islandwarps", "cosmoislands_islands");
        this.userWarpsRegistry = new UserWarpsRegistry(userWarpsModel);
        this.teleportExecutor = new CosmoTeleportExecutor(islandRegistry, teleportService);
        this.islandWarpsModel.init();
        userWarpsModel.init();
    }

    @Override
    public CompletableFuture<IslandWarp> getAsync(int islandId) {
        return CompletableFuture.completedFuture(get(islandId));
    }

    @Override
    @SneakyThrows
    public IslandWarp get(int islandId) {
        return cache.get(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        service.getFactory().addLast("warps", new IslandWarpLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
