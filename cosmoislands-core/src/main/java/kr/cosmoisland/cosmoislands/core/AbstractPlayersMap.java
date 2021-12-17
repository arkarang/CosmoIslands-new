package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


@RequiredArgsConstructor
public abstract class AbstractPlayersMap implements IslandPlayersMap {

    protected final Map<IslandPlayer, MemberRank> players;

    protected AbstractPlayersMap(IslandPlayersMap map) throws ExecutionException, InterruptedException {
        players = new ConcurrentHashMap<>();
        players.putAll(map.getMembers().get());
    }

    @Override
    public CompletableFuture<IslandPlayer> getOwner() {
        IslandPlayer owner = null;
        for (Map.Entry<IslandPlayer, MemberRank> entry : players.entrySet()) {
            if(entry.getValue().equals(MemberRank.OWNER))
                owner = entry.getKey();
        }
        return CompletableFuture.completedFuture(owner);
    }

    public CompletableFuture<MemberRank> getRank(IslandPlayer ip){
        return CompletableFuture.completedFuture(players.getOrDefault(ip, MemberRank.NONE));
    }

    public CompletableFuture<Boolean> isMember(UUID uuid){
        boolean found = false;
        for (Map.Entry<IslandPlayer, MemberRank> entry : players.entrySet()) {
            if(entry.getKey().getUniqueID().equals(uuid))
                found = true;
        }
        return CompletableFuture.completedFuture(found);
    }

    @Override
    public CompletableFuture<Map<IslandPlayer, MemberRank>> getMembers() {
        return CompletableFuture.completedFuture(players);
    }

}
