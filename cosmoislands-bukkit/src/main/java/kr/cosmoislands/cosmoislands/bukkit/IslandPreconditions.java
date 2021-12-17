package kr.cosmoislands.cosmoislands.bukkit;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandPreconditions {

    @Getter
    private static final Factory factory = new Factory();

    static class Factory{

        @Getter
        @Setter
        private IslandPlayerRegistry playerRegistry;

        IslandPreconditions build(Island island){
            return new IslandPreconditions(playerRegistry, island);
        }

    }

    private final IslandPlayerRegistry playerRegistry;
    private final Island island;

    public CompletableFuture<Boolean> isMemberOf(UUID uuid){
        return hasRank(uuid, MemberRank.INTERN);
    }

    public CompletableFuture<Boolean> isInternOf(UUID uuid){
        return rankAs(uuid, MemberRank.INTERN);
    }

    public CompletableFuture<Boolean> isOwnerOf(UUID uuid){
        return rankAs(uuid, MemberRank.OWNER);
    }

    public CompletableFuture<Boolean> isPlayerOf(UUID uuid){
        return rankAs(uuid, MemberRank.MEMBER);
    }

    public CompletableFuture<Boolean> hasRank(UUID uuid, MemberRank requiredRank){
        IslandPlayersMap map = island.getComponent(IslandPlayersMap.class);
        IslandPlayer islandPlayer = playerRegistry.get(uuid);
        return map.getRank(islandPlayer).thenApply(playerRank -> {
            return playerRank.getPriority() >= requiredRank.getPriority();
        });
    }

    public CompletableFuture<Boolean> rankAs(UUID uuid, MemberRank requiredRank){
        IslandPlayersMap map = island.getComponent(IslandPlayersMap.class);
        IslandPlayer islandPlayer = playerRegistry.get(uuid);
        return map.getRank(islandPlayer).thenApply(playerRank -> {
            return playerRank.getPriority() >= requiredRank.getPriority();
        });
    }

    public static IslandPreconditions of(Island island){
        if(factory.getPlayerRegistry() == null){
            throw new NullPointerException("could not found player registry");
        }
        return factory.build(island);
    }
}
