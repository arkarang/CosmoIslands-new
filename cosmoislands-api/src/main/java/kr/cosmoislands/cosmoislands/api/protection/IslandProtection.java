package kr.cosmoislands.cosmoislands.api.protection;

import kr.cosmoislands.cosmoislands.api.IslandComponent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandProtection extends IslandComponent {

    byte COMPONENT_ID = 9;

    CompletableFuture<Boolean> isPrivate();

    CompletableFuture<Void> setPrivate(boolean b);

    boolean hasPermission(UUID uuid, IslandPermissions permissions);

    CompletableFuture<Void> update(UUID uuid);

}
