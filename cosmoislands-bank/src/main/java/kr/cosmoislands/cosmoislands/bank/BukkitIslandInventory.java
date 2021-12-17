package kr.cosmoislands.cosmoislands.bank;

import com.google.common.cache.LoadingCache;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.bank.IslandBank;
import kr.cosmoisland.cosmoislands.core.utils.Cached;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BukkitIslandInventory implements IslandBank  {

    private final int islandId;
    private final BukkitExecutor executor;
    private final IslandInventoryDataModel model;
    private final BankChestGUI gui;
    private final Cached<Integer> cachedLevel;

    BukkitIslandInventory(int islandId,
                          IslandInventoryView initialView,
                          IslandInventoryDataModel model,
                          LoadingCache<Integer, CompletableFuture<Integer>> cache,
                          BukkitExecutor executor){
        this.islandId = islandId;
        this.executor = executor;
        this.model = model;
        this.cachedLevel = new Cached<>(initialView.level, ()-> {
            try {
                return cache.get(islandId);
            } catch (ExecutionException e) {
                return CompletableFuture.completedFuture(initialView.level);
            }
        });
        this.gui = new BankChestGUI(this, initialView, executor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return saveInventory();
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public CompletableFuture<Integer> getLevel() {
        return model.getLevel(islandId);
    }

    @Override
    public CompletableFuture<Void> setLevel(int level) {
        return model.setLevel(islandId, level);
    }

    @Override
    public CompletableFuture<Void> saveInventory() {
        return model.saveInventory(islandId, gui.getContents());
    }

    @Override
    public CompletableFuture<Void> openInventory(UUID uuid) {
        return executor.sync(()->{
            Player player = Bukkit.getPlayer(uuid);
            if(player != null)
                gui.openGUI(player);
        });
    }

    int getCachedLevel(){
        return cachedLevel.get();
    }
}
