package kr.cosmoislands.cosmoislands.bukkit.protection;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.protection.IslandProtection;
import kr.cosmoislands.cosmoislands.api.world.IslandWorld;
import kr.cosmoislands.cosmoislands.api.world.IslandWorldHandler;
import kr.cosmoislands.cosmoislands.bukkit.IslandPreconditions;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProtectionCommands {

    public static void init(PaperCommandManager manager){
        manager.registerCommand(new User());
    }

    @CommandAlias("섬")
    @RequiredArgsConstructor
    protected static class User extends BaseCommand {

        @Subcommand("잠금")
        public void lock(Player player){
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
                            IslandProtection protection = islandPreconditions.getIsland().getComponent(IslandProtection.class);
                            IslandWorld islandWorld = islandPreconditions.getIsland().getComponent(IslandWorld.class);
                            IslandWorldHandler worldHandler = islandWorld.getWorldHandler();

                            protection.isPrivate().thenAccept(isPrivate->{
                                final boolean lockIsland = !isPrivate;

                                protection.setPrivate(lockIsland).thenRun(()->{
                                    DebugLogger.log("command lock:4" + isPrivate);
                                    if(lockIsland){
                                        player.sendMessage("섬 잠금을 해제했습니다.");
                                    }else{
                                        player.sendMessage("섬을 잠구었습니다.");
                                    }
                                });
                            });
                        } else {
                            player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                        }
                    });
                }
            });
        }
    }
}
