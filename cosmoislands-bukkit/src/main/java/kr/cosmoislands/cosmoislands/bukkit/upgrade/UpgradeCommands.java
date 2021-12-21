package kr.cosmoislands.cosmoislands.bukkit.upgrade;

import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UpgradeCommands {

    protected static class User{

        BukkitExecutor executor;

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
                                UpgradeMainGUI gui;
                            }else{
                                player.sendMessage("섬 안에서만 명령어를 실행할수 있습니다.");
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    });

        }

    }

}
