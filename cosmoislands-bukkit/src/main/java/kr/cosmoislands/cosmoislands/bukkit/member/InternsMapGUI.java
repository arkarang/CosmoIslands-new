package kr.cosmoislands.cosmoislands.bukkit.member;

import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class InternsMapGUI extends PlayerListGUI{

    private static BiConsumer<OfflinePlayer, ItemStack> con = (off, item) ->{
        final ItemMeta metaBefore  = item.getItemMeta();
        metaBefore.setDisplayName("§7§l조회중...");
        item.setItemMeta(metaBefore);
        HelloPlayers.inst().getProxied(off.getUniqueId()).isOnline().thenAccept(online->{
            final ItemMeta meta  = item.getItemMeta();
            if(online)
                meta.setDisplayName("§a§l온라인: "+off.getName());
            else
                meta.setDisplayName("§a§l오프라인: "+off.getName());
            Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                item.setItemMeta(meta);
            });
        });
    };


    public InternsMapGUI(Island island) throws ExecutionException, InterruptedException {
        super("알바 목록", get(island), con);

    }

    private static List<UUID> get(Island island) throws ExecutionException, InterruptedException {
        List<UUID> uuid = new ArrayList<>();
        uuid.add(island.getPlayersMap().get().getOwner().get().getUniqueID());
        island.getInternsMap().get().getInterns().get().forEach(id->uuid.add(id.getUniqueID()));
        return uuid;
    }
}
