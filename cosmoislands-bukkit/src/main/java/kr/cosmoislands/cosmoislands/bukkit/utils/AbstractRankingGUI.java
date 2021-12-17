package kr.cosmoislands.cosmoislands.bukkit.utils;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandRanking;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.level.IslandLevel;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.points.IslandPoints;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AbstractRankingGUI extends ArkarangGUI {

    @RequiredArgsConstructor
    public static class Factory{

        private final HelloPlayers playersModule;
        private final RankingComponents components;
        private final IslandRegistry islandRegistry;
        private final Function<Island, CompletableFuture<Integer>> function;

        public CompletableFuture<AbstractRankingGUI> build(List<IslandRanking.RankingData> list){
            CompletableFuture<AbstractRankingGUI> result = new CompletableFuture<>();
            List<CompletableFuture<RankingIcon>> futures = new ArrayList<>();

            list.forEach(data->futures.add(toIcon(data)));

            try {
                List<RankingIcon> icons = new ArrayList<>();
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
                for (CompletableFuture<RankingIcon> future : futures) {
                    icons.add(future.get());
                }
                result.complete(new AbstractRankingGUI(components, icons));
            }catch (Exception e){
                result.completeExceptionally(e);
            }
            return result;
        }

        private CompletableFuture<RankingIcon> toIcon(IslandRanking.RankingData data){
            Island island = islandRegistry.getIsland(data.getIslandId());
            IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
            val ownerFuture = playersMap.getOwner();
            val valueFuture = function.apply(island);
            val nameFuture = ownerFuture.thenCompose(islandPlayer->playersModule.getUsername(islandPlayer.getUniqueId()));
            return nameFuture.thenCombine(valueFuture, RankingIcon::new);
        }
    }

    @Data
    public static class RankingIcon {
        final String owner;
        final int value;
    }

    public static abstract class RankingComponents {

        public abstract String getTitle();

        public abstract String formatData(int value);
    }


    public static final ItemStack glass;
    public static final int[] slots = new int[3];
    public static final ItemStack[] places = new ItemStack[3];

    static{
        glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1);
        places[0] = new ItemStackBuilder(Material.DIAMOND_BLOCK).setName("§b§l1§f§l등").getHandle();
        places[1] = new ItemStackBuilder(Material.GOLD_BLOCK).setName("§b§l2§f§l등").getHandle();
        places[2] = new ItemStackBuilder(Material.IRON_BLOCK).setName("§b§l3§f§l등").getHandle();
        slots[0] = 2;
        slots[1] = 4;
        slots[2] = 6;
    }

    public final RankingComponents components;

    private AbstractRankingGUI(RankingComponents components, List<RankingIcon> icons) {
        super(4, components.getTitle());
        this.components = components;
        for(int i = 0 ; i < 36; i++){
            inv.setItem(i, glass);
        }
        for(int i = 0 ; i < 3 && i < icons.size(); i++){
            inv.setItem(9+slots[i], places[i]);
            RankingIcon icon = null;
            inv.setItem(18+slots[i], getIcon(icons.get(i)));
        }

    }

    private ItemStack getIcon(RankingIcon data){
        OfflinePlayer player = Bukkit.getOfflinePlayer(data.getOwner());
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);
        return new ItemStackBuilder(item).setName("§a§l섬 §f§l이름 §f: "+ player.getName()+"님의 섬")
                .addLine(components.formatData(data.getValue())).getHandle();
    }

}
