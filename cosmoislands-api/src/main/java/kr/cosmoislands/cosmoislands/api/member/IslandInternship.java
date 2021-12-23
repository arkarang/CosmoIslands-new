package kr.cosmoislands.cosmoislands.api.member;

import kr.cosmoislands.cosmoislands.api.Island;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandInternship {

    UUID getUniqueId();

    CompletableFuture<Integer> getMaxInternships();

    CompletableFuture<List<Island>> getHiredIslands();

}
