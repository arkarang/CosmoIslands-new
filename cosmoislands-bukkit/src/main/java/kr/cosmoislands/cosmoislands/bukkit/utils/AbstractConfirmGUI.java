package kr.cosmoislands.cosmoislands.bukkit.utils;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AbstractConfirmGUI extends ArkarangGUI {

    private static final ItemStack glass;

    static{
        glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
    }

    private final ConfirmCompounds components;

    public AbstractConfirmGUI(ConfirmCompounds comp) {
        super(1, comp.getTitle());
        // 0 1 2v 3 4 5 6v 7 8
        this.components = comp;
        funcs.put(2, (event)->{
            Player player = (Player)event.getWhoClicked();
            components.onConfirm(player);
            player.closeInventory();
        });
        funcs.put(6, (event)->{
            Player player = (Player)event.getWhoClicked();
            components.onReject(player);
            player.closeInventory();
        });
        for(int i = 0; i < 9; i++){
            inv.setItem(i, glass);
        }
        inv.setItem(2, getConfirmItem());
        inv.setItem(6, getRejectItem());
    }

    private ItemStack getConfirmItem(){
        ItemStackBuilder builder = new ItemStackBuilder(new ItemStack(Material.GREEN_WOOL, 1)).setName(components.getConfirm().getKey());
        components.getConfirm().getValue().forEach(builder::addLine);
        return builder.getHandle();
    }

    private ItemStack getRejectItem(){
        ItemStackBuilder builder = new ItemStackBuilder(new ItemStack(Material.RED_WOOL, 1)).setName(components.getReject().getKey());
        components.getReject().getValue().forEach(builder::addLine);
        return builder.getHandle();
    }
}
