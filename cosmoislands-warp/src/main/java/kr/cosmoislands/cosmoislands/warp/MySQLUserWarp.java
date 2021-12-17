package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoisland.cosmoislands.api.warp.IslandLocation;
import kr.cosmoisland.cosmoislands.api.warp.IslandUserWarp;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLUserWarp implements IslandUserWarp {

    private final UUID uuid;
    private final MySQLUserWarpsModel model;

    @Override
    public String getName() {
        return uuid.toString();
    }

    @Override
    public CompletableFuture<IslandLocation> getLocation() {
        return model.getLocation(getName());
    }

    @Override
    public CompletableFuture<Void> setLocation(IslandLocation loc) {
        return model.setLocation(uuid, loc);
    }

    @Override
    public CompletableFuture<Void> delete() {
        return model.delete(uuid);
    }
}
