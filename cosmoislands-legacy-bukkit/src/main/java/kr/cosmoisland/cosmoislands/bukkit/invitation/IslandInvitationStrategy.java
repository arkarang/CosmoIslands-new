package kr.cosmoisland.cosmoislands.bukkit.invitation;

import com.minepalm.arkarangutils.invitation.Invitation;
import com.minepalm.arkarangutils.invitation.InvitationExecuteStrategy;
import com.minepalm.arkarangutils.invitation.exception.InvitationTimeoutException;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoisland.cosmoislands.bukkit.database.IslandInvitationLoader;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class IslandInvitationStrategy extends InvitationExecuteStrategy {

    final CosmoIslandsBukkitBootstrap plugin;
    final CosmoIslands manager;
    final HelloPlayers players;
    final IslandInvitationLoader loader;

    @Override
    public void onInvited(Invitation invitation) {
        players.getUsername(invitation.getIssuer()).thenAcceptAsync(username->{
            plugin.sendMessage(invitation.getReceived(),
                    username + " 님으로부터 섬 초대가 도착했습니다.\n"
                    + "/섬 초대 수락 "+username+" 시 섬 초대를 받을 수 있습니다.\n"
                    + "/섬 거절 "+username+" 시 섬 초대를 거절할수 있습니다.\n");
        });
    }

    @Override
    public void onAccept(Invitation invitation) throws InvitationTimeoutException {
        try {
            CompletableFuture<String> usernameFuture = players.getUsername(invitation.getReceived());
            IslandPlayer ip = plugin.getIslandPlayer(invitation.getIssuer());
            Island island = manager.getIsland(ip.getIslandId());
            IslandPlayersMap map = island.getPlayersMap().get();
            if (map.getOwner().get().getUniqueID().equals(invitation.getIssuer())) {
                map.addMember(new IslandPlayer(island.getID(), invitation.getReceived())).get();
                plugin.syncPlayer(invitation.getReceived());
            }
            ((IslandComponent) map).sync();
            remove(invitation.getReceived(), ip.getIslandId());
            CosmoIslandsBukkitBootstrap.getInst().syncPlayer(invitation.getReceived());
            usernameFuture.thenAccept(name -> island.getChat().thenAccept(ic-> ic.sendSystem(name + "님이 섬의 새로운 섬원이 되었습니다.")));
        } catch (InterruptedException | ExecutionException e) {
            plugin.sendMessage(invitation.getReceived(), "명령어 실행 중 오류가 발생했습니다. 관리자에게 문의 해주세요.");
        }
    }

    @Override
    public void onDeny(Invitation invitation) throws InvitationTimeoutException {
        try {
            CompletableFuture<String> usernameFuture = players.getUsername(invitation.getReceived());
            IslandPlayer ip = plugin.getIslandPlayer(invitation.getIssuer());
            remove(invitation.getReceived(), ip.getIslandId());
            plugin.syncPlayer(invitation.getReceived());
            usernameFuture.thenAccept(username-> plugin.sendMessage(invitation.getIssuer(), username+"님이 섬초대를 거절하셨습니다."));
        }catch (ExecutionException e){
            plugin.sendMessage(invitation.getReceived(), "명령어 실행 중 오류가 발생했습니다. 관리자에게 문의 해주세요.");
        }
    }

    @Override
    public void onTimeout(Invitation invitation) {
        CompletableFuture<String> receiverNameFuture = players.getUsername(invitation.getReceived());
        CompletableFuture<String> senderNameFuture = players.getUsername(invitation.getReceived());
        receiverNameFuture.thenAccept(username->plugin.sendMessage(invitation.getIssuer(), "플레이어 "+username+"님에게 보낸 초대 메세지가 만료 되었습니다."));
        senderNameFuture.thenAccept(username -> plugin.sendMessage(invitation.getReceived(), "플레이어 "+username+" 님이 보낸 초대 메세지가 만료 되었습니다."));
    }

    void remove(UUID uuid, int id){
        loader.remove(uuid, id);
    }
}
