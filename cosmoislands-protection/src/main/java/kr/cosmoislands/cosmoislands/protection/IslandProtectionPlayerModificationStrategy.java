package kr.cosmoislands.cosmoislands.protection;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.member.PlayerModificationStrategy;
import kr.cosmoislands.cosmoislands.api.protection.IslandProtection;

import java.util.UUID;

public class IslandProtectionPlayerModificationStrategy implements PlayerModificationStrategy {

    @Override
    public void onOwnerChange(Island island, UUID uuid) {
        island.getComponent(IslandProtection.class).update(uuid);
    }

    @Override
    public void onPlayerAdd(Island island, UUID uuid) {
        island.getComponent(IslandProtection.class).update(uuid);
    }

    @Override
    public void onPlayerRemove(Island island, UUID uuid) {
        island.getComponent(IslandProtection.class).update(uuid);
    }

    @Override
    public void onInternRemove(Island island, UUID uuid) {
        island.getComponent(IslandProtection.class).update(uuid);
    }

    @Override
    public void onInternAdd(Island island, UUID uuid) {
        island.getComponent(IslandProtection.class).update(uuid);
    }

    @Override
    public void onRankChanged(Island island, UUID uuid, MemberRank rank) {
        island.getComponent(IslandProtection.class).update(uuid);
    }
}
