package kr.cosmoisland.cosmoislands.api.player;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandPlayersMap extends IslandComponent {

    byte COMPONENT_ID = 6;

    CompletableFuture<IslandPlayer> getOwner();

    CompletableFuture<Void> setOwner(IslandPlayer player);

    CompletableFuture<Void> setRank(IslandPlayer player, MemberRank rank);

    CompletableFuture<MemberRank> getRank(IslandPlayer player);

    CompletableFuture<Void> removeMember(IslandPlayer player);

    CompletableFuture<Void> addMember(IslandPlayer player);

    CompletableFuture<Boolean> isMember(UUID uuid);

    CompletableFuture<Map<UUID, MemberRank>> getMembers();

    CompletableFuture<Void> removeIntern(UUID uuid);

    CompletableFuture<Void> addIntern(UUID intern);

    CompletableFuture<List<UUID>> getInterns();

    CompletableFuture<Boolean> isIntern(UUID uuid);

    CompletableFuture<Void> setMaxPlayers(int i);

    CompletableFuture<Integer> getMaxInterns();

    CompletableFuture<Void> setMaxInterns(int i);

    CompletableFuture<Integer> getMaxPlayers();


}
