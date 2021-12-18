package kr.cosmoislands.cosmoislands.bukkit.warp;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.warp.IslandLocation;
import kr.cosmoisland.cosmoislands.api.warp.IslandWarp;
import kr.cosmoisland.cosmoislands.api.warp.TeleportExecutor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WarpListGUI extends ArkarangGUI {

    private static final ItemStack glass = new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(" ").getHandle();
    private static final Map<Integer, Integer> slots = new HashMap<>();

    static{
        for(int i = 0; i < 27; i++){
            slots.put(i, i+9);
        }
    }

    private final Island island;
    private final TeleportExecutor teleportExecutor;
    private final List<IslandWarp> warps;

    public WarpListGUI(Island island, TeleportExecutor teleportExecutor, List<IslandWarp> warps) {
        super(6, "워프 목록");
        this.island = island;
        this.teleportExecutor = teleportExecutor;
        this.warps = warps;
        for(int i = 0; i < 9; i++){
            inv.setItem(i, glass);
            inv.setItem(i+45, glass);
        }
        for(int i = 0; i < 27 && i < this.warps.size(); i++){
            if(slots.containsKey(i)){
                IslandWarp warp = warps.get(i);
                inv.setItem(slots.get(i), toIcon(warp));
                funcs.put(slots.get(i), provideFunction(warp));
            }
        }
    }

    private ItemStack toIcon(IslandWarp warp){
        ItemStackBuilder builder = new ItemStackBuilder(Material.GRASS_BLOCK);
        builder.setName("§f워프: "+warp.getName());
        builder.addLine("§f클릭시 워프합니다.");
        return builder.getHandle();
    }

    private Consumer<InventoryClickEvent> provideFunction(IslandWarp warp){
        return event -> {
            Player player = (Player)event.getWhoClicked();
            player.closeInventory();
            teleportExecutor.teleportPlayer(player.getUniqueId(), new IslandLocation(island.getId(), warp));
        };
    }
}
