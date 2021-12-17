package kr.cosmoisland.cosmoislands.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IslandRanking {

    @Data
    @RequiredArgsConstructor
    class RankingData{
        int islandId;
        int value;
    }

    CompletableFuture<List<RankingData>> getTopOf(int rank);

    CompletableFuture<Integer> getRank(Island island);

}
