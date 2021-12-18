package kr.cosmoislands.cosmoislands.bukkit.level;

import com.google.common.collect.HashBiMap;
import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import com.minepalm.arkarangutils.bukkit.ItemUtils;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.level.IslandAchievements;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class IslandRewardGUI extends ArkarangGUI {

    private static final ItemStack yellow, white, bottle;
    private static HashMap<Integer, Integer> map = new HashMap<>();
    private static HashBiMap<Integer, Integer> slot2Slot = HashBiMap.create();

    static{
        yellow = new ItemStackBuilder(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        white = new ItemStackBuilder(new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
        slot2Slot.put(0, 19);
        slot2Slot.put(1, 29);
        slot2Slot.put(2, 21);
        slot2Slot.put(3, 31);
        slot2Slot.put(4, 23);
        slot2Slot.put(5, 33);
        slot2Slot.put(6, 25);
    }

    HashMap<Integer, AtomicBoolean> isRunning = new HashMap<>();
    RewardSettingLoader loader;
    IslandData data;
    IslandAchievements rewardsData;

    public IslandRewardGUI(Island island) throws ExecutionException, InterruptedException {
        super(6, "섬 보상");
        loader = CosmoIslandsBukkitBootstrap.getInst().getDatabase().getLoader(RewardSettingLoader.class);
        data = island.getData().get();
        rewardsData = island.getAchievementData().get();
        for(int i = 0; i < 9; i++){
            inv.setItem(i, yellow);
            inv.setItem(i+45, yellow);
            for(int j = 1; j < 5; j++){
                inv.setItem(i+j*9, white);
            }
        }
        try{
            int currentLevel = island.getData().get().getLevel().get();
            inv.setItem(4, getLevelIcon(currentLevel));
            for(int i = 0; i < slot2Slot.size(); i++){
                int slot = slot2Slot.getOrDefault(i, -1);
                if(slot != -1) {
                    isRunning.put(slot, new AtomicBoolean(false));
                    inv.setItem(slot, getRewardIcon(i, map.get(i), currentLevel, island.getAchievementData().get().isAchieved(i).get()));
                    setFunction(slot, CosmoIslandsBukkitBootstrap.getInst().getYamlIslandConfig().getAchievementLevel(i));
                }
            }
        }catch (ExecutionException | InterruptedException e){
            e.printStackTrace();
        }
    }


    private ItemStack getRewardIcon(int slot, int requiredLevel, int currentLevel, boolean isAchieved){
        ItemStack item = new ItemStack(Material.GOLD_BLOCK);
        if(isAchieved) {
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
            item.setItemMeta(meta);
        }
        ItemStackBuilder builder = new ItemStackBuilder(item)
                .setName("§f§l[ §e§l"+slot+"단계 보상 §f§l]".replaceAll("§", "§"))
                .addLine("§f§l"+currentLevel+" / "+requiredLevel);
        return builder.getHandle();
    }

    private void setFunction(int slot, int reqLevel){
        if(reqLevel != -1){
            funcs.put(slot, event->{
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                    Player player = (Player)event.getWhoClicked();
                    try {
                        if(!isRunning.get(slot).get()) {
                            isRunning.get(slot).set(true);
                            int rewardSlot = slot2Slot.inverse().get(slot);
                            if (!rewardsData.isAchieved(rewardSlot).get() && data.getLevel().get() >= reqLevel) {
                                RewardSetting setting = loader.getReward(CosmoIslandsBukkitBootstrap.getInst().getYamlIslandConfig(), slot2Slot.inverse().get(slot)).get();
                                List<ItemStack> list = setting.getRewards();
                                ItemStack[] items = list.toArray(new ItemStack[0]);
                                if (ItemUtils.hasSlots(player.getInventory(), items)) {
                                    rewardsData.setAchieved(rewardSlot, true).get();
                                    player.getInventory().addItem(items);
                                    player.sendMessage("보상을 받았습니다!");
                                    Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), (Runnable)player::closeInventory);
                                } else {
                                    player.sendMessage("보상을 받을 인벤토리 슬롯이 부족합니다.");
                                    Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), (Runnable)player::closeInventory);
                                }
                            }
                            isRunning.get(slot).set(false);
                        }
                    } catch (InterruptedException | ExecutionException e) {

                    }
                });
            });
        }
    }

    private ItemStack getLevelIcon(int level){
        ItemStack item = bottle.clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("현재 레벨: "+level);
        item.setItemMeta(meta);
        return item;
    }

}
