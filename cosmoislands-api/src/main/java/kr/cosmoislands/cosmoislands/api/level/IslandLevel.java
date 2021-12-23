package kr.cosmoislands.cosmoislands.api.level;

import kr.cosmoislands.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandLevel extends IslandComponent {

    byte COMPONENT_ID = 5;

    //level
    CompletableFuture<Integer> getLevel();

    CompletableFuture<Void> addLevel(int value);

    //level
    CompletableFuture<Void> setLevel(int value);
}
