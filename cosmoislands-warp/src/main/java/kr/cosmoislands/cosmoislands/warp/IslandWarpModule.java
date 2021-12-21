package kr.cosmoislands.cosmoislands.warp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.warp.IslandWarpsMap;
import kr.cosmoisland.cosmoislands.core.Database;
import kr.cosmoisland.cosmoislands.settings.IslandSettingsModule;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class IslandWarpModule implements IslandModule<IslandWarpsMap> {

    private final IslandSettingsModule settingsModule;
    @Getter
    private final CosmoTeleportExecutor teleportExecutor;
    @Getter
    private final UserWarpsRegistry userWarpsRegistry;
    private final MySQLIslandWarpsDataModel islandWarpsModel;
    @Getter
    private final Logger logger;

    LoadingCache<Integer, IslandWarpsMap> cache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, IslandWarpsMap>() {
        @Override
        public IslandWarpsMap load(Integer integer) throws Exception {
            return new MySQLIslandWarpsMap(integer, settingsModule.get(integer), islandWarpsModel);
        }
    });

    public IslandWarpModule(Database database,
                            IslandRegistry islandRegistry,
                            IslandPlayerRegistry playerRegistry,
                            CosmoTeleport teleportService,
                            IslandSettingsModule settingsModule,
                            Logger logger){
        this.logger = logger;
        this.settingsModule = settingsModule;
        MySQLUserWarpsModel userWarpsModel = new MySQLUserWarpsModel(database, "cosmoislands_userwarps", "cosmoislands_islands");
        this.islandWarpsModel = new MySQLIslandWarpsDataModel(database, "cosmoislands_islandwarps", "cosmoislands_islands");
        this.userWarpsRegistry = new UserWarpsRegistry(userWarpsModel);
        this.teleportExecutor = new CosmoTeleportExecutor(islandRegistry, playerRegistry, teleportService);
        this.islandWarpsModel.init();
        userWarpsModel.init();
    }

    @Override
    public CompletableFuture<IslandWarpsMap> getAsync(int islandId) {
        return CompletableFuture.completedFuture(get(islandId));
    }

    @Override
    @SneakyThrows
    public IslandWarpsMap get(int islandId) {
        return cache.get(islandId);
    }

    @Override
    public void invalidate(int islandId) {
        cache.invalidate(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        service.getFactory().addLast("warps", new IslandWarpLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
