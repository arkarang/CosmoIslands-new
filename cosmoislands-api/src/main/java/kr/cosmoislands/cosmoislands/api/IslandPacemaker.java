package kr.cosmoislands.cosmoislands.api;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public interface IslandPacemaker {

    long getHeartbeatPeriod();

    void addTask(String tag, Consumer<Island> island);

    void shutdown() throws ExecutionException, InterruptedException;
}
