package kr.cosmoislands.cosmoislands.bukkit.level;

import com.minepalm.arkarangutils.bukkit.SimpleGUI;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.bukkit.database.RewardSettingLoader;
import kr.cosmoisland.cosmoislands.bukkit.island.RewardSetting;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class IslandRewardEditGUI extends SimpleGUI {

    int slot;
    RewardSetting setting;

    public IslandRewardEditGUI(int slot, RewardSetting setting) {
        super(1, setting.getRequiredLevel()+"레벨 보상 수정하기");
        this.slot = slot;
        this.setting = setting;
        for(int i = 0; i < setting.getRewards().size() || i < 9 ; i++){
            inv.setItem(i, setting.getRewards().get(i));
        }
    }

    @Override
    public void onClose(){
        List<ItemStack> newList = new ArrayList<>();
        for(int i = 0; i < 9 ; i++){
            newList.add(inv.getItem(i));
        }
        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
            CosmoIslandsBukkitBootstrap.getInst().getDatabase().getLoader(RewardSettingLoader.class).setReward(slot, setting.setReward(newList));
        });
    }
}
