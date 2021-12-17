package kr.cosmoislands.cosmoislands.bukkit.upgrade;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.bukkit.config.ReinforceSetting;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public abstract class ReinforceGUI<T> extends ArkarangGUI {

    ReinforceSetting setting;
    HashMap<Integer, Supplier<ItemStack>> icons = new HashMap<>();
    T t;

    public ReinforceGUI(String title, ReinforceSetting setting, T t) {
        super(InventoryType.HOPPER, title);
        this.setting = setting;
        this.t = t;
    }

    protected void init(){
        for(int i = 0; i < Math.min(setting.getMaxLevel(), 5); i++){
            final int lvl = i+1;
            if(icons.containsKey(i)) {
                inv.setItem(i, getItemStack(icons.get(i)));
                funcs.put(i, event -> {
                    try {
                        final int cl = getClickedSlotToLevel(event.getSlot());
                        Player player = (Player)event.getWhoClicked();
                        if(getCurrentLevel(t) >= cl){
                            executeAlreadyReached(player, cl);
                        }
                        if(canLevelUp(t, cl)) {
                            if (hasCost(lvl, t)) {
                                takeAndLevelUp(lvl, t);
                                executeSuccessful(player, cl);
                            } else {
                                executeNotEnoughMoney(player, cl);
                            }
                        }else{
                            executeNotEnoughLevel(player, cl);
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    abstract void executeAlreadyReached(Player player, int cl) throws ExecutionException, InterruptedException;

    abstract int getCurrentLevel(T t) throws ExecutionException, InterruptedException;

    abstract boolean canLevelUp(T t, int cl) throws ExecutionException, InterruptedException;

    abstract boolean hasCost(int lv, T t) throws ExecutionException, InterruptedException;

    abstract void takeAndLevelUp(int lv, T t) throws ExecutionException, InterruptedException;

    abstract void executeNotEnoughMoney(Player player, int cl) throws ExecutionException, InterruptedException;

    abstract void executeNotEnoughLevel(Player player, int cl) throws ExecutionException, InterruptedException;

    abstract void executeSuccessful(Player player, int cl) throws ExecutionException, InterruptedException;

    abstract int getClickedSlotToLevel(int i) throws ExecutionException, InterruptedException;

    double getCost(int lv){
        return setting.getComponent(lv).getCost();
    }

    ItemStack getItemStack(Supplier<ItemStack> sup){
        return sup.get();

    }

    private static final HashMap<Integer, String> orderings = new HashMap<>();

    static{
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

    protected void closeAndSend(HumanEntity entity, String text){
        Player player = (Player)entity;
        Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), (@NotNull Runnable) player::closeInventory);
        player.sendMessage(text);
    }

}


