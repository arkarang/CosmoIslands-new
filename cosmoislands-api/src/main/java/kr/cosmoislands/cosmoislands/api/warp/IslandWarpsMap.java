package kr.cosmoislands.cosmoislands.api.warp;

import kr.cosmoislands.cosmoislands.api.AbstractLocation;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IslandWarpsMap extends IslandComponent {

    byte COMPONENT_ID = 12;

    CompletableFuture<IslandLocation> getSpawnLocation();

    CompletableFuture<Void> setSpawnLocation(AbstractLocation location);

    CompletableFuture<IslandWarp> getWarp(String name);

    CompletableFuture<Boolean> changeWarpPermission(String name, MemberRank toSet);

    CompletableFuture<List<IslandWarp>> getWarps(MemberRank required);

    CompletableFuture<Void> insertWarp(IslandWarp warp);

    CompletableFuture<Void> deleteWarp(String name);

    CompletableFuture<Integer> getMaxWarps();

}
