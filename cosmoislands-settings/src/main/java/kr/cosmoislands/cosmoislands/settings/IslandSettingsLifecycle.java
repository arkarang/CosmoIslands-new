package kr.cosmoislands.cosmoislands.settings;

import kr.cosmoislands.cosmoislands.api.ComponentLifecycle;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.IslandContext;
import kr.cosmoislands.cosmoislands.api.ModulePriority;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandSettingsLifecycle implements ComponentLifecycle {

    final IslandSettingsModule module;

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.MEDIUM;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        return module.getAsync(island.getIslandId()).thenAccept(settingsMap->island.register(IslandSettingsMap.class, settingsMap));
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        return onLoad(island);
    }

    @Override
    public CompletableFuture<Void> onUnload(IslandContext island) {
        module.invalidate(island.getIslandId());
        return module.getAsync(island.getIslandId()).thenAccept(IslandComponent::invalidate);
    }

    @Override
    public CompletableFuture<Void> onDelete(IslandContext island) {
        return onUnload(island);
    }
}
