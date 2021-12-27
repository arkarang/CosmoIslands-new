package kr.cosmoislands.cosmoislands.bukkit.upgrade;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UpgradeCommands {

    public static void init(PaperCommandManager manager, BukkitExecutor executor){
        manager.registerCommand(new User(executor));
    }

    public static void init(PaperCommandManager manager, BukkitExecutor executor, Map<IslandUpgradeType, UpgradeMainGUI.IconMap> icons){
        manager.registerCommand(new User(executor, icons));
    }

    @CommandAlias("섬")
    @RequiredArgsConstructor
    protected static class User extends BaseCommand {

        final BukkitExecutor executor;
        final Map<IslandUpgradeType, UpgradeMainGUI.IconMap> iconMaps;

        protected User(BukkitExecutor executor){
            this.executor = executor;
            this.iconMaps = new HashMap<>();
            for (IslandUpgradeType value : IslandUpgradeType.values()) {
                iconMaps.put(value, UpgradeMainGUI.IconMap.singletonIconMap(new ItemStack(Material.GRASS_BLOCK)));
            }
        }

        @Subcommand("강화")
        public void upgrade(Player player){
            final World world = player.getWorld();
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            preconditions.hasIsland()
                    .thenCompose(hasIsland->{
                        if(hasIsland){
                            return preconditions.isOwner(player.getUniqueId());
                        }else{
                            player.sendMessage("당신은 섬에 가입되어 있지 않습니다.");
                            return CompletableFuture.completedFuture(null);
                        }
                    }).thenCompose(isOwner->{
                        if(isOwner != null){
                            if(isOwner){
                                return preconditions.doesInIsland(world);
                            }else{
                                player.sendMessage("당신은 섬장이 아닙니다.");
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    }).thenCompose(inIsland -> {
                        if(inIsland != null){
                            if(inIsland){
                                val executionFuture = preconditions.getIsland().thenAccept(island->{
                                    UpgradeMainGUI gui = new UpgradeMainGUI(iconMaps, island, executor);
                                    executor.sync(()->gui.openGUI(player));
                                });
                            }else{
                                player.sendMessage("섬 안에서만 명령어를 실행할수 있습니다.");
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    });

        }

    }

}
