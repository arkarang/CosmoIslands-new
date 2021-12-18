package kr.cosmoisland.cosmoislands.api.points;

import java.util.concurrent.CompletableFuture;

public interface IslandVoter {

    CompletableFuture<Boolean> canVote();

    long vote(int id, IslandPoints points, int value);

    CompletableFuture<Long> getLatestTime();

}
