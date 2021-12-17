package kr.cosmoislands.cosmoislands.bukkit.warp;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.warp.IslandLocation;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoisland.cosmoislands.core.IslandTracker;
import kr.cosmoisland.cosmoislands.core.IslandTrackerLoader;
import kr.cosmoisland.cosmoislands.core.packet.IslandStatusChangePacket;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class IslandWarpCommands {

    protected static class User extends BaseCommand{

        @Subcommand("가기")
        public void go(Player player){
            IslandPlayer ip = CosmoIslands.getInst().getIslandPlayer(player.getUniqueId());
            if(ip.getIslandID() == Island.NIL_ID){
                player.sendMessage("섬이 존재하지 않습니다.");
            }else{
                player.sendMessage("섬 이동을 시도합니다.");
                if(!IslandManager.trackIsland(ip.getIslandID()).isLoaded().get()){
                    CosmoIslands.getInst().send(new IslandStatusChangePacket(CosmoIslands.getInst().getProxyName(), ip.getIslandID(), true));
                    CosmoIslands.getInst().getLoadingListener().subscribe(IslandLoadedEvent.class, true, event->{
                        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()-> {
                            try {
                                if (event.getIslandID() == ip.getIslandID()) {
                                    findAndTeleport(player, ip);
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        });
                    });
                }else{
                    findAndTeleport(player, ip);
                }
            }
        }

        @Subcommand("스폰설정")
        public void setSpawn(Player player){
            IslandPlayer owner = island.getPlayersMap().get().getOwner().get();
            if(!owner.getUniqueId().equals(player.getUniqueId())){
                player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                return;
            }
            if(player.isFlying() || player.isSprinting() || player.isInsideVehicle()){
                player.sendMessage("이동 중에는 명령어를 실행할 수 없습니다.");
                return;
            }
            island.getData().get().setSpawnLocation(IslandUtils.convertBukkit(player.getLocation())).get();
            ((IslandComponent)island.getData().get()).sync();
            player.sendMessage("섬 스폰 좌표를 설정했습니다. 이제 /섬 가기를 통해 이동하면, 이곳으로 이동됩니다.");


        }

        @Subcommand("워프")
        public void warp(Player player, @Default("!self") String name){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
                UUID uuid = null;
                if(name.equals("!self")) {
                    uuid = player.getUniqueId();
                }else{
                    OfflinePlayer off = Bukkit.getOfflinePlayer(name);
                    uuid = off.getUniqueId();
                }

                try{
                    IslandWarp warp = CosmoIslands.getInst().getWarp(uuid.toString());
                    IslandLocation loc = warp.getLocation().get();
                    Island island = CosmoIslands.getInst().getIslandManager().getIsland(loc.getIslandID());
                    IslandData data = island.getData().get();
                    if(loc == null) {
                        player.sendMessage("설정 된 워프 지점이 없습니다.");
                        return;
                    }
                    if(!data.isPrivate().get()) {
                        IslandTracker tracker = IslandManager.trackIsland(loc.getIslandID());
                        player.sendMessage("텔레포트를 시도합니다...");
                        if (tracker.isLoaded().get()) {
                            tracker.getLocatedServer().get().ifPresent(server -> {
                                teleporter.teleportLocation(player.getUniqueId(), server, IslandUtils.convertTeleport(loc));
                            });

                        } else {
                            CosmoIslands.getInst().getLoadingListener().subscribe(IslandLoadedEvent.class, true, (event) -> {
                                if (event.getIslandID() == loc.getIslandID())
                                    teleporter.teleportLocation(player.getUniqueId(), event.getExecutedServer(), IslandUtils.convertTeleport(loc));
                            });
                            CosmoIslands.getInst().send(new IslandStatusChangePacket(CosmoIslands.getInst().getProxyName(), loc.getIslandID(), true));
                        }
                    }else{
                        player.sendMessage("섬이 잠구어져 있어서 이동할수 없습니다.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    player.sendMessage("명령어 실행 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
                }

            });
        }

        @Subcommand("워프설정")
        public void setWarp(Player player){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
                UUID uuid = player.getUniqueId();
                IslandWarp warp = CosmoIslands.getInst().getWarp(uuid.toString());
                try {
                    warp.setLocation(IslandUtils.convert(player.getLocation())).get();
                    player.sendMessage("지금 서 있는 지점을 섬 워프로 지정했습니다.");
                } catch (IllegalArgumentException e){
                    player.sendMessage("섬에서만 명령어를 실행시켜 주세요.");
                } catch (ExecutionException | InterruptedException e) {
                    player.sendMessage("여기서는 워프를 설정할수 없습니다.");
                }
            });
        }

        private void findAndTeleport(Player player, IslandPlayer ip) throws InterruptedException, ExecutionException {
            IslandTrackerLoader loader = CosmoIslands.getInst().getDatabase().getLoader(IslandTrackerLoader.class);
            IslandDataLoader dataLoader = CosmoIslands.getInst().getDatabase().getLoader(IslandDataLoader.class);
            IslandTracker status = new IslandTracker(ip.getIslandID(), loader);
            Optional<String> located = status.getLocatedServer().get();
            if (located.isPresent()) {
                teleporter.teleportLocation(player.getUniqueId(), located.get(), IslandUtils.convertLocation("island_" + ip.getIslandID(), dataLoader.getSpawnLocation(ip.getIslandID()).get()));
            } else {
                player.sendMessage("섬 이동에 실패했습니다.");
            }
        }
    }
}
