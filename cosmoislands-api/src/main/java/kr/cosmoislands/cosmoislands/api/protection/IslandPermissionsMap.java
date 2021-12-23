package kr.cosmoislands.cosmoislands.api.protection;

import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IslandPermissionsMap extends IslandComponent {

    byte COMPONENT_ID = 8;

    CompletableFuture<List<IslandPermissions>> getPermissions(MemberRank rank);

    CompletableFuture<Boolean> hasPermission(IslandPermissions perm, MemberRank rank);

    CompletableFuture<Void> setPermission(IslandPermissions perm, MemberRank rank);

    CompletableFuture<Map<IslandPermissions, MemberRank>> asMap();
}
