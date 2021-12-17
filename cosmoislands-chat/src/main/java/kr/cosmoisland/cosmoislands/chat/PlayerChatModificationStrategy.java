package kr.cosmoisland.cosmoislands.chat;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.player.PlayerModificationStrategy;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerChatModificationStrategy implements PlayerModificationStrategy {

    private final IslandChatModule module;

    @Override
    public void onOwnerChange(Island island, UUID uuid) {
        module.get(island.getId()).setOwner(uuid);
    }

    @Override
    public void onPlayerAdd(Island island, UUID uuid) {
        module.get(island.getId()).add(uuid);
    }

    @Override
    public void onPlayerRemove(Island island, UUID uuid) {
        module.get(island.getId()).remove(uuid);
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
