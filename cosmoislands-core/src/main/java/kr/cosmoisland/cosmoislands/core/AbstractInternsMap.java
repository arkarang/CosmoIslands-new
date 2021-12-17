package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.internship.IslandIntern;
import kr.cosmoisland.cosmoislands.api.internship.IslandInternsMap;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public abstract class AbstractInternsMap implements IslandInternsMap {

    protected final Map<UUID, IslandIntern> map;

    public AbstractInternsMap(IslandInternsMap initial) throws ExecutionException, InterruptedException {
        map = new HashMap<>();
        initial.getInterns().get().forEach(ii -> map.put(ii.getUniqueId(), ii));
    }

    @Override
    public CompletableFuture<List<IslandIntern>> getInterns() {
        return CompletableFuture.completedFuture(new ArrayList<>(map.values()));
    }

    @Override
    public CompletableFuture<Boolean> isIntern(UUID uuid) {
        return CompletableFuture.completedFuture(map.containsKey(uuid));
    }
}
