package kr.cosmoisland.cosmoislands.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoisland.cosmoislands.core.IslandCache;
import kr.cosmoisland.cosmoislands.core.IslandTracker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@CommandAlias("ci")
@CommandPermission("cosmoislands.op")
public class TestCommands extends BaseCommand {

    IslandCache<IslandPlayer> cache;

    TestCommands(IslandCache<IslandPlayer> cache){
        this.cache = cache;
    }

    @Subcommand("ip")
    public void getIslandPlayer(Player player) throws ExecutionException {
        player.sendMessage("섬 이이디: "+cache.get(player.getUniqueId()).getIslandId());
    }

    @Subcommand("track")
    public void track(Player player, int id) throws ExecutionException, InterruptedException {
        IslandTracker tracker = CosmoIslands.trackIsland(id);
        player.sendMessage("섬 로드: "+tracker.getStatus().get());
    }

    @Subcommand("unload")
    public void unload(Player player, int id){
        player.sendMessage("섬 언로드 ㅇㅇ");
        Bukkit.unloadWorld("island_"+id, true);
    }

    @Subcommand("sm")
    public void sendMessage(Player player, String text) throws ExecutionException, InterruptedException {
        IslandPlayer ip = cache.get(player.getUniqueId());
        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
        new WrappedPlayersMap(island.getPlayersMap().get()).sendMessages(text);
    }

    @Subcommand("itemgen")
    public void getItem(Player player){
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("123456789");
        meta.setLore(Arrays.asList("1","2","3"));
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }

}
