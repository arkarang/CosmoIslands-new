package kr.cosmoislands.cosmoislands.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandDataModel {

    void init();

    CompletableFuture<Void> create(int id, UUID uuid);

    CompletableFuture<Void> delete(int id);

}
