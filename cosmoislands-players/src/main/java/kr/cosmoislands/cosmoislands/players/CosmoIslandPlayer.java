package kr.cosmoislands.cosmoislands.players;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.member.IslandInternship;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayer;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@RequiredArgsConstructor
public class CosmoIslandPlayer implements IslandPlayer {

    @Getter
    private final UUID uniqueId;
    private final IslandRegistry registry;
    @Getter
    private final IslandInternship internship;
    private final IslandPlayerRegistry playerRegistry;

    @Override
    public CompletableFuture<Island> getIsland() {
        return playerRegistry.getIslandId(this.uniqueId).thenCompose(id-> id == Island.NIL_ID ? registry.getIsland(id) : CompletableFuture.completedFuture(null));
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
