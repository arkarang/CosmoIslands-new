package kr.cosmoislands.cosmoislands.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.core.helper.CosmoChatHelper;
import kr.cosmoislands.cosmoislands.api.*;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@CommandAlias("ci|cit|kieet")
@CommandPermission("cosmoisland.admin")
@RequiredArgsConstructor
public class TestCommands extends BaseCommand {

    private final IslandService service;
    private final CosmoChatHelper helper;
    private final BukkitExecutor executor;

    @Subcommand("pl")
    public void player(CommandSender sender, String username){
        executor.async(()->{
            try {
                OfflinePlayer off = Bukkit.getOfflinePlayer(username);
                IslandPlayer ip = service.getPlayerRegistry().get(off.getUniqueId());
                int islandId = service.getPlayerRegistry().getIslandId(off.getUniqueId()).get();
                sender.sendMessage("플레이어 uid: " + off.getUniqueId());
                sender.sendMessage("플레이어 섬 id: " + islandId);
                Island island = service.getRegistry().getIsland(islandId).get();
                MemberRank rank = null;
                if(island != null) {
                    rank = island
                            .getComponent(IslandPlayersMap.class)
                            .getRank(ip)
                            .get();
                }
                sender.sendMessage("플레이어 섬 랭크: " + (rank == null ? "null" : rank.name()));
            }catch (InterruptedException | ExecutionException e){

            }
        });
    }

    @Subcommand("ip")
    public void ip(Player player){
        player(player, player.getName());
    }

    @Subcommand("is")
    public void island(CommandSender sender, int islandId){
        executor.async(()->{
            try {
                Island island = service.getRegistry().getIsland(islandId).get();
                if(island != null) {
                    IslandStatus status = island.getStatus().get();
                    IslandServer server = island.getLocated().get();
                    sender.sendMessage("섬 id: "+island.getId());
                    sender.sendMessage("섬 유형: "+(island.isLocal()?"local":"remoted"));
                    sender.sendMessage("섬 상태: "+status.name());
                    sender.sendMessage("적용된 컴포넌트 목록: ");
                    for (Map.Entry<Class<? extends IslandComponent>, ? extends IslandComponent> entry : island.getComponents().entrySet()) {
                        sender.sendMessage(" - "+entry.getKey().getSimpleName()+", "+entry.getValue().getClass().getSimpleName());
                    }
                    sender.sendMessage("섬 서버: "+server.getType().name()+":"+server.getName());
                }else {
                    sender.sendMessage("해당하는 섬이 존재하지 않습니다.");
                }
            }catch (InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
        });
    }

    @Subcommand("sv")
    public void islandServer(CommandSender sender){
        executor.async(()->{
            try {
                List<IslandServer> servers = service.getCloud().getIslandServers().get();
                int i = 1;
                for (IslandServer server : servers) {
                    sender.sendMessage(i+". 서버 이름: "+server.getName()+", 타입: "+server.getType()+", 온라인: "+server.isOnline().get()+", 로드 수: "+server.getLoadedCount().get());
                    i++;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Subcommand("sys")
    public void system(CommandSender sender, String username){
        val future1 = HelloPlayers.getInst().getUniqueID(username);
        val future2 = future1.thenCompose(uuid->{
            return HelloPlayers.getInst().getUsername(uuid);
        });
        future2.thenCombine(future1, (user, uuid)->{
            helper.system(uuid).send("당신의 닉네임은 "+user+" 입니다.");
            return null;
        });
    }
}
