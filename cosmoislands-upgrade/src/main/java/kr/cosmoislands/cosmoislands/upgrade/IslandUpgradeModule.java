package kr.cosmoislands.cosmoislands.upgrade;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.cosmoislands.cosmoislands.api.IslandModule;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgrade;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class IslandUpgradeModule implements IslandModule<IslandUpgrade> {

    private final IslandRegistry registry;
    private final MySQLIslandUpgradeDataModel upgradeModel;
    @Getter
    private final IslandUpgradeSettingRegistry settingsRegistry;
    @Getter
    private final UpgradeConditionFactory factory;
    @Getter
    private final Logger logger;

    private final LoadingCache<Integer, IslandUpgrade> cache = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<Integer, IslandUpgrade>() {
                @Override
                public IslandUpgrade load(Integer islandId) throws Exception {
                    return new MySQLIslandUpgrade(islandId, registry, upgradeModel, factory);
                }
            });

    public IslandUpgradeModule(IslandRegistry registry, Database database, Logger logger){
        upgradeModel = new MySQLIslandUpgradeDataModel("cosmoislands_upgrade_levels", "cosmoislands_islands", database);
        UpgradeSettingsDataModel settingsModel = new UpgradeSettingsDataModel("cosmoislands_upgrade_settings", database);
        settingsModel.init();
        upgradeModel.init();
        this.registry = registry;
        this.settingsRegistry = new IslandUpgradeSettingRegistry(settingsModel);
        this.factory = new UpgradeConditionFactory(this.settingsRegistry);
        this.logger = logger;
    }

    @Override
    public CompletableFuture<IslandUpgrade> getAsync(int islandId) {
        return CompletableFuture.completedFuture(get(islandId));
    }

    @Override
    @SneakyThrows
    public IslandUpgrade get(int islandId) {
        return cache.get(islandId);
    }

    @Override
    public void invalidate(int islandId) {
        cache.invalidate(islandId);
    }

    @Override
    public CompletableFuture<Void> create(int islandId, UUID uuid) {
        List<CompletableFuture<?>> list = new ArrayList<>();
        for (IslandUpgradeType value : IslandUpgradeType.values()) {
            list.add(upgradeModel.setLevel(islandId, value, 0));
        }
        return CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
    }

    @Override
    public void onEnable(IslandService service) {
        service.getFactory().addLast("upgrade", new IslandUpgradeLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
