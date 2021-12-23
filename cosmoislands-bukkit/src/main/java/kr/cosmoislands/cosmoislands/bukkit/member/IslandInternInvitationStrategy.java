package kr.cosmoislands.cosmoislands.bukkit.member;

import com.minepalm.arkarangutils.invitation.Invitation;
import com.minepalm.arkarangutils.invitation.InvitationExecuteStrategy;
import com.minepalm.arkarangutils.invitation.exception.InvitationTimeoutException;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoislands.cosmochat.core.helper.CosmoChatHelper;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class IslandInternInvitationStrategy extends InvitationExecuteStrategy {

    final IslandRegistry islandRegistry;
    final IslandPlayerRegistry playerRegistry;
    final HelloPlayers players;
    final CosmoChatHelper helper;

    public IslandInternInvitationStrategy(IslandRegistry islandRegistry,
                                          IslandPlayerRegistry playerRegistry,
                                          HelloPlayers players,
                                          ExecutorService workers,
                                          CosmoChatHelper helper) {
        super(workers);
        this.islandRegistry = islandRegistry;
        this.playerRegistry = playerRegistry;
        this.players = players;
        this.helper = helper;
    }

    @Override
    public void onInvited(Invitation invitation) {
        players.getUsername(invitation.getIssuer()).thenAcceptAsync(username->{
            helper.system(invitation.getReceived()).send(
                    username + " 님으로부터 섬 알바 초대가 도착했습니다.\n"
                            + "/섬 알바 수락 "+username+" 시 섬 초대를 받을 수 있습니다.\n"
                            + "/섬 알바 거절 "+username+" 시 섬 초대를 거절할수 있습니다.\n");
        });
    }

    @Override
    public void onAccept(Invitation invitation) throws InvitationTimeoutException {
        CompletableFuture<String> usernameFuture = players.getUsername(invitation.getReceived());
        IslandPlayer islandPlayer = playerRegistry.get(invitation.getIssuer());

        islandPlayer.getIsland().thenAccept(island->{
            IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
            IslandChat chat = island.getComponent(IslandChat.class);

            playersMap.getOwner().thenAccept(owner->{
                if(owner.getUniqueId().equals(invitation.getIssuer())){
                    //todo: 알바 최대 가입 횟수 체크하기
                    playersMap.addIntern(invitation.getReceived());
                    usernameFuture.thenAccept(username->{
                        chat.sendSystem(username + "님이 새로운 섬 알바원이 되었습니다.");
                    });
                }
            });
        });
    }

    @Override
    public void onDeny(Invitation invitation) throws InvitationTimeoutException {
        CompletableFuture<String> usernameFuture = players.getUsername(invitation.getReceived());
        usernameFuture.thenAccept(username-> helper.system(invitation.getIssuer()).send(username+"님이 섬 알바 초대를 거절하셨습니다."));
    }

    @Override
    public void onTimeout(Invitation invitation) {
        CompletableFuture<String> receiverNameFuture = players.getUsername(invitation.getReceived());
        CompletableFuture<String> senderNameFuture = players.getUsername(invitation.getIssuer());
        receiverNameFuture.thenAccept(username->{
            helper.system(invitation.getIssuer()).send("플레이어 "+username+"님에게 보낸 섬 알바 초대 메세지가 만료 되었습니다.");
        });
        senderNameFuture.thenAccept(username -> {
            helper.system(invitation.getReceived()).send("플레이어 "+username+" 님이 보낸 섬 알바 초대 메세지가 만료 되었습니다.");
        });
    }
}
