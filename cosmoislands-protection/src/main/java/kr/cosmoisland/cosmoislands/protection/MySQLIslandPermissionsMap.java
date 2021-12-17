package kr.cosmoisland.cosmoislands.protection;

import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissionsMap;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandPermissionsMap implements IslandPermissionsMap {

    private final int islandId;
    private final MySQLIslandPermissionsDataModel model;

    @Override
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture(null);
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
    public CompletableFuture<List<IslandPermissions>> getPermissions(MemberRank rank) {
        return model.getPermissions(this.islandId, rank);
    }

    @Override
    public CompletableFuture<Boolean> hasPermission(IslandPermissions perm, MemberRank rank) {
        return model.hasPermission(this.islandId, perm, rank);
    }

    @Override
    public CompletableFuture<Void> setPermission(IslandPermissions perm, MemberRank rank) {
        return model.setPermission(this.islandId, perm, rank);
    }

    @Override
    public CompletableFuture<Map<IslandPermissions, MemberRank>> asMap() {
        return model.asMap(this.islandId);
    }
}
