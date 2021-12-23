package kr.cosmoislands.cosmoislands.players.internship;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.member.IslandInternship;
import kr.cosmoislands.cosmoislands.api.member.IslandInternshipRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CosmoIslandInternship implements IslandInternship {

    @Getter
    private final UUID uniqueId;
    private final IslandRegistry islandRegistry;
    private final IslandInternshipRegistry internshipRegistry;

    @Override
    public CompletableFuture<Integer> getMaxInternships() {
        return internshipRegistry.getMaxInternships(uniqueId);
    }

    @Override
    public CompletableFuture<List<Island>> getHiredIslands() {
        return internshipRegistry
                .getHiredIslandIds(uniqueId)
                .thenCompose(this::reMap);
    }

    private CompletableFuture<List<Island>> reMap(List<Integer> list){
        List<Island> result = new ArrayList<>();
        List<CompletableFuture<?>> islandFutures = new ArrayList<>();
        for (Integer id : list) {
            val future = islandRegistry.getIsland(id);
            val addFuture = future.thenAccept(island -> {
                if(island != null){
                    result.add(island);
                }
            });
            islandFutures.add(addFuture);
        }
        return CompletableFuture.allOf(islandFutures.toArray(new CompletableFuture[0])).thenApply(ignored->result);
    }
}
