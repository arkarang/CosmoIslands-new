package kr.cosmoislands.cosmoislands.chat;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.member.PlayerModificationStrategy;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerChatModificationStrategy implements PlayerModificationStrategy {

    private final IslandChatModule module;

    @Override
    public void onOwnerChange(Island island, UUID uuid) {
        module.getAsync(island.getId()).thenAccept(chat->{
            chat.setOwner(uuid);
        });
    }

    @Override
    public void onPlayerAdd(Island island, UUID uuid) {
        module.getAsync(island.getId()).thenAccept(chat->{
            chat.add(uuid);
        });
    }

    @Override
    public void onPlayerRemove(Island island, UUID uuid) {
        module.getAsync(island.getId()).thenAccept(chat->{
            chat.remove(uuid);
        });
    }

    @Override
    public void onInternRemove(Island island, UUID uuid) {

    }

    @Override
    public void onInternAdd(Island island, UUID uuid) {

    }

    @Override
    public void onRankChanged(Island island, UUID uuid, MemberRank rank) {

    }
}
