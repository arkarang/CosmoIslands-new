package kr.cosmoislands.cosmoislands.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.IslandContext;
import kr.cosmoislands.cosmoislands.api.member.PlayerModificationStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class CosmoIslandContext implements IslandContext {

    private final int islandId;
    private final boolean local;
    private final ConcurrentHashMap<Class<? extends IslandComponent>, IslandComponent> components = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PlayerModificationStrategy> strategies = new ConcurrentHashMap<>();

    CosmoIslandContext(Island island, boolean local){
        this.islandId = island.getId();
        this.local = local;
        components.putAll(island.getComponents());
    }

    @Override
    public List<Class<? extends IslandComponent>> getApplied() {
        return ImmutableList.copyOf(components.keySet());
    }

    @Override
    public <T extends IslandComponent> void register(Class<T> clazz, IslandComponent component) {
        components.putIfAbsent(clazz, component);
        DebugLogger.log("applied: "+getApplied().stream().map(Class::getSimpleName).collect(Collectors.toList()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> T getComponent(Class<T> clazz) {
        return (T)components.get(clazz);
    }
}
