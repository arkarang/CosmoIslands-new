package kr.cosmoislands.cosmoislands.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IslandRanking {

    @Data
    @RequiredArgsConstructor
    class RankingData{
        final int islandId;
        final int value;
    }

    CompletableFuture<List<RankingData>> getTopOf(int rank);

    CompletableFuture<Integer> getRank(Island island);

}
