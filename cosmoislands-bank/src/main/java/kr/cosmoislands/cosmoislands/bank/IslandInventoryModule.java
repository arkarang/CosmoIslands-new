package kr.cosmoislands.cosmoislands.bank;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.bank.IslandBank;
import kr.cosmoisland.cosmoislands.core.Database;
import lombok.Getter;

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
            .expireAfterWrite(5, TimeUnit.MINUTES)
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
    public void onEnable(IslandService service) {
        model.init();
        service.getFactory().addLast("inventory", new BankLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
