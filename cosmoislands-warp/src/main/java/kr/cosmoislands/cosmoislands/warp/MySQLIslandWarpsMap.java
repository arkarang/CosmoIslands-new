package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.api.warp.IslandLocation;
import kr.cosmoisland.cosmoislands.api.warp.IslandWarp;
import kr.cosmoisland.cosmoislands.api.warp.IslandWarpsMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandWarpsMap implements IslandWarpsMap {

    private final int islandId;
    private final IslandSettingsMap settingsMap;
    private final MySQLIslandWarpsDataModel model;
    static final String SPAWN = "spawn";

    @Override
    public CompletableFuture<IslandLocation> getSpawnLocation() {
        return model.getWarp(islandId, SPAWN).thenApply(this::toIslandLocation);
    }

    @Override
    public CompletableFuture<Void> setSpawnLocation(AbstractLocation location) {
        return model.insertWarp(islandId, new IslandWarp(SPAWN, MemberRank.NONE, location));
    }

    @Override
    public CompletableFuture<IslandWarp> getWarp(String name) {
        return model.getWarp(islandId, name);
    }

    @Override
    public CompletableFuture<Boolean> changeWarpPermission(String name, MemberRank toSet) {
        return model.updateWarpPermission(islandId, name, toSet);
    }

    @Override
    public CompletableFuture<List<IslandWarp>> getWarps(MemberRank required) {
        return model.getWarps(islandId, required);
    }

    @Override
    public CompletableFuture<Void> insertWarp(IslandWarp warp) {
        return getWarp(warp.getName()).thenCompose(found->{
            val countFuture = model.count(islandId);
            CompletableFuture<Void> result;
            if(found == null){
                result = getMaxWarps().thenCombine(countFuture, (maxWarps, count) -> {
                    // spawn 제외
                    if(maxWarps > count - 1){
                        return model.insertWarp(islandId, warp);
                    }else{
                        CompletableFuture<Void> future = new CompletableFuture<>();
                        future.completeExceptionally(new IllegalStateException("exceeded max warps"));
                        return future;
                    }
                }).thenCompose(future->future);
            }else{
                result = model.insertWarp(islandId, warp);
            }
            return result;
        });
    }

    @Override
    public CompletableFuture<Void> deleteWarp(String name) {
        if(name.equals(SPAWN)){
            throw new IllegalArgumentException("cannot delete spawn");
        }
        return model.delete(islandId, name);
    }

    @Override
    public CompletableFuture<Integer> getMaxWarps() {
        return settingsMap.getSettingAsync(IslandSetting.MAX_WARPS).thenApply(Integer::parseInt);
    }

    @Override
    @SneakyThrows
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

    private IslandLocation toIslandLocation(AbstractLocation location){
        return new IslandLocation(this.islandId, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }
}
