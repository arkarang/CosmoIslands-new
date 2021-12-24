package kr.cosmoislands.cosmoislands.bank;

import kr.cosmoislands.cosmoislands.api.ComponentLifecycle;
import kr.cosmoislands.cosmoislands.api.IslandContext;
import kr.cosmoislands.cosmoislands.api.ModulePriority;
import kr.cosmoislands.cosmoislands.api.bank.IslandBank;
import kr.cosmoislands.cosmoislands.api.bank.IslandVault;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class VaultLifecycle implements ComponentLifecycle {

    final IslandVaultModule module;

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.MEDIUM;
    }

    @Override
    public CompletableFuture<Void> onLoad(IslandContext island) {
        return module.getAsync(island.getIslandId()).thenAccept(vault->island.register(IslandVault.class, vault));
    }

    @Override
    public CompletableFuture<Void> onCreate(UUID owner, IslandContext island) {
        val future = module.getAsync(island.getIslandId()).thenAccept(vault->island.register(IslandVault.class, vault));
        val future2 = module.create(island.getIslandId(), owner);
        return CompletableFuture.allOf(future, future2);
    }

    @Override
    public CompletableFuture<Void> onUnload(IslandContext island) {
        module.invalidate(island.getIslandId());
        return island.getComponent(IslandBank.class).invalidate();
    }

    @Override
    public CompletableFuture<Void> onDelete(IslandContext island) {
        module.invalidate(island.getIslandId());
        return island.getComponent(IslandBank.class).invalidate();
    }
}
