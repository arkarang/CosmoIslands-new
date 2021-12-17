package kr.cosmoislands.cosmoislands.bukkit;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.bukkit.utils.IslandIcon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class IslandInfoGUI extends ArkarangGUI {

    private static final int CENTER = 4;
    private static final ItemStack glass;

    static{
        glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
    }

    private final Island island;
    private final BukkitExecutor executor;

    public IslandInfoGUI(Island island, BukkitExecutor executor) {
        super(InventoryType.DISPENSER, "섬 정보");
        this.island = island;
        this.executor = executor;

        for(int i = 0; i < 9; i++){
            this.inv.setItem(i, glass);
        }

        CompletableFuture<ItemStack> iconFuture = IslandIcon.of(island).provide();
        iconFuture.thenAccept(icon -> executor.sync(()->inv.setItem(CENTER, icon)));

    }

}
