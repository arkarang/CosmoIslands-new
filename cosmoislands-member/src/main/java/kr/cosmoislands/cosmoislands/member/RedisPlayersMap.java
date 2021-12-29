package kr.cosmoislands.cosmoislands.member;

import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RedisPlayersMap implements IslandPlayersMap {

    private final String membersKey, internsKey, ownerKey;
    private final RedisAsyncCommands<String, String> async;
    private final IslandPlayerRegistry registry;

    RedisPlayersMap(int islandId, IslandPlayerRegistry registry, RedisAsyncCommands<String, String> async){
        this.async = async;
        this.registry = registry;
        ownerKey = "cosmoislands:island:"+islandId+":owner";
        membersKey = "cosmoislands:island:"+islandId+":members";
        internsKey = "cosmoislands:island:"+islandId+":interns";
    }

    CompletableFuture<Void> migrate(IslandPlayersMap players){
        CompletableFuture<Void> insertOwnerFuture = migrateOwner(players.getOwner());
        CompletableFuture<Void> insertMembersFuture = migrateMembers(players.getMembers());
        CompletableFuture<Void> insertInternsFuture = migrateInterns(players.getInterns());
        return CompletableFuture.allOf(insertOwnerFuture, insertMembersFuture);
    }

    CompletableFuture<Void> migrateOwner(CompletableFuture<IslandPlayer> ownerFuture){
        return ownerFuture.thenCompose(ip->{
            if(ip == null){
                return CompletableFuture.completedFuture(null);
            }else{
                return setOwner(ip);
            }
        });
    }

    CompletableFuture<Void> migrateMembers(CompletableFuture<Map<UUID, MemberRank>> membersFuture){
        return membersFuture.thenCompose(map -> {
            DebugLogger.log("redisPlayersMap: migrateMembers");
            HashMap<String, String> result = new HashMap<>();
            map.forEach((key, value) -> result.put(key.toString(), value.name()));
            if(!result.isEmpty()){
                return async.hmset(membersKey, result).thenRun(()->{}).toCompletableFuture();
            }else{
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    CompletableFuture<Void> migrateInterns(CompletableFuture<List<UUID>> internsMap){
        return internsMap.thenCompose(list-> {
            return async.sadd(internsKey, list.stream().map(UUID::toString).toArray(String[]::new)).thenRun(()->{}).toCompletableFuture();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.allOf(async.del(membersKey).toCompletableFuture(), async.del(internsKey).toCompletableFuture(), async.del(ownerKey).toCompletableFuture());
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public CompletableFuture<IslandPlayer> getOwner() {
        return async.get(ownerKey)
                .thenApply(str-> str != null ? registry.get(UUID.fromString(str)) : null)
                .handle((ip, e) -> e != null ? null : ip)
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> setOwner(@Nonnull IslandPlayer player) {
        return async.set(ownerKey, player.getUniqueId().toString())
                .thenCompose(ignored-> setRank(player, MemberRank.OWNER))
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> setRank(@Nonnull IslandPlayer player, MemberRank rank) {
        return async.hset(membersKey, player.getUniqueId()+"", rank.name())
                .thenRun(()->{})
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<MemberRank> getRank(@Nonnull IslandPlayer player) {
        return async.hget(membersKey, player.getUniqueId()+"")
                .thenApply(value-> value == null ? MemberRank.NONE : MemberRank.valueOf(value))
                .handle((rank, e)-> e != null ? MemberRank.NONE : rank)
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> removeMember(@Nonnull IslandPlayer player) {
        return async.hdel(membersKey, player.getUniqueId()+"")
                .thenRun(()->{})
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> addMember(@Nonnull IslandPlayer player) {
        return async.hset(membersKey, player.getUniqueId()+"", MemberRank.MEMBER.name())
                .thenRun(()->{})
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Boolean> isMember(UUID uuid) {
        return async.hexists(membersKey, uuid.toString()).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Map<UUID, MemberRank>> getMembers() {
        return async.hgetall(membersKey).thenApply(map->{
            Map<UUID, MemberRank> result = new HashMap<>();
            for (String key : map.keySet()) {
                try {
                    result.put(UUID.fromString(key), MemberRank.valueOf(map.get(key)));
                }catch (IllegalArgumentException e){
                    continue;
                }
            }
            return result;
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> removeIntern(IslandPlayer player) {
        UUID uuid = player.getUniqueId();
        return async.srem(internsKey, uuid.toString())
                .thenRun(()->{})
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> addIntern(IslandPlayer player) {
        UUID uuid = player.getUniqueId();
        return async.sadd(internsKey, uuid.toString())
                .thenCompose(ignored -> {
                    return player.getInternship().update();
                }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<List<UUID>> getInterns() {
        return async.smembers(internsKey).thenApply(list->{
            List<UUID> newList = new ArrayList<>();
            for (String key : list) {
                try{
                    newList.add(UUID.fromString(key));
                }catch (IllegalArgumentException e){
                    continue;
                }
            }
            return newList;
        }).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Boolean> isIntern(UUID uuid) {
        return async.sismember(internsKey, uuid.toString()).toCompletableFuture();
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
