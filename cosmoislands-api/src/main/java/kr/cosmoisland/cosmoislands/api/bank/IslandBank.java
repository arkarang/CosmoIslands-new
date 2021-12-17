package kr.cosmoisland.cosmoislands.api.bank;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandBank extends IslandComponent {

    CompletableFuture<Integer> getLevel();

    CompletableFuture<Void> setLevel(int level);

    CompletableFuture<Void> saveInventory();

    CompletableFuture<Void> openInventory(UUID uuid);

}
