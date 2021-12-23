package kr.cosmoislands.cosmoislands.bukkit.level;

import com.google.common.collect.HashBiMap;
import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import kr.cosmoislands.cosmoislands.api.level.IslandAchievements;
import kr.cosmoislands.cosmoislands.api.level.IslandLevel;
import kr.cosmoislands.cosmoislands.api.level.IslandRewardData;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AchievementGUI extends ArkarangGUI {

    private static final ItemStack yellow, white, bottle;
    private static HashMap<Integer, Integer> map = new HashMap<>();
    private static final HashBiMap<Integer, Integer> ID_TO_SLOT = HashBiMap.create();

    static{
        yellow = new ItemStackBuilder(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        white = new ItemStackBuilder(new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ID_TO_SLOT.put(0, 19);
        ID_TO_SLOT.put(1, 29);
        ID_TO_SLOT.put(2, 21);
        ID_TO_SLOT.put(3, 31);
        ID_TO_SLOT.put(4, 23);
        ID_TO_SLOT.put(5, 33);
        ID_TO_SLOT.put(6, 25);
    }

    HashMap<Integer, AtomicBoolean> locks = new HashMap<>();
    Map<Integer, IslandRewardData> rewardDataView;
    Map<Integer, Boolean> achievementView;
    int currentLevel;
    IslandAchievements achievements;
    IslandLevel level;
    BukkitExecutor executor;

    public AchievementGUI(Map<Integer, IslandRewardData> rewardDataView,
                          Map<Integer, Boolean> achievementView,
                          int currentLevel,
                          IslandAchievements achievements,
                          IslandLevel level,
                          BukkitExecutor executor) {
        super(6, "섬 보상");
        
        this.rewardDataView = new HashMap<>(rewardDataView);
        this.achievementView = new HashMap<>(achievementView);
        this.currentLevel = currentLevel;
        this.achievements = achievements;
        this.level = level;
        this.executor = executor;
        
        for(int i = 0; i < 9; i++){
            inv.setItem(i, yellow);
            inv.setItem(i+45, yellow);
            for(int j = 1; j < 5; j++){
                inv.setItem(i+j*9, white);
            }
        }

        inv.setItem(4, getLevelIcon(currentLevel));
        for(int i = 0; i < ID_TO_SLOT.size(); i++){
            int slot = ID_TO_SLOT.getOrDefault(i, -1);
            if(slot != -1) {
                locks.put(slot, new AtomicBoolean(false));
                inv.setItem(slot, getRewardIcon(i, map.get(i), currentLevel, achievementView.get(i)));
                setFunction(slot, rewardDataView.get(i).getRequiredLevel());
            }
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
                Player player = (Player)event.getWhoClicked();
                if(!locks.get(slot).get()) {
                    locks.get(slot).set(true);
                    int rewardId = ID_TO_SLOT.inverse().get(slot);
                    if (!achievementView.get(rewardId) && currentLevel >= reqLevel) {
                        IslandRewardData data = rewardDataView.get(rewardId);
                        try{
                            CompletableFuture<Integer> levelFuture = level.getLevel();
                            achievements.isAchieved(rewardId).thenCombine(levelFuture, (isAchieved, levelValue)->{
                                if(!isAchieved && levelValue >= reqLevel){
                                    achievements.setAchieved(rewardId, true);
                                    executor.sync(()->{
                                        data.provide(player.getUniqueId());
                                        player.sendMessage("보상을 받았습니다!");
                                        executor.sync(()->(Runnable)player::closeInventory);
                                    });
                                }
                                locks.get(slot).set(false);
                                return null;
                            });
                        }catch (IllegalStateException e){
                            player.sendMessage("보상을 받을 인벤토리 슬롯이 부족합니다.");
                            executor.sync(()->(Runnable)player::closeInventory);
                            locks.get(slot).set(false);
                        }
                    }
                }
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
