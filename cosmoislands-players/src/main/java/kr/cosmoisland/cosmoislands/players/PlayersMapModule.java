package kr.cosmoisland.cosmoislands.players;

import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMapModule;
import kr.cosmoisland.cosmoislands.api.player.ModificationStrategyRegistry;
import kr.cosmoisland.cosmoislands.core.Database;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    private final PlayersMapDataModel model;
    @Getter
    private final ModificationStrategyRegistry strategyRegistry;

    public PlayersMapModule(IslandRegistry islandRegistry,
                            IslandPlayerRegistry registry,
                            Database database,
                            RedisAsyncCommands<String, String> redis, Logger logger){
        this.database = database;
        this.redis = redis;
        this.logger = logger;
        this.registry = registry;
        this.strategyRegistry = new CosmoPlayerModificationStrategyRegistry();
        this.model = new PlayersMapDataModel("cosmoislands_members", "cosmoislands_interns ", "cosmoislands_islands", database);
        this.playersMapRegistry = new PlayersMapRegistry(islandRegistry, this.registry, model, this.redis, this.strategyRegistry);
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
