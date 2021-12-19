package kr.cosmoisland.cosmoislands.api.player;

import kr.cosmoisland.cosmoislands.api.Island;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandInternship {

    UUID getUniqueId();

    CompletableFuture<Integer> getMaxInternships();

    CompletableFuture<List<Island>> getHiredIslands();

}
