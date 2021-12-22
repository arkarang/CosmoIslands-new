package kr.cosmoisland.cosmoislands.api.warp;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;

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
