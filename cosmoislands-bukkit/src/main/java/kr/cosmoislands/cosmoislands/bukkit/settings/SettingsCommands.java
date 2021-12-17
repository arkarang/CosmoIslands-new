package kr.cosmoislands.cosmoislands.bukkit.settings;

import co.aikar.commands.annotation.Subcommand;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SettingsCommands {

    protected static class User {

        @Subcommand("설정")
        public void settings(Player player) {
            IslandPlayer owner = island.getPlayersMap().get().getOwner().get();
            if (!owner.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                return;
            }
            SettingGUI gui = new SettingGUI(i);
            Bukkit.getScheduler().runTask(CosmoIslands.getInst(), () -> gui.openGUI(player));
        }

        @Subcommand("이름")
        public void rename(Player player, String name) {
            if (ip.getIslandID() == Island.NIL_ID) {
                player.sendMessage("섬이 존재하지 않습니다.");
                return;
            }
            Island island = CosmoIslands.getInst().getIslandManager().getIsland(ip.getIslandID());
            IslandPlayer owner = island.getPlayersMap().get().getOwner().get();
            if (!owner.getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                return;
            }
            IslandData data = island.getData().get();
            Future<Void> execution;
            try {
                String displayname = name.replace("&", "§");
                if (displayname.contains("§k") || displayname.contains("§n") || displayname.equals("§o")) {
                    player.sendMessage("해당 이름은 사용할수 없습니다.");
                    return;
                }
                execution = data.setDisplayname(name.replace("&", "§"));
            } catch (IllegalArgumentException e) {
                player.sendMessage("올바르지 않은 이름입니다! ");
                return;
            }
            if (execution != null) {
                execution.get();
                ((IslandComponent) data).sync();
                player.sendMessage("섬 이름을 " + name.replace("&", "§") + "§f으로 바꾸었습니다!");
            } else {
                player.sendMessage("명령어 실행 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
            }
        }
    }
}
