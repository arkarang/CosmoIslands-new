package kr.cosmoisland.cosmoislands.protection;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.IslandCloud;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoisland.cosmoislands.api.protection.IslandProtection;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandProtectionRemote extends CachedIslandProtection {

    private final IslandCloud cloud;

    IslandProtectionRemote(int islandId,
                           IslandPermissionsMap permissionsMap,
                           IslandPlayersMap playersMap,
                           IslandSettingsMap settingsMap,
                           IslandPlayerRegistry playerRegistry,
                           ImmutableMap<IslandPermissions, MemberRank> defaultValues,
                           IslandCloud cloud) {
        super(islandId, permissionsMap, playersMap, settingsMap, playerRegistry, defaultValues);
        this.cloud = cloud;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return cloud.getLocated(islandId).thenApply(server->{
            if(server != null) {
                server.sync(islandId, IslandProtection.class);
            }
            return (T)IslandProtectionRemote.this;
        });
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public CompletableFuture<Void> update(UUID uuid) {
        return CompletableFuture.completedFuture(null);
    }
}
