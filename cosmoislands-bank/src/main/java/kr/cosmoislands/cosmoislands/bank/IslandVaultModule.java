package kr.cosmoislands.cosmoislands.bank;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import kr.cosmoislands.cosmoislands.api.IslandModule;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.bank.IslandVault;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class IslandVaultModule implements IslandModule<IslandVault> {

    @Getter
    private final Logger logger;
    private final IslandVaultDataModel model;
    private final LoadingCache<Integer, IslandVault> cache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, IslandVault>() {
        @Override
        public IslandVault load(Integer islandId) throws Exception {
            return new MySQLIslandVault(islandId, model);
        }
    });

    public IslandVaultModule(Database database, Logger logger){
        this.logger = logger;
        this.model = new IslandVaultDataModel("cosmoislands_money", "cosmoislands_islands", database);
    }

    @Override
    public CompletableFuture<IslandVault> getAsync(int islandId) {
        try {
            return CompletableFuture.completedFuture(cache.get(islandId));
        }catch (ExecutionException e){
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public IslandVault get(int islandId) {
        try{
            return cache.get(islandId);
        }catch (ExecutionException e){
            return null;
        }
    }

    @Override
    public void invalidate(int islandId) {
        cache.invalidate(islandId);
    }

    @Override
    public CompletableFuture<Void> create(int islandId, UUID uuid) {
        return model.create(islandId, uuid);
    }

    @Override
    public void onEnable(IslandService service) {
        model.init();
        service.getFactory().addLast("money", new VaultLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
