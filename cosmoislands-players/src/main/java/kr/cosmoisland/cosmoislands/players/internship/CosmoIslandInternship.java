package kr.cosmoisland.cosmoislands.players.internship;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandInternship;
import kr.cosmoisland.cosmoislands.api.player.IslandInternshipRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
                .thenApply(this::reMap);
    }

    private List<Island> reMap(List<Integer> list){
        List<Island> result = new ArrayList<>();
        for (Integer id : list) {
            Island island = islandRegistry.getIsland(id);
            if(island != null){
                result.add(island);
            }
        }
        return result;
    }
}
