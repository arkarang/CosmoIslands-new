package kr.cosmoislands.cosmoislands.member;

import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.settings.IslandSettingsModule;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMapModule;
import kr.cosmoislands.cosmoislands.api.member.ModificationStrategyRegistry;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.Getter;

import java.util.UUID;
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
        this.model.init();
        this.playersMapRegistry = new PlayersMapRegistry(islandRegistry, this.registry, settingsModule, this.redis, model, this.strategyRegistry);
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
    public void invalidate(int islandId) {
        playersMapRegistry.invalidate(islandId);
    }

    @Override
    public CompletableFuture<Void> create(int islandId, UUID uuid) {
        return this.model.create(islandId, uuid);
    }

    @Override
    public void onEnable(IslandService service) {
        service.getFactory().addFirst("players", new PlayersMapLifecycle(this, this.registry, this.playersMapRegistry));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
