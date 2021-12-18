package kr.comsoisland.cosmoislands.points;

import kr.cosmoisland.cosmoislands.api.points.IslandPoints;
import kr.cosmoisland.cosmoislands.api.points.IslandVoter;

import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CosmoIslandVoter implements IslandVoter {

    final UUID uuid;
    final VoteLogDataModel model;

    public CosmoIslandVoter(UUID uuid, VoteLogDataModel model){
        this.uuid = uuid;
        this.model = model;
    }

    @Override
    public CompletableFuture<Boolean> canVote(){
        return getLatestTime().thenApply(time->{
            return time + 24*60*60*1000L < getTodayMidnight();
        });
    }

    @Override
    public long vote(int id, IslandPoints points, int value) {
        long now = System.currentTimeMillis();
        points.addPoint(value);
        model.log(uuid, id, now);
        return now;
    }

    @Override
    public CompletableFuture<Long> getLatestTime(){
        return model.getLatestVotedTime(uuid);
    }

    private long getTodayMidnight(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
