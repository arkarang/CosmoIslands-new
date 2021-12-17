package kr.cosmoisland.cosmoislands.api.points;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandPoints extends IslandComponent {

    CompletableFuture<Integer> getPoints();

    CompletableFuture<Void> addPoint(int value);

    CompletableFuture<Void> setPoint(int value);

}
