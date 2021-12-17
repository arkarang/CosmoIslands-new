package kr.cosmoisland.cosmoislands.protection;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.generic.IslandSettings;
import kr.cosmoisland.cosmoislands.api.generic.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoisland.cosmoislands.api.protection.IslandProtection;
import kr.cosmoisland.cosmoislands.core.utils.Cached;
import lombok.val;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CachedIslandProtection implements IslandProtection {

    private static final Cached<MemberRank> defaultRank = new Cached<>(MemberRank.NONE, ()->CompletableFuture.completedFuture(MemberRank.NONE));

    protected final int islandId;
    protected final ImmutableMap<IslandPermissions, MemberRank> defaultValues;
    protected final IslandPermissionsMap permissionsMap;
    protected final IslandPlayersMap playersMap;
    protected final IslandSettingsMap settingsMap;
    protected final IslandPlayerRegistry playerRegistry;
    protected final ConcurrentHashMap<IslandPermissions, MemberRank> permissionsCache = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<UUID, Cached<MemberRank>> cachedMap = new ConcurrentHashMap<>();

    public CachedIslandProtection(int islandId,
                                  IslandPermissionsMap permissionsMap,
                                  IslandPlayersMap playersMap,
                                  IslandSettingsMap settingsMap,
                                  IslandPlayerRegistry playerRegistry,
                                  ImmutableMap<IslandPermissions, MemberRank> defaultValues){
        this.islandId = islandId;
        this.permissionsMap = permissionsMap;
        this.settingsMap = settingsMap;
        this.defaultValues = defaultValues;
        this.playersMap = playersMap;
        this.playerRegistry = playerRegistry;
        sync();
    }

    @Override
    public CompletableFuture<Boolean> isPrivate() {
        return this.settingsMap.getSetting(IslandSettings.PRIVATE).thenApply(Boolean::parseBoolean);
    }

    @Override
    public CompletableFuture<Void> setPrivate(boolean b) {
        return this.settingsMap.setSetting(IslandSettings.PRIVATE, Boolean.toString(b));
    }

    @Override
    public boolean hasPermission(UUID uuid, IslandPermissions permissions) {
        int userPriority = cachedMap.getOrDefault(uuid, defaultRank).get().getPriority();
        int requiredPriority = permissionsCache.getOrDefault(permissions, defaultValues.get(permissions)).getPriority();
        return userPriority >= requiredPriority;
    }

    @Override
    public CompletableFuture<Void> update(UUID uuid) {
        CompletableFuture<Void> future;
        if (cachedMap.containsKey(uuid)) {
            cachedMap.get(uuid).get();
            future = CompletableFuture.completedFuture(null);
        }else{
            future = playersMap.getRank(playerRegistry.get(uuid)).thenAccept(rank->{
                if (rank != null) {
                    cachedMap.put(uuid, new Cached<>(rank, () -> playersMap.getRank(playerRegistry.get(uuid))));
                }else {
                    cachedMap.remove(uuid);
                }
            });
        }
        return future;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        val userPermissionsSyncFuture = playersMap.getMembers().thenAccept(map->{
            synchronized (cachedMap) {
                cachedMap.clear();
                for (Map.Entry<UUID, MemberRank> entry : map.entrySet()) {
                    cachedMap.put(entry.getKey(), new Cached<>(entry.getValue(), () -> {
                        return playersMap.getRank(playerRegistry.get(entry.getKey()));
                    }));
                }
            }
        });
        val permissionsSyncFuture = permissionsMap.asMap().thenAccept(map->{
            synchronized (permissionsCache) {
                permissionsCache.clear();
                permissionsCache.putAll(map);
            }
        });
        return CompletableFuture.allOf(userPermissionsSyncFuture, permissionsSyncFuture).thenApply(ignored->(T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        cachedMap.clear();
        permissionsCache.clear();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean validate() {
        return true;
    }
}
