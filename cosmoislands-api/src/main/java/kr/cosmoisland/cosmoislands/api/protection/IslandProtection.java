package kr.cosmoisland.cosmoislands.api.protection;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandProtection extends IslandComponent {

    CompletableFuture<Boolean> isPrivate();

    CompletableFuture<Void> setPrivate(boolean b);

    boolean hasPermission(UUID uuid, IslandPermissions permissions);

    CompletableFuture<Void> update(UUID uuid);

}
