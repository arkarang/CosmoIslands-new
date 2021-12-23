package kr.cosmoislands.cosmoislands.bukkit.upgrade;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.bank.IslandVault;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgrade;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeCondition;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class UpgradeMainGUI extends ArkarangGUI {

    private static final HashMap<Integer, String> orderings = new HashMap<>();
    private static final ItemStack[] glass = new ItemStack[3];
    private static final ItemStack size, member, chest, intern;

    static{
        glass[0] = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
        glass[1] = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
        glass[2] = new ItemStack(Material.BROWN_STAINED_GLASS_PANE, 1);

        size = new ItemStackBuilder(new ItemStack(Material.GRASS)).setName("§f§l[§2§l섬 크기 §f§l]").getHandle();
        member = new ItemStackBuilder(new ItemStack(Material.IRON_HELMET)).setName("§f§l[§3§l섬 인원 §f§l]").getHandle();
        chest = new ItemStackBuilder(new ItemStack(Material.CHEST)).setName("§f§l[§6§l섬 창고 §f§l]").getHandle();
        intern = new ItemStackBuilder(new ItemStack(Material.TOTEM_OF_UNDYING)).setName("§f§l[§e§l섬 알바 인원 §f§l]").getHandle();

        orderings.put(0, "영");
        orderings.put(1, "하나");
        orderings.put(2, "둘");
        orderings.put(3, "셋");
        orderings.put(4, "넷");
        orderings.put(5, "다섯");
        orderings.put(6, "여섯");

    }
    protected static String getKoreanOrdering(int i){
        return orderings.getOrDefault(i, i+"");
    }


    @RequiredArgsConstructor
    public static class IconMap{
        final Map<Integer, ItemStack> icons;

        public ItemStack getIcon(int level){
            return icons.get(level);
        }

        public static IconMap singletonIconMap(ItemStack item){
            return new IconMap(null){
                @Override
                public ItemStack getIcon(int level) {
                    return item;
                }
            };
        }
    }

    private final BiFunction<Integer, IslandUpgradeSettings.PairData, ItemStack>
            itemSize, itemMember, itemChest, itemIntern;
    private final BukkitExecutor executor;

    public UpgradeMainGUI(Map<IslandUpgradeType, IconMap> icons, Island island, BukkitExecutor executor) {
        super(3, "§f§l[§a§l섬 강화 §f§l]");
        this.executor = executor;

        itemSize = (level, pairData) ->{
            return new ItemStackBuilder(icons.get(IslandUpgradeType.BORDER_SIZE).getIcon(level))
                    .setName("섬 최대 사이즈 : " + pairData.getValue() + "블럭")
                    .addLine("§a§l 가격 §f: §e" + pairData.getCost() + "G")
                    .getHandle();

        };
        itemMember = (level, pairData) ->{
            return new ItemStackBuilder(icons.get(IslandUpgradeType.MAX_PLAYERS).getIcon(level))
                    .setName("섬 최대 인원 : " + pairData.getValue() + "명")
                    .addLine("§a§l 가격 §f: §e" + pairData.getCost() + "G")
                    .getHandle();

        };
        itemChest = (level, pairData) ->{
            return new ItemStackBuilder(icons.get(IslandUpgradeType.INVENTORY_SIZE).getIcon(level))
                    .setName("섬 창고 " + getKoreanOrdering(pairData.getValue()) + "줄")
                    .addLine("§a§l 가격 §f: §e" + pairData.getCost() + "G")
                    .getHandle();
        };
        itemIntern = (level, pairData) ->{
            return new ItemStackBuilder(icons.get(IslandUpgradeType.MAX_INTERNS).getIcon(level))
                    .setName("섬 최대 알바 : " + pairData.getValue() + "명")
                    .addLine("§a§l 가격 §f: §e" + pairData.getCost() + "G")
                    .getHandle();
        };

        for(int i = 0 ; i < 3; i++){
            for(int j =0; j < 9; j++){
                inv.setItem(i*9+j, glass[i]);
            }
        }

        inv.setItem(10, size);
        inv.setItem(12, member);
        inv.setItem(14, chest);
        inv.setItem(16, intern);

        funcs.put(10, event->{
            Player player = (Player)event.getWhoClicked();
            player.closeInventory();
            builder(IslandUpgradeType.BORDER_SIZE, island, "섬 강화: 섬 최대 사이즈")
                    .thenAccept(gui-> executor.sync(()->gui.openGUI(player)));
        });
        funcs.put(12, event->{
            Player player = (Player)event.getWhoClicked();
            player.closeInventory();
            builder(IslandUpgradeType.MAX_PLAYERS, island, "섬 강화: 최대 인원")
                    .thenAccept(gui-> executor.sync(()->gui.openGUI(player)));
        });
        funcs.put(14, event->{
            Player player = (Player)event.getWhoClicked();
            player.closeInventory();
            builder(IslandUpgradeType.INVENTORY_SIZE, island, "섬 강화: 창고")
                    .thenAccept(gui-> executor.sync(()->gui.openGUI(player)));
        });
        funcs.put(16, event->{
            Player player = (Player)event.getWhoClicked();
            player.closeInventory();
            builder(IslandUpgradeType.INVENTORY_SIZE, island, "섬 강화: 최대 알바원")
                    .thenAccept(gui-> executor.sync(()->gui.openGUI(player)));
        });
    }

    private CompletableFuture<UpgradeGUI> builder(IslandUpgradeType type, Island island, String title){
        IslandUpgrade upgrade = island.getComponent(IslandUpgrade.class);
        IslandVault vault = island.getComponent(IslandVault.class);

        IslandUpgradeCondition condition = upgrade.getCondition(type);
        val currentLevelFuture = upgrade.getLevel(type);
        val currentMoneyFuture = vault.getMoney();
        val maxLevelFuture = condition.getSettings()
                .thenApply(IslandUpgradeSettings::getMaxLevel);
        val viewFuture = condition.getSettings()
                .thenApply(IslandUpgradeSettings::asMap);
        BiFunction<Integer, IslandUpgradeSettings.PairData, ItemStack> func = null;


        switch (type){
            case BORDER_SIZE:
                func = itemSize;
                break;
            case MAX_INTERNS:
                func = itemIntern;
                break;
            case MAX_PLAYERS:
                func = itemMember;
                break;
            case INVENTORY_SIZE:
                func = itemChest;
                break;
        }

        final BiFunction<Integer, IslandUpgradeSettings.PairData, ItemStack> finalFunc = func;
        return CompletableFuture.allOf(currentLevelFuture, currentMoneyFuture, maxLevelFuture, viewFuture)
                .thenApply(ignored->{
                    try{
                        int currentLevel = currentLevelFuture.get();
                        double currentMoney = currentMoneyFuture.get();
                        int maxLevel = maxLevelFuture.get();
                        Map<Integer, IslandUpgradeSettings.PairData> view = viewFuture.get();
                        return new UpgradeGUI(title, type, maxLevel, view, currentLevel, currentMoney, upgrade, finalFunc, executor);
                    }catch (Exception e){
                        return null;
                    }
                });
    }

}
