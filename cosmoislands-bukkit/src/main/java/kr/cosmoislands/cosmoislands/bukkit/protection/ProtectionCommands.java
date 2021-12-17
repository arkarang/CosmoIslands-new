package kr.cosmoislands.cosmoislands.bukkit.protection;

import co.aikar.commands.annotation.Subcommand;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoisland.cosmoislands.core.IslandLocal;
import org.bukkit.entity.Player;

import java.util.List;

public class ProtectionCommands {

    protected static class User{

        @Subcommand("잠금")
        public void lock(Player player){
                    IslandPlayer ip = CosmoIslands.getInst().getIslandPlayer(player.getUniqueId());
                    if(ip.getIslandID() == Island.NIL_ID){
                        player.sendMessage("섬이 존재하지 않습니다.");
                    }else{
                        IslandLocal island = CosmoIslands.getInst().getIslandManager().getIslandLocal(ip.getIslandID());
                        if(island == null){
                            player.sendMessage("섬 안에서만 명령어를 실행할수 있습니다.");
                            return;
                        }
                        IslandPlayer owner = island.getPlayersMap().get().getOwner().get();
                        if(!owner.getUniqueId().equals(player.getUniqueId())){
                            player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                            return;
                        }
                        if(player.getWorld().getName().equals(island.getWorldName())) {
                            IslandData data = island.getData().get();
                            boolean isPrivate = data.isPrivate().get();
                            isPrivate = !isPrivate;

                            data.setPrivate(isPrivate);

                            if (isPrivate) {
                                player.sendMessage("섬을 잠구었습니다.");
                                if (island.getWorld().isPresent()) {
                                    List<Player> players = island.getWorld().get().getPlayers();
                                    CompletableFuture<WarpLocation> spawnFuture = teleporter.getWarpService().getWarp("!spawn");
                                    for (Player p : players) {
                                        IslandPlayer ipp = CosmoIslands.getInst().getIslandPlayer(p);
                                        if(ipp.getIslandID() != island.getID())
                                            spawnFuture.thenAccept(lobby-> teleporter.warpPlayer(ipp.getUniqueId(), lobby));
                                    }
                                }
                            } else {
                                player.sendMessage("섬 잠금을 해제했습니다.");
                            }
                        }else{
                            player.sendMessage("섬에서만 명령어를 사용할수 있습니다.");
                        }

                    }

        }
    }
}
