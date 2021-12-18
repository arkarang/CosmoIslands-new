package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissionsMap;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractPermissionsMap implements IslandPermissionsMap {

    protected final Map<IslandPermissions, MemberRank> map;

    protected AbstractPermissionsMap(IslandPermissionsMap map) throws ExecutionException, InterruptedException {
        this.map = map.asMap().get();
    }

    @Override
    public CompletableFuture<List<IslandPermissions>> getPermissions(MemberRank rank) {
        List<IslandPermissions> list = map.entrySet().stream().filter(entry-> entry.getValue().getPriority() <= rank.getPriority()).map(Map.Entry::getKey).collect(Collectors.toList());
        return CompletableFuture.completedFuture(list);
    }

    @Override
    public CompletableFuture<Boolean> hasPermission(IslandPermissions perm, MemberRank rank) {
        return CompletableFuture.completedFuture(map.containsKey(perm) && map.get(perm).getPriority() <= rank.getPriority());
    }

    @Override
    public CompletableFuture<Map<IslandPermissions, MemberRank>> asMap() {
        return CompletableFuture.completedFuture(map);
    }
}
