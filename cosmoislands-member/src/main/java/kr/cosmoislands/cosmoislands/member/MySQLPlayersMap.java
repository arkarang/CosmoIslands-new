package kr.cosmoislands.cosmoislands.member;

import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLPlayersMap implements IslandPlayersMap {

    final int islandId;
    final IslandPlayerRegistry registry;
    final PlayersMapDataModel model;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean validate() {
        //todo: validate 규칙 정비하기
        return true;
    }

    @Override
    public CompletableFuture<IslandPlayer> getOwner() {
        return model.getOwner(islandId).thenApply(registry::get);
    }

    @Override
    public CompletableFuture<Void> setOwner(IslandPlayer player) {
        return model.setOwner(islandId, player.getUniqueId());
    }

    @Override
    public CompletableFuture<Void> setRank(IslandPlayer player, MemberRank rank) {
        return model.setRank(islandId, player.getUniqueId(), rank);
    }

    @Override
    public CompletableFuture<MemberRank> getRank(IslandPlayer player) {
        return model.getRank(islandId, player.getUniqueId());
    }

    @Override
    public CompletableFuture<Void> removeMember(IslandPlayer player) {
        return model.removeMember(islandId, player.getUniqueId());
    }

    @Override
    public CompletableFuture<Void> addMember(IslandPlayer player) {
        return model.addMember(islandId, player.getUniqueId());
    }

    @Override
    public CompletableFuture<Boolean> isMember(UUID uuid) {
        return model.isMember(islandId, uuid);
    }

    @Override
    public CompletableFuture<Map<UUID, MemberRank>> getMembers() {
        return model.getMembers(islandId);
    }

    @Override
    public CompletableFuture<Void> removeIntern(UUID uuid) {
        return model.removeIntern(islandId, uuid);
    }

    @Override
    public CompletableFuture<Void> addIntern(UUID uuid) {
        return model.addIntern(islandId, uuid);
    }

    @Override
    public CompletableFuture<List<UUID>> getInterns() {
        return model.getInterns(islandId);
    }

    @Override
    public CompletableFuture<Boolean> isIntern(UUID uuid) {
        return model.isIntern(islandId, uuid);
    }

    @Override
    public CompletableFuture<Void> setMaxPlayers(int i) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public CompletableFuture<Integer> getMaxInterns() {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public CompletableFuture<Void> setMaxInterns(int i) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public CompletableFuture<Integer> getMaxPlayers() {
        throw new UnsupportedOperationException("not supported");
    }
}
