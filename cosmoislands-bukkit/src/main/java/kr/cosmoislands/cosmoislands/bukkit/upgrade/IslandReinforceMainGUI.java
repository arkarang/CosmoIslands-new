package kr.cosmoislands.cosmoislands.bukkit.upgrade;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.bukkit.island.IslandLocal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ExecutionException;

public class IslandReinforceMainGUI extends ArkarangGUI {
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
    }

    public IslandReinforceMainGUI(IslandLocal island) {
        super(3, "§f§l[§a§l섬 강화 §f§l]");

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
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                try {
                    ArkarangGUI gui = new IslandReinforceSizeGUI(CosmoIslandsBukkitBootstrap.getInst().getYamlIslandConfig().getReinforceSetting("size"), island.getBank().get(), island.getData().get());
                    Player player = (Player)event.getWhoClicked();
                    Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()-> gui.openGUI(player));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
        funcs.put(12, event->{
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                try {
                    ArkarangGUI gui = new IslandReinforceMembersGUI(CosmoIslandsBukkitBootstrap.getInst().getYamlIslandConfig().getReinforceSetting("members"), island.getBank().get(), island.getData().get());
                    Player player = (Player)event.getWhoClicked();
                    Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()-> gui.openGUI(player));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
        funcs.put(14, event->{
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                try {
                    ArkarangGUI gui = new IslandReinforceChestGUI(CosmoIslandsBukkitBootstrap.getInst().getYamlIslandConfig().getReinforceSetting("chest"), island.getBank().get());
                    Player player = (Player)event.getWhoClicked();
                    Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()-> gui.openGUI(player));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
        funcs.put(16, event->{
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                try {
                    ArkarangGUI gui = new IslandReinforceInternsGUI(CosmoIslandsBukkitBootstrap.getInst().getYamlIslandConfig().getReinforceSetting("interns"), island.getBank().get(), island.getData().get());
                    Player player = (Player)event.getWhoClicked();
                    Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()-> gui.openGUI(player));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
