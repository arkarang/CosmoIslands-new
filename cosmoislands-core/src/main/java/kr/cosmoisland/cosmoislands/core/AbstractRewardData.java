package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.level.IslandAchievements;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public abstract class AbstractRewardData implements IslandAchievements {

    protected final Map<Integer, Boolean> map;

    public AbstractRewardData(IslandAchievements data) throws ExecutionException, InterruptedException {
        map = new HashMap<>();
        map.putAll(data.asMap().get());
    }

    @Override
    public CompletableFuture<Boolean> isAchieved(int slot) {
        return CompletableFuture.completedFuture(map.getOrDefault(slot, false));
    }

    @Override
    public CompletableFuture<Map<Integer, Boolean>> asMap() {
        return CompletableFuture.completedFuture(map);
    }
}
