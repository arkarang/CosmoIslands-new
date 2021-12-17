package kr.cosmoisland.cosmoislands.players;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.player.PlayerModificationStrategy;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CosmoIslandPlayersMap implements IslandPlayersMap {

    final Island island;
    final MySQLPlayersMap mysql;
    final RedisPlayersMap redis;
    final ImmutableMap<String, PlayerModificationStrategy> strategies;

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
        return true;
    }

    @Override
    public CompletableFuture<IslandPlayer> getOwner() {
        return redis.getOwner().thenCompose(value->{
            if(value == null){
                CompletableFuture<IslandPlayer> future = mysql.getOwner();
                future.thenAccept(redis::setOwner);
                return future;
            }else{
                return CompletableFuture.completedFuture(value);
            }
        });
    }

    @Override
    public CompletableFuture<Void> setOwner(IslandPlayer player) {
        CompletableFuture<Void> redisFuture = redis.setOwner(player);
        CompletableFuture<Void> mysqlFuture = mysql.setOwner(player);
        return CompletableFuture.allOf(redisFuture, mysqlFuture)
                .thenRun(()->strategies.values().forEach(strategy -> strategy.onOwnerChange(island, player.getUniqueId())));
    }

    @Override
    public CompletableFuture<Void> setRank(IslandPlayer player, MemberRank rank) {
        CompletableFuture<Void> redisFuture = redis.setRank(player, rank);
        CompletableFuture<Void> mysqlFuture = mysql.setRank(player, rank);
        return CompletableFuture.allOf(redisFuture, mysqlFuture)
                .thenRun(()->strategies.values().forEach(strategy -> strategy.onRankChanged(island, player.getUniqueId(), rank)));
    }

    @Override
    public CompletableFuture<MemberRank> getRank(IslandPlayer player) {
        return redis.getRank(player).thenCompose(value->{
            if(value == null){
                CompletableFuture<MemberRank> future = mysql.getRank(player);
                future.thenAccept(rank->redis.setRank(player, rank));
                return future;
            }else{
                return CompletableFuture.completedFuture(value);
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeMember(IslandPlayer player) {
        CompletableFuture<Void> redisFuture = redis.removeMember(player);
        CompletableFuture<Void> mysqlFuture = mysql.removeMember(player);
        return CompletableFuture.allOf(redisFuture, mysqlFuture)
                .thenRun(()->strategies.values().forEach(strategy -> strategy.onPlayerRemove(island, player.getUniqueId())));
    }

    @Override
    public CompletableFuture<Void> addMember(IslandPlayer player) {
        CompletableFuture<Void> redisFuture = redis.addMember(player);
        CompletableFuture<Void> mysqlFuture = mysql.addMember(player);
        return CompletableFuture.allOf(redisFuture, mysqlFuture)
                .thenRun(()->strategies.values().forEach(strategy -> strategy.onPlayerAdd(island, player.getUniqueId())));
    }

    @Override
    public CompletableFuture<Boolean> isMember(UUID uuid) {
        return redis.isMember(uuid);
    }

    @Override
    public CompletableFuture<Map<UUID, MemberRank>> getMembers() {
        return redis.getMembers().thenCompose(members->{
            if(members.isEmpty()){
                CompletableFuture<Map<UUID, MemberRank>> mysqlFuture = mysql.getMembers();
                redis.migrateMembers(mysqlFuture);
                return mysqlFuture;
            }else
                return CompletableFuture.completedFuture(members);
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> removeIntern(UUID uuid) {
        CompletableFuture<Void> redisFuture = redis.removeIntern(uuid);
        CompletableFuture<Void> mysqlFuture = mysql.removeIntern(uuid);
        return CompletableFuture.allOf(redisFuture, mysqlFuture)
                .thenRun(()->strategies.values().forEach(strategy -> strategy.onInternRemove(island, uuid)));
    }

    @Override
    public CompletableFuture<Void> addIntern(UUID intern) {
        CompletableFuture<Void> redisFuture = redis.addIntern(intern);
        CompletableFuture<Void> mysqlFuture = mysql.addIntern(intern);
        return CompletableFuture.allOf(redisFuture, mysqlFuture)
                .thenRun(()->strategies.values().forEach(strategy -> strategy.onInternAdd(island, intern)));
    }

    @Override
    public CompletableFuture<List<UUID>> getInterns() {
        return redis.getInterns().thenCompose(interns->{
            if(interns.isEmpty()){
                CompletableFuture<List<UUID>> mysqlFuture = mysql.getInterns();
                redis.migrateInterns(mysqlFuture);
                return mysqlFuture;
            }else
                return CompletableFuture.completedFuture(interns);
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Boolean> isIntern(UUID uuid) {
        return redis.isIntern(uuid);
    }
}
