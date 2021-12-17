package kr.cosmoislands.cosmoislands.bukkit.upgrade;

import co.aikar.commands.annotation.Subcommand;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class UpgradeCommands {

    protected static class User{

        @Subcommand("강화")
        public void upgrade(Player player){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
                IslandPlayer ip;
                try {
                    ip = CosmoIslands.getInst().getIslandPlayer(player.getUniqueId());
                } catch (ExecutionException e) {
                    player.sendMessage("플레이어 정보 조회 중 오류가 발생했습니다. 관리자에게 문의 해주세요.");
                    return;
                }

                runIslandExists(player, ip, (island)-> runIfIslandLocal(player, island, (local)->{
                    IslandReinforceMainGUI gui = new IslandReinforceMainGUI(local);
                    Bukkit.getScheduler().runTask(CosmoIslands.getInst(), ()->gui.openGUI(player));
                }));

            });
        }

    }

}
