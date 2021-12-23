package kr.cosmoislands.cosmoislands.level;

import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.IslandRanking;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class LevelRanking implements IslandRanking {

    final IslandLevelDataModel model;

    @Override
    public CompletableFuture<List<RankingData>> getTopOf(int rank) {
        return model.getTopOf(rank);
    }

    @Override
    public CompletableFuture<Integer> getRank(Island island) {
        return model.getRank(island.getId());
    }
}
