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
import org.bukkit.World;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class PlayerPreconditions {

    @Getter
    private static final PlayerPreconditions.Factory factory = new PlayerPreconditions.Factory();

    static class Factory{

        @Getter
        @Setter
        private IslandPlayerRegistry playerRegistry;
        @Getter
        @Setter
        private ExecutorService executor;

        PlayerPreconditions build(UUID uuid){
            return new PlayerPreconditions(playerRegistry, executor, uuid);
        }

    }

    private final IslandPlayerRegistry playerRegistry;
    private final ExecutorService executor;
    private final UUID uuid;

    public CompletableFuture<Boolean> doesInIsland(World world) {
        if(world == null){
            return CompletableFuture.completedFuture(false);
        }else{
            return getIsland().thenApply(island -> {
                if (island != null) {
                    String islandWorldName = "island_" + island.getId();
                    return islandWorldName.equals(world.getName());
                } else {
                    return false;
                }
            });
        }
    }

    public CompletableFuture<Boolean> hasIsland(){
        return playerRegistry.get(uuid).getIsland().thenApply(Objects::nonNull);
    }

    public CompletableFuture<Island> getIsland(){
        return playerRegistry.get(uuid).getIsland();
    }

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
        return getIsland().thenCompose(island->{
            if(island == null){
                return CompletableFuture.completedFuture(false);
            }
            IslandPlayersMap map = island.getComponent(IslandPlayersMap.class);
            IslandPlayer islandPlayer = playerRegistry.get(uuid);
            return map.getRank(islandPlayer).thenApply(playerRank -> {
                return playerRank.getPriority() >= requiredRank.getPriority();
            });
        });
    }

    public CompletableFuture<Boolean> rankAs(UUID uuid, MemberRank requiredRank){
        return getIsland().thenCompose(island->{
            if(island == null){
                return CompletableFuture.completedFuture(false);
            }
            IslandPlayersMap map = island.getComponent(IslandPlayersMap.class);
            IslandPlayer islandPlayer = playerRegistry.get(uuid);
            return map.getRank(islandPlayer).thenApply(playerRank -> {
                return playerRank.getPriority() >= requiredRank.getPriority();
            });
        });
    }

    public static PlayerPreconditions of(UUID uuid){
        if(factory.getPlayerRegistry() == null){
            throw new NullPointerException("could not found uuid registry");
        }
        return factory.build(uuid);
    }

}
