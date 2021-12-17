package kr.cosmoisland.cosmoislands.api.warp;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TeleportExecutor {

    CompletableFuture<WarpResult> teleportPlayer(UUID uuid, IslandLocation location);

}
