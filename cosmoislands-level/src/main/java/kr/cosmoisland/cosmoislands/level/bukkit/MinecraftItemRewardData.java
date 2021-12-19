package kr.cosmoisland.cosmoislands.level.bukkit;

import com.minepalm.arkarangutils.bukkit.ItemUtils;
import kr.cosmoisland.cosmoislands.level.AbstractRewardData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MinecraftItemRewardData extends AbstractRewardData {

    @Getter
    final ItemStack[] items;

    public MinecraftItemRewardData(int id, int requiredLevel, ItemStack[] items) {
        super(id, requiredLevel);
        this.items = items;
    }

    @Override
    public void provide(UUID uuid) throws IllegalStateException{
        Player player = getPlayer(uuid);
        Inventory inv = player.getInventory();
        if(ItemUtils.hasSlots(inv, items)){
            inv.addItem(items);
        }else
            throw new IllegalStateException("have not slot");
    }

    Player getPlayer(UUID uuid){
        return Bukkit.getPlayer(uuid);
    }

}
