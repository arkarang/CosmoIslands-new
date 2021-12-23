package kr.cosmoislands.cosmoislands.api;

import java.util.concurrent.CompletableFuture;

public interface IslandComponent {

    <T extends IslandComponent> CompletableFuture<T> sync();

    CompletableFuture<Void> invalidate();

    boolean validate();

}
