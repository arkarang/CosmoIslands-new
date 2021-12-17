package kr.cosmoislands.cosmoislands.bukkit.member;

import com.minepalm.arkarangutils.invitation.Invitation;
import com.minepalm.arkarangutils.invitation.InvitationExecuteStrategy;
import com.minepalm.arkarangutils.invitation.exception.InvitationTimeoutException;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslands;
import kr.cosmoisland.cosmoislands.bukkit.IslandManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class IslandInvitationStrategy extends InvitationExecuteStrategy {

    final CosmoIslands plugin;
    final IslandManager manager;
    final HelloPlayers players;

    public IslandInvitationStrategy(CosmoIslands plugin, IslandManager manager, HelloPlayers players, ExecutorService workers) {
        super(workers);
        this.plugin = plugin;
        this.manager = manager;
        this.players = players;
    }

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
            Island island = manager.getIsland(ip.getIslandID());
            IslandPlayersMap map = island.getPlayersMap().get();
            IslandChat chat = island.getChat().get();
            if (map.getOwner().get().getUniqueID().equals(invitation.getIssuer())) {
                map.addMember(new IslandPlayer(island.getID(), invitation.getReceived())).get();
                chat.add(invitation.getReceived());
                plugin.syncPlayer(invitation.getReceived());
            }
            ((IslandComponent) map).sync();
            CosmoIslands.getInst().syncPlayer(invitation.getReceived());
            usernameFuture.thenAccept(name -> island.getChat().thenAccept(ic-> ic.sendSystem(name + "님이 섬의 새로운 섬원이 되었습니다.")));
        } catch (InterruptedException | ExecutionException e) {
            plugin.sendMessage(invitation.getReceived(), "명령어 실행 중 오류가 발생했습니다. 관리자에게 문의 해주세요.");
        }
    }

    @Override
    public void onDeny(Invitation invitation) throws InvitationTimeoutException {
        CompletableFuture<String> usernameFuture = players.getUsername(invitation.getReceived());
        plugin.syncPlayer(invitation.getReceived());
        usernameFuture.thenAccept(username-> plugin.sendMessage(invitation.getIssuer(), username+"님이 섬초대를 거절하셨습니다."));
    }

    @Override
    public void onTimeout(Invitation invitation) {
        CompletableFuture<String> receiverNameFuture = players.getUsername(invitation.getReceived());
        CompletableFuture<String> senderNameFuture = players.getUsername(invitation.getIssuer());
        receiverNameFuture.thenAccept(username->plugin.sendMessage(invitation.getIssuer(), "플레이어 "+username+"님에게 보낸 초대 메세지가 만료 되었습니다."));
        senderNameFuture.thenAccept(username -> plugin.sendMessage(invitation.getReceived(), "플레이어 "+username+" 님이 보낸 초대 메세지가 만료 되었습니다."));
    }
}
