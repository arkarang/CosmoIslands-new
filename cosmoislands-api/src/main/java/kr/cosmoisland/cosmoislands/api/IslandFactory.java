package kr.cosmoisland.cosmoislands.api;

import com.google.common.collect.ImmutableList;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandFactory {

    void addFirst(String tag, ComponentLifecycle strategy);

    void addLast(String tag, ComponentLifecycle strategy);

    void addBefore(String before, String tag, ComponentLifecycle strategy);

    void addAfter(String after, String tag, ComponentLifecycle strategy);

    ImmutableList<String> getOrders();

    CompletableFuture<IslandContext> fireCreate(UUID uuid);

    CompletableFuture<IslandContext> fireLoad(int islandId);

    CompletableFuture<IslandContext> fireUnload(Island island);

    CompletableFuture<IslandContext> fireDelete(Island island);

}
