package kr.cosmoislands.cosmoislands.bukkit.upgrade;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgrade;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class UpgradeGUI extends ArkarangGUI {

    final String title;
    final IslandUpgradeType type;
    final int maxLevel;
    final int currentLevel;
    final double currentMoney;
    final Map<Integer, IslandUpgradeSettings.PairData> view;
    final IslandUpgrade upgrade;
    final BiFunction<Integer, IslandUpgradeSettings.PairData, ItemStack> itemGenerate;
    final BukkitExecutor executor;

    public UpgradeGUI(String title,
                      IslandUpgradeType type,
                      int maxLevel,
                      Map<Integer, IslandUpgradeSettings.PairData> view,
                      int currentLevel,
                      double currentMoney,
                      IslandUpgrade upgrade,
                      BiFunction<Integer, IslandUpgradeSettings.PairData, ItemStack> itemGenerate,
                      BukkitExecutor executor) {
        super(InventoryType.HOPPER, title);
        this.title = title;
        this.type = type;
        this.maxLevel = maxLevel;
        this.view = view;
        this.upgrade = upgrade;
        this.currentLevel = currentLevel;
        this.currentMoney = currentMoney;
        this.itemGenerate = itemGenerate;
        this.executor = executor;
        init();
    }

    private UpgradeGUI(UpgradeGUI gui, int currentLevel, double currentMoney){
        this(gui.title, gui.type, gui.maxLevel, gui.view, currentLevel, currentMoney, gui.upgrade, gui.itemGenerate, gui.executor);
    }

    private void init(){
        for(int i = 0; i < 5; i++){
            int level = i + 1;
            boolean reached = this.currentLevel >= level;
            ItemStack icon = itemGenerate.apply(level, view.get(level));
            makeEnchantedIfReached(icon, level);
            inv.setItem(i, icon);
            if(!reached){
                funcs.put(i, generateFunction(level));
            }
        }
    }

    private void makeEnchantedIfReached(ItemStack icon, int level){
        if (icon != null && icon.getType() != Material.AIR) {
            if(level <= this.currentLevel){
                ItemMeta meta = icon.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                icon.setItemMeta(meta);
            }
        }
    }

    private boolean validateNextLevel(Player player, int level){
        int nextLevel = currentLevel + 1;
        if(currentLevel >= level){
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1f, 3f);
            player.sendMessage("이미 업그레이드를 완료했습니다.");
            return false;
        }else if(nextLevel < level){
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1f, 3f);
            player.sendMessage("아직 이전 단계의 업그레이드를 마치지 않았습니다.");
            return false;
        }
        return true;
    }

    Consumer<InventoryClickEvent> generateFunction(int level){
        return event -> {
            Player player = (Player)event.getWhoClicked();
            int requiredCost = view.get(level).getCost();

            if(!validateNextLevel(player, level)){
                return;
            }

            if(requiredCost >= currentMoney){
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1f, 3f);
                player.sendMessage("섬에 돈이 부족합니다.");
            }else{
                UpgradeGUI gui = new UpgradeGUI(this, currentLevel+1, currentMoney-requiredCost);
                upgrade.getCondition(type).upgrade().thenAccept(result->{
                    switch (result){
                        case SUCCESSFUL:
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 3f);
                            player.sendMessage("섬을 "+level+"레벨로 강화했습니다! ");
                            break;
                        case ALREADY_REACHED:
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1f, 3f);
                            player.sendMessage("이미 해당 레벨"+(level)+"에 도달했습니다.");
                            break;
                        case NOT_ENOUGH_MONEY:
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1f, 3f);
                            player.sendMessage("섬에 돈에 부족합니다.");
                            break;
                        case REACHED_MAX_LEVEL:
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1f, 3f);
                            player.sendMessage("이미 최대 레벨에 도달했습니다.");
                            break;
                    }
                }).thenRun(()->{
                    //Thread.sleep(1000L);
                    executor.sync(()->{
                        gui.openGUI(player);
                    });
                });
            }
        };
    }

}
