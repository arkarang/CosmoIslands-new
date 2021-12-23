package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoislands.cosmoislands.api.warp.IslandLocation;
import kr.cosmoislands.cosmoislands.api.warp.IslandUserWarp;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLUserWarp implements IslandUserWarp {

    private static final String HOME  = "HOME";

    private final UUID uuid;
    private final MySQLUserWarpsModel model;

    @Override
    public CompletableFuture<IslandLocation> getLocation() {
        return model.getLocation(uuid, HOME);
    }

    @Override
    public CompletableFuture<Void> setLocation(IslandLocation loc) {
        return model.setLocation(uuid, HOME, loc);
    }

    @Override
    public CompletableFuture<Void> delete() {
        return model.delete(uuid);
    }
}
