package kr.cosmoislands.cosmoislands.bukkit.warp;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.AbstractLocation;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.api.warp.*;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import kr.cosmoislands.cosmoislands.bukkit.utils.IslandUtils;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import kr.cosmoislands.cosmoislands.warp.IslandWarpModule;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class IslandWarpCommands {

    public static void init(PaperCommandManager manager, IslandService service, IslandWarpModule module, BukkitExecutor executor){
        manager.registerCommand(new User(service.getPlayerRegistry(), module.getTeleportExecutor(), executor));
    }

    @CommandAlias("섬")
    @RequiredArgsConstructor
    protected static class User extends BaseCommand{

        private final IslandPlayerRegistry playerRegistry;
        private final TeleportExecutor teleportExecutor;
        private final BukkitExecutor executor;

        @Subcommand("가기")
        public void go(Player player){
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            preconditions.getIsland().thenCompose(island->{
                if(island == null){
                    player.sendMessage("섬이 존재하지 않습니다.");
                    return null;
                }else{
                    return island.getComponent(IslandWarpsMap.class).getSpawnLocation();
                }
            }).thenCombine(preconditions.isPlayer(player.getUniqueId()), (warp, isMember) -> {
                if(warp != null){
                    DebugLogger.log("island go warp: "+warp);
                    player.sendMessage("섬 이동을 시도합니다.");
                    return teleportExecutor.teleportPlayer(player.getUniqueId(), warp);
                }else{
                    return CompletableFuture.completedFuture((WarpResult)null);
                }
            }).thenCompose(future->future).thenAccept(warpResult -> {
                if(warpResult != null){
                    if(!warpResult.isSuccess()){
                        player.sendMessage("오류: 섬이 로드되어 있지 않습니다.");
                    }else{
                        DebugLogger.log("island go failed - 1");
                    }
                }
                DebugLogger.log("island go failed - 2");
            });
        }

        @Subcommand("스폰설정")
        public void setSpawn(Player player){
            if(player.isFlying() || player.isSprinting() || player.isInsideVehicle()){
                player.sendMessage("날거나, 이동 중에는 명령어를 실행할 수 없습니다.");
                return;
            }

            final Location bukkitLocation = player.getLocation();

            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            preconditions.getIsland().thenApply(island->{
                if(island == null){
                    player.sendMessage("섬이 존재하지 않습니다.");
                    return null;
                }else{
                    return island.getComponent(IslandWarpsMap.class);
                }
            }).thenCombine(preconditions.hasRank(player.getUniqueId(), MemberRank.OWNER), (warpsMap, hasRank)->{
                if(warpsMap != null){
                    if(hasRank){
                        AbstractLocation location = IslandUtils.convertBukkit(bukkitLocation);
                        warpsMap.setSpawnLocation(location).thenRun(()->{
                            player.sendMessage("섬 스폰 좌표를 설정했습니다. 이제 /섬 가기를 통해 이동하면, 이곳으로 이동됩니다.");
                        });
                    }else{
                        player.sendMessage("오류: 섬장만 입력할수 있습니다.");
                    }
                }
                return null;
            });
        }

        @Subcommand("워프")
        public void warp(Player player, String name){
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            preconditions.getIsland().thenCompose(island -> {
                if(island == null){
                    player.sendMessage("당신은 섬이 존재하지 않습니다.");
                    return CompletableFuture.completedFuture(null);
                }else{
                    IslandWarpsMap map = island.getComponent(IslandWarpsMap.class);
                    return map.getWarp(name).thenApply(warp->new IslandLocation(island.getId(), warp));
                }
            }).thenCompose(warp -> {
                if(warp != null){
                    player.sendMessage("워프를 시도합니다...");
                    return teleportExecutor.teleportPlayer(player.getUniqueId(), warp);
                }else
                    return CompletableFuture.completedFuture(null);
            }).thenAccept(warpResult -> {
                if(warpResult != null){
                    if(!warpResult.isSuccess()){
                        player.sendMessage("오류: 섬이 로드되어 있지 않습니다.");
                    }
                }
            });
        }

        @Subcommand("워프설정")
        public void setWarp(Player player, String name){
            PlayerPreconditions playerPreconditions = PlayerPreconditions.of(player.getUniqueId());
            final World world = player.getWorld();
            final Location bukkitLocation = player.getLocation();

            playerPreconditions.hasIsland().thenCompose(hasIsland->{
                if(hasIsland){
                    return playerPreconditions.doesInIsland(world);
                }else{
                    player.sendMessage("당신은 섬이 존재하지 않습니다.");
                }
                return CompletableFuture.completedFuture(null);
            }).thenCompose(inIsland -> {
                if(inIsland != null){
                    if(inIsland){
                        return playerPreconditions.isOwner(player.getUniqueId());
                    }else{
                        player.sendMessage("오류: 이 명령어는 자신의 섬 안에서만 입력할수 있습니다.");
                    }
                }
                return CompletableFuture.completedFuture(null);
            }).thenAccept(isOwner -> {
                if(isOwner != null){
                    if(isOwner){
                        playerPreconditions.getIsland().thenAccept(island -> {
                            IslandWarpsMap warps = island.getComponent(IslandWarpsMap.class);
                            AbstractLocation location = IslandUtils.convert(bukkitLocation);
                            IslandWarp warp = new IslandWarp(name, MemberRank.MEMBER, location);
                            warps.insertWarp(warp).handle((result, exception) -> {
                                if(exception != null){
                                    player.sendMessage("오류: 추가 가능한 최대 워프 개수를 초과했습니다.");
                                }else{
                                    player.sendMessage("지금 서 있는 지점을 "+name+"의 워프 지점으로 지정했습니다.");
                                }
                                return null;
                            });
                        });
                    }else{
                        player.sendMessage("오류: 섬장만 입력할수 있습니다.");
                    }
                }
            });
        }

        @Subcommand("워프목록")
        public void warpList(Player player){
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            final World world = player.getWorld();
            preconditions.getIsland().thenAccept(island -> {
                if(island == null){
                    player.sendMessage("당신은 섬이 존재하지 않습니다.");
                }else{
                    IslandPlayer islandPlayer = playerRegistry.get(player.getUniqueId());
                    CompletableFuture<MemberRank> rankFuture = island.getComponent(IslandPlayersMap.class).getRank(islandPlayer);
                    IslandWarpsMap map = island.getComponent(IslandWarpsMap.class);
                    rankFuture.thenCompose(map::getWarps).thenAccept(warps -> {
                        executor.sync(()->{
                            WarpListGUI gui = new WarpListGUI(island, teleportExecutor, warps);
                            gui.openGUI(player);
                        });
                    });
                }
            });
        }
    }
}
