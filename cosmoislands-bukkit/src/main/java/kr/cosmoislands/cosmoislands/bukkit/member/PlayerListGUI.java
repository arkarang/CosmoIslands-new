package kr.cosmoislands.cosmoislands.bukkit.member;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.helloplayer.core.HelloPlayers;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class PlayerListGUI extends ArkarangGUI {

    protected static final UUID SYSTEM = new UUID(0, 0);

    private static final ItemStack glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1);
    static final HashMap<Integer, Integer> slots = new HashMap<>();

    static{
        slots.put(0, 13);
        slots.put(1, 20);
        slots.put(2, 22);
        slots.put(3, 24);
        slots.put(4, 29);
        slots.put(5, 31);
        slots.put(6, 33);
        slots.put(7, 38);
        slots.put(8, 40);
        slots.put(9, 42);
    }

    protected final BukkitExecutor executor;

    public PlayerListGUI(String title, List<UUID> players, BukkitExecutor executor) {
        super(6, title);
        this.executor = executor;
        for(int i = 0; i < 6; i++){
            inv.setItem(i*9, glass);
            inv.setItem(i*9+8, glass);
        }
        for(int j = 0; j < 9; j++) {
            inv.setItem(j, glass);
            inv.setItem(45+j, glass);
        }
        for(int i = 0; i < players.size(); i++){
            if(slots.containsKey(i)){
                UUID uuid = players.get(i);
                setLazy(uuid, get(uuid), slots.get(i));
            }
        }
    }

    public ItemStack get(UUID uuid){
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        if(!uuid.equals(SYSTEM)) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            ItemMeta meta = item.getItemMeta();
            SkullMeta sm = (SkullMeta) meta;
            sm.setOwningPlayer(player);
            item.setItemMeta(sm);
        }
        return item;
    }


    void setLazy(UUID uuid, ItemStack item, int slot) {
        final ItemMeta metaBefore  = item.getItemMeta();
        metaBefore.setDisplayName("§7§l조회중...");
        item.setItemMeta(metaBefore);
        if(!uuid.equals(SYSTEM)) {
            val usernameFuture = HelloPlayers.getInst().getUsername(uuid);
            HelloPlayers.inst().getProxied(uuid).isOnline().thenCombine(usernameFuture, (online, username) -> {
                executor.sync(() -> {
                    final ItemMeta meta = item.getItemMeta();
                    if (online)
                        meta.setDisplayName("§a§l온라인: " + username);
                    else
                        meta.setDisplayName("§a§l오프라인: " + username);
                    item.setItemMeta(meta);
                    inv.setItem(slot, item);
                });
                return null;
            });
        }else{
            metaBefore.setDisplayName("§a§l알수 없음");
            item.setItemMeta(metaBefore);
        }
    }
}
