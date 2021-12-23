package kr.cosmoislands.cosmoislands.bukkit.settings;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.bukkit.IslandPreconditions;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class SettingsCommands {

    public static void init(PaperCommandManager manager, BukkitExecutor executor){
        manager.registerCommand(new User(executor));
    }

    @RequiredArgsConstructor
    @CommandAlias("섬")
    protected static class User extends BaseCommand {

        private final BukkitExecutor executor;

        @Subcommand("설정")
        public void settings(Player player) {
            final World world = player.getWorld();
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());

            preconditions.getIsland().thenApply(island -> {
                if (island == null) {
                    player.sendMessage("소속된 섬이 존재하지 않습니다.");
                    return null;
                }
                try {
                    return IslandPreconditions.of(world);
                } catch (IllegalArgumentException e) {
                    player.sendMessage("자신의 섬 안에서만 명령어를 입력할수 있습니다.");
                    return null;
                }
            }).thenAccept(islandPreconditions -> {
                if (islandPreconditions != null) {
                    islandPreconditions.isOwnerOf(player.getUniqueId()).thenAccept(isOwner->{
                        if (isOwner) {
                            IslandSettingsMap map = islandPreconditions.getIsland().getComponent(IslandSettingsMap.class);
                            map.asMap().thenAccept(view->{
                                executor.sync(() -> {
                                    SettingGUI gui = new SettingGUI(world, map, view, executor);
                                    gui.openGUI(player);
                                });
                            });
                        } else {
                            player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                            return;
                        }
                    });
                }
            });
        }

        @Subcommand("이름")
        public void rename(Player player, String name) {
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());

            preconditions.hasIsland().thenCompose(hasIsland->{
                if(hasIsland){
                    return preconditions.isOwner(player.getUniqueId());
                }else{
                    player.sendMessage("섬이 존재하지 않습니다.");
                    return CompletableFuture.completedFuture(null);
                }
            }).thenCompose(isOwner->{
                if(isOwner){
                    return preconditions.getIsland();
                }else{
                    player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                    return CompletableFuture.completedFuture(null);
                }
            }).thenAccept(island->{
                if(island != null){
                    try {
                        String displayname = name.replace("&", "§");

                        if (displayname.contains("§k") || displayname.contains("§n") || displayname.equals("§o")) {
                            player.sendMessage("해당 이름은 사용할수 없습니다.");
                        }else {
                            IslandSettingsMap settings = island.getComponent(IslandSettingsMap.class);
                            settings.setDisplayname(displayname)
                                    .thenRun(() -> {
                                        player.sendMessage("섬 이름을 " + name.replace("&", "§") + "§f으로 바꾸었습니다!");
                                        settings.sync();
                                    });
                        }
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("올바르지 않은 이름입니다! ");
                    }
                }
            });
        }
    }
}
