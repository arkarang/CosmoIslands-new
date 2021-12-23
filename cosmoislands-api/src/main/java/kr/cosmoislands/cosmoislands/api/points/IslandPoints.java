package kr.cosmoislands.cosmoislands.api.points;

import kr.cosmoislands.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandPoints extends IslandComponent {

    byte COMPONENT_ID = 7;

    CompletableFuture<Integer> getPoints();

    CompletableFuture<Void> addPoint(int value);

    CompletableFuture<Void> setPoint(int value);

}
