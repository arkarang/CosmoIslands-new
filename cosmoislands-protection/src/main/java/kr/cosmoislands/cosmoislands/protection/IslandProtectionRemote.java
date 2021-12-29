package kr.cosmoislands.cosmoislands.protection;

import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.api.IslandCloud;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoislands.cosmoislands.api.protection.IslandProtection;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandProtectionRemote extends CachedIslandProtection {

    private final IslandCloud cloud;

    IslandProtectionRemote(int islandId,
                           IslandRegistry registry,
                           IslandPermissionsMap permissionsMap,
                           IslandPlayersMap playersMap,
                           IslandSettingsMap settingsMap,
                           IslandPlayerRegistry playerRegistry,
                           ImmutableMap<IslandPermissions, MemberRank> defaultValues,
                           IslandCloud cloud) {
        super(islandId, permissionsMap, playersMap, settingsMap, registry, playerRegistry, defaultValues);
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
