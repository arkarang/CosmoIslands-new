package kr.cosmoisland.cosmoislands.players;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.player.PlayerModificationStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@RequiredArgsConstructor
public class CosmoIslandPlayer implements IslandPlayer {

    @Getter
    private final UUID uniqueId;
    private final IslandRegistry registry;
    private final IslandPlayerRegistry playerRegistry;

    @Override
    public CompletableFuture<Island> getIsland() {
        return playerRegistry.getIslandId(this.uniqueId).thenApply(id-> id == Island.NIL_ID ? registry.getIsland(id) : null);
    }

    @Override
    public CompletableFuture<Integer> getIslandId() {
        return playerRegistry.getIslandId(this.uniqueId);
    }

    @Override
    public void invalidate() {
        playerRegistry.unload(uniqueId);
    }

}
