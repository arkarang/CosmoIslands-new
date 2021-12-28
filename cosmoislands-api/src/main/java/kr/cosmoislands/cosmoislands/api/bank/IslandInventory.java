package kr.cosmoislands.cosmoislands.api.bank;

import kr.cosmoislands.cosmoislands.api.IslandComponent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandInventory extends IslandComponent {

    byte COMPONENT_ID = 1;

    CompletableFuture<Integer> getLevel();

    CompletableFuture<Void> setLevel(int level);

    CompletableFuture<Void> saveInventory();

    CompletableFuture<Void> openInventory(UUID uuid);

}
