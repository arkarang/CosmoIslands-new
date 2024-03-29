package kr.cosmoislands.cosmoislands.bank;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class BankChestGUI extends ArkarangGUI {

    private static final ItemStack barrier, air;
    private static final List<Function<ItemStack, Boolean>> invalidItems = new ArrayList<>();

    static{
        barrier = new ItemStack(Material.BARRIER);
        air = new ItemStack(Material.AIR);
        invalidItems.add(item-> item.getType().equals(Material.BARRIER));
    }

    private final BukkitIslandInventory bank;
    private final BukkitExecutor executor;
    private final ReentrantLock lock = new ReentrantLock();

    BankChestGUI(BukkitIslandInventory bank, IslandInventoryView view, BukkitExecutor executor) {
        super(6, "섬 가방");
        this.bank = bank;
        this.executor = executor;
        for(int i = 0 ; i < 54 && i < view.list.size(); i++){
            inv.setItem(i, view.list.get(i));
        }
        for(int i = (getLevel())*9; i < 54; i++){
            inv.setItem(i, barrier);
        }
        for(int i = 0; i < 54; i++){
            cancelled.put(i, false);
            funcs.put(i, event->{
                ItemStack item = event.getCurrentItem();
                if(!isUnlocked(event.getSlot())){
                    event.setCancelled(true);
                }else if(invalid(item)){
                    event.setCancelled(true);
                    if(item != null)
                        item.setAmount(0);
                }
            });
        }
    }

    public List<ItemStack> getContents(){
        final int lv = getLevel();
        List<ItemStack> items = new ArrayList<>(Math.min(lv*9,54));
        for(int i = 0; i < lv*9 && i < 54; i++){
            ItemStack item = inv.getItem(i);
            if(!invalid(item)){
                items.add(item);
            }else{
                items.add(air);
            }
        }
        return items;
    }

    public boolean invalid(ItemStack item){
        if(item != null)
            for (Function<ItemStack, Boolean> valid : invalidItems) {
                if(valid.apply(item))
                    return true;
            }
        return false;
    }

    public void updateInventory(){
        for(int i = 0; i < 54; i++){
            ItemStack item = inv.getItem(i);
            if(item != null && item.getType() != Material.AIR) {
                if (isUnlocked(i) && invalid(item)) {
                    item.setAmount(0);
                }
            }
        }

        for(int i = (getLevel())*9; i < 54; i++){
            ItemStack item = inv.getItem(i);
            if(item == null || item.getType() == Material.AIR){
                inv.setItem(i, barrier);
            }
        }
    }

    public boolean isUnlocked(int slot){
        return slot < (getLevel())*9;
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        if(inv.getViewers().isEmpty()){
            executor.async(()->{
                lock.lock();
                bank.saveInventory().thenRun(lock::unlock);
            });
        }
    }

    @Override
    public void openGUI(Player player) {
        executor.sync(()->{
            updateInventory();
            super.openGUI(player);
        });
    }

    private int getLevel(){
        return bank.getCachedLevel();
    }

}
