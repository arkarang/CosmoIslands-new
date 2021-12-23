package kr.cosmoislands.cosmoislands.api.member;

import kr.cosmoislands.cosmoislands.api.Island;

import java.util.UUID;

public interface PlayerModificationStrategy {

    void onOwnerChange(Island island, UUID uuid);

    void onPlayerAdd(Island island, UUID uuid);

    void onPlayerRemove(Island island, UUID uuid);

    void onInternRemove(Island island, UUID uuid);

    void onInternAdd(Island island, UUID uuid);

    void onRankChanged(Island island, UUID uuid, MemberRank rank);

}
