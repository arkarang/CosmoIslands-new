package kr.cosmoislands.cosmoislands.bank;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.IslandModule;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.bank.IslandBank;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class IslandInventoryModule implements IslandModule<IslandBank> {

    private final BukkitExecutor executor;
    private final IslandInventoryDataModel model;
    @Getter
    private final Logger logger;
    LoadingCache<Integer, CompletableFuture<IslandBank>> cache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, CompletableFuture<IslandBank>>() {
        @Override
        public CompletableFuture<IslandBank> load(Integer islandId) throws Exception {
            return model.getView(islandId).thenApply(view-> new BukkitIslandInventory(islandId, view, model, levelCache, executor));
        }
    });
    LoadingCache<Integer, CompletableFuture<Integer>> levelCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<Integer, CompletableFuture<Integer>>() {
                @Override
                public CompletableFuture<Integer> load(Integer islandId) throws Exception {
                    return model.getLevel(islandId);
                }
            });

    public IslandInventoryModule(Database database, BukkitExecutor executor, Logger logger){
        this.executor = executor;
        this.model = new IslandInventoryDataModel("cosmoislands_inventory", "cosmoislands_islands", database);
        this.logger = logger;
    }

    @Override
    public CompletableFuture<IslandBank> getAsync(int islandId) {
        try {
            return cache.get(islandId);
        }catch (ExecutionException e){
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public IslandBank get(int islandId) {
        try {
            return cache.get(islandId).get();
        }catch (InterruptedException | ExecutionException e){
            return null;
        }
    }

    @Override
    public void invalidate(int islandId) {
        cache.invalidate(islandId);
    }

    @Override
    public CompletableFuture<Void> create(int islandId, UUID uuid) {
        return this.model.create(islandId, uuid);
    }

    @Override
    public void onEnable(IslandService service) {
        model.init();
        service.getFactory().addLast("inventory", new BankLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
