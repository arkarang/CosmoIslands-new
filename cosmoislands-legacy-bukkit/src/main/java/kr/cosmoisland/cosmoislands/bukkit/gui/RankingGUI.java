package kr.cosmoisland.cosmoislands.bukkit.gui;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import kr.cosmoisland.cosmoislands.bukkit.gui.components.RankingComponents;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RankingGUI extends ArkarangGUI {

    public static final ItemStack glass;
    public static final int[] slots = new int[3];
    public static final ItemStack[] places = new ItemStack[3];

    static{
        glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1);
        places[0] = new ItemStackBuilder(Material.DIAMOND_BLOCK).setName("§b§l1§f§l등").getHandle();
        places[1] = new ItemStackBuilder(Material.GOLD_BLOCK).setName("§b§l2§f§l등").getHandle();
        places[2] = new ItemStackBuilder(Material.IRON_BLOCK).setName("§b§l3§f§l등").getHandle();
        slots[0] = 2;
        slots[1] = 4;
        slots[2] = 6;
    }

    public RankingComponents components;

    public RankingGUI(RankingComponents components, Map<Integer, IslandRankingData> data) {
        super(4, components.getTitle());
        this.components = components;
        for(int i = 0 ; i < 36; i++){
            inv.setItem(i, glass);
        }
        for(int i = 0 ; i < 3 ; i++){
            if(data.containsKey(i)){
                inv.setItem(9+slots[i], places[i]);
                inv.setItem(18+slots[i], getIcon(data.get(i)));
            }
        }

    }

    public ItemStack getIcon(IslandRankingData data){
        OfflinePlayer player = Bukkit.getOfflinePlayer(data.getOwner());
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);
        return new ItemStackBuilder(item).setName("§a§l섬 §f§l이름 §f: "+Optional.ofNullable(data.getDisplayname()).orElseGet(()->player.getName()+"님의 섬"))
                .addLine(components.formatData(data.getValue())).getHandle();
    }
}
