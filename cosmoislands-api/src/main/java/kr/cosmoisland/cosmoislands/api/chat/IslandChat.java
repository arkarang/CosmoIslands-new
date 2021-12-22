package kr.cosmoisland.cosmoislands.api.chat;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandChat extends IslandComponent {

    byte COMPONENT_ID = 3;

    int getIslandID();

    CompletableFuture<UUID> getOwner();

    CompletableFuture<Void> setOwner(UUID uuid);

    CompletableFuture<Void> add(UUID uuid);

    CompletableFuture<Void> remove(UUID uuid);

    CompletableFuture<Boolean> switchChannel(UUID uuid);

    CompletableFuture<Void> disband();

    void sendSystem(String message);

    void sendPlayer(UUID uuid, String username, String message);
}
