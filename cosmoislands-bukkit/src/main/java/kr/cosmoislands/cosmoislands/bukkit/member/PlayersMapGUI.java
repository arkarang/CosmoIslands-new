package kr.cosmoislands.cosmoislands.bukkit.member;

import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class PlayersMapGUI extends PlayerListGUI{
    private static final BiConsumer<OfflinePlayer, ItemStack> con = (off, item) ->{
        final ItemMeta metaBefore  = item.getItemMeta();
        metaBefore.setDisplayName("§7§l조회중...");
        item.setItemMeta(metaBefore);
        HelloPlayers.inst().getProxied(off.getUniqueId()).isOnline().thenAccept(online->{
            final ItemMeta meta  = item.getItemMeta();
            if(online)
                meta.setDisplayName("§a§l온라인: "+off.getName());
            else
                meta.setDisplayName("§a§l오프라인: "+off.getName());
            Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()-> item.setItemMeta(meta));
        });
    };


    public PlayersMapGUI(IslandPlayersMap map) throws ExecutionException, InterruptedException {
        super("섬원 목록", get(map), con);

    }

    private static List<UUID> get(IslandPlayersMap map) throws ExecutionException, InterruptedException {
        List<UUID> result = new ArrayList<>();
        Map<IslandPlayer, MemberRank> list = map.getMembers().get();
        IslandPlayer owner = list.entrySet().stream().filter(entry->entry.getValue().equals(MemberRank.OWNER)).findFirst().get().getKey();
        result.add(owner.getUniqueID());
        list.remove(owner);
        result.addAll(list.keySet().stream().map(IslandPlayer::getUniqueID).collect(Collectors.toList()));
        return result;
    }
}
