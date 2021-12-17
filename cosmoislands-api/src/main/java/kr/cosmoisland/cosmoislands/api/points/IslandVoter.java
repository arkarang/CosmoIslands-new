package kr.cosmoisland.cosmoislands.api.points;

import java.util.concurrent.CompletableFuture;

public interface IslandVoter {

    public boolean canVote();

    public long vote(int id, IslandPoints points, int value);

    CompletableFuture<Long> getLatestTime();

}
