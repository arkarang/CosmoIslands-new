package kr.cosmoislands.cosmoislands.protection;

import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissionsMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StaticIslandPermissionsMap implements IslandPermissionsMap {

    ImmutableMap<IslandPermissions, MemberRank> permissionsMap;

    StaticIslandPermissionsMap(Map<IslandPermissions, MemberRank> initialPermissions){
        this.permissionsMap = ImmutableMap.copyOf(initialPermissions);
    }

    @Override
    public CompletableFuture<List<IslandPermissions>> getPermissions(MemberRank rank) {
        List<IslandPermissions> list = new ArrayList<>();
        for (Map.Entry<IslandPermissions, MemberRank> entry : permissionsMap.entrySet()) {
            if(entry.getValue().equals(rank)){
                list.add(entry.getKey());
            }
        }
        return CompletableFuture.completedFuture(list);
    }

    @Override
    public CompletableFuture<Boolean> hasPermission(IslandPermissions perm, MemberRank rank) {
        MemberRank requiredRank = permissionsMap.get(perm);
        return CompletableFuture.completedFuture(requiredRank.getPriority() <= rank.getPriority());
    }

    @Override
    public CompletableFuture<Void> setPermission(IslandPermissions perm, MemberRank rank) {
        throw new UnsupportedOperationException("cannot be set");
    }

    @Override
    public CompletableFuture<Map<IslandPermissions, MemberRank>> asMap() {
        return CompletableFuture.completedFuture(permissionsMap);
    }

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
}
