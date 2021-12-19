package kr.comsoislands.comsoislands.member;

import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMapModule;
import kr.cosmoisland.cosmoislands.api.player.ModificationStrategyRegistry;
import kr.cosmoisland.cosmoislands.core.Database;
import kr.cosmoisland.cosmoislands.settings.IslandSettingsModule;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class PlayersMapModule implements IslandPlayersMapModule {

    @Getter
    private final Logger logger;
    private final RedisAsyncCommands<String, String> redis;
    private final Database database;
    @Getter
    private final PlayersMapRegistry playersMapRegistry;
    @Getter
    private final IslandPlayerRegistry registry;
    private final IslandSettingsModule settingsModule;
    private final PlayersMapDataModel model;
    @Getter
    private final ModificationStrategyRegistry strategyRegistry;

    public PlayersMapModule(IslandRegistry islandRegistry,
                            IslandPlayerRegistry registry,
                            IslandSettingsModule settingsModule,
                            Database database,
                            RedisAsyncCommands<String, String> redis, Logger logger){
        this.database = database;
        this.redis = redis;
        this.logger = logger;
        this.registry = registry;
        this.settingsModule = settingsModule;
        this.strategyRegistry = new CosmoPlayerModificationStrategyRegistry();
        this.model = new PlayersMapDataModel("cosmoislands_members", "cosmoislands_islands", database);
        this.playersMapRegistry = new PlayersMapRegistry(islandRegistry, this.registry, model, settingsModule, this.redis, this.strategyRegistry);
    }

    @Override
    public CompletableFuture<IslandPlayersMap> getAsync(int islandId) {
        return playersMapRegistry.getAsync(islandId);
    }

    @Override
    public IslandPlayersMap get(int islandId) {
        return playersMapRegistry.get(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        this.model.init();
        this.database.register(PlayersMapDataModel.class, this.model);
        service.getFactory().addFirst("players", new PlayersMapLifecycle(this.playersMapRegistry));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
