package kr.cosmoisland.cosmoislands.api.level;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandLevel extends IslandComponent {

    //level
    CompletableFuture<Integer> getLevel();

    //level
    CompletableFuture<Void> setLevel(int value);
}
