package kr.cosmoislands.cosmoislands.bukkit.level;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import kr.cosmoisland.cosmoislands.level.IslandAchievementsModule;
import kr.cosmoisland.cosmoislands.level.bukkit.MinecraftItemRewardData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AchievementEditGUI extends ArkarangGUI {

    IslandAchievementsModule module;
    MinecraftItemRewardData data;

    public AchievementEditGUI(IslandAchievementsModule module, MinecraftItemRewardData data) {
        super(1, data.getRequiredLevel()+"레벨 보상 수정하기");
        this.module = module;
        this.data = data;
        for(int i = 0; i < data.getItems().length && i < 9 ; i++){
            inv.setItem(i, data.getItems()[i]);
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event){
        List<ItemStack> newList = new ArrayList<>();
        for(int i = 0; i < 9 ; i++){
            ItemStack item = inv.getItem(i);
            if(item != null && item.getType() != Material.AIR) {
                newList.add(inv.getItem(i));
            }
        }
        this.module.getRewardDataRegistry().insertRewardData(new MinecraftItemRewardData(data.getId(), data.getRequiredLevel(), newList.toArray(new ItemStack[0])));
    }
}
