package kr.cosmoislands.cosmoislands.bukkit.utils;

import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.generic.IslandSettings;
import kr.cosmoisland.cosmoislands.api.generic.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.api.level.IslandLevel;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.points.IslandPoints;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class IslandIcon {

    private static ExecutorService executors = Executors.newSingleThreadExecutor();
    private static HelloPlayers playerModule;
    private final Island island;

    public static synchronized void setPlayerModule(HelloPlayers playerModule){
        if(IslandIcon.playerModule == null){
            IslandIcon.playerModule = playerModule;
        }
    }

    public CompletableFuture<ItemStack> provide(){
        IslandSettingsMap settings = island.getComponent(IslandSettingsMap.class);
        IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
        IslandLevel level = island.getComponent(IslandLevel.class);
        IslandPoints points = island.getComponent(IslandPoints.class);
        val displaynameFuture = settings.getDisplayname();
        val playersListFuture = playersMap.getMembers();
        val levelValueFuture = level.getLevel();
        val pointValueFuture = points.getPoints();
        val maxPlayersFuture = playersMap.getMaxPlayers();
        val playerNamesFuture = playersListFuture
                .thenApply(map -> {
                    try{
                        Map<UUID, CompletableFuture<String>> usernameFutures = new HashMap<>();
                        for (Map.Entry<UUID, MemberRank> entry : map.entrySet()) {
                            if(entry.getValue().getPriority() >= MemberRank.MEMBER.getPriority()) {
                                val usernameFuture = playerModule.getUsername(entry.getKey());
                                usernameFutures.put(entry.getKey(), usernameFuture);
                            }
                        }

                        CompletableFuture.allOf(usernameFutures.values().toArray(new CompletableFuture[0])).get();

                        Map<Integer, List<String>> sortedMap = new HashMap<>();
                        for (Map.Entry<UUID, CompletableFuture<String>> entry : usernameFutures.entrySet()) {
                            MemberRank rank = map.get(entry.getKey());
                            int priority = rank.getPriority();

                            if(!sortedMap.containsKey(priority)){
                                sortedMap.put(priority, new ArrayList<>());
                            }

                            sortedMap.get(priority).add(entry.getValue().get());
                        }

                        List<String> results = new ArrayList<>();
                        for(int i = MemberRank.OWNER.getPriority(); i >= MemberRank.MEMBER.getPriority(); i--){
                            List<String> names = sortedMap.get(i);

                            if(names != null)
                                results.addAll(names);
                        }

                        return results;
                    }catch (ExecutionException | InterruptedException ignored){
                        return null;
                    }
                });

        return CompletableFuture.supplyAsync(()->{
            String displayname;
            List<String> usernames;
            int levelValue;
            int pointValue;
            int maxPlayers;

            try {
                try {
                    usernames = playerNamesFuture.get(3000L, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    usernames = new ArrayList<>(Collections.singletonList("알수 없음"));
                }
                displayname = displaynameFuture.get();
                levelValue = levelValueFuture.get();
                pointValue = pointValueFuture.get();
                maxPlayers = maxPlayersFuture.get();
                IconData data = new IconData(displayname, usernames, levelValue, pointValue, maxPlayers);
                return getInfoIcon(data);
            }catch (ExecutionException | InterruptedException e){
                return null;
            }
        }, executors);
    }

    @Data
    @RequiredArgsConstructor
    static class IconData{
        final String displayname;
        final List<String> usernames;
        final int level;
        final int points;
        final int maxPlayers;
    }

    public static IslandIcon of(Island island){
        return new IslandIcon(island);
    }

    private ItemStack getInfoIcon(IconData data) throws ExecutionException, InterruptedException {
        ItemStack item = new ItemStack(Material.GRASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§a§l섬 정보");
        List<String> lore = new ArrayList<>(4);
        lore.add("§a§l섬 §f§l이름 §f: " + Optional.ofNullable(data.displayname).orElse("이름없는 섬"));
        lore.add("§a§l"+ String.join(", ", data.usernames.toArray(new String[0])));
        lore.add("§a§l섬 §e§l섬 최대 인원 수 §f: " + data.maxPlayers);
        lore.add("§a§l섬 §b§l레벨 §f§l: " + data.level);
        lore.add("§a§l섬 §c§l인기도 §f§l:§f§l "+data.points);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

}
