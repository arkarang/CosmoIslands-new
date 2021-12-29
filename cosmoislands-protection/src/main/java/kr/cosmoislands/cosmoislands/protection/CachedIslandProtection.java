package kr.cosmoislands.cosmoislands.protection;

import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.IslandRegistry;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoislands.cosmoislands.api.protection.IslandProtection;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.world.IslandWorld;
import kr.cosmoislands.cosmoislands.core.utils.Cached;
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
    protected final IslandRegistry islandRegistry;
    protected final IslandPlayerRegistry playerRegistry;
    protected final ConcurrentHashMap<IslandPermissions, MemberRank> permissionsCache = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<UUID, Cached<MemberRank>> cachedMap = new ConcurrentHashMap<>();

    public CachedIslandProtection(int islandId,
                                  IslandPermissionsMap permissionsMap,
                                  IslandPlayersMap playersMap,
                                  IslandSettingsMap settingsMap,
                                  IslandRegistry islandRegistry,
                                  IslandPlayerRegistry playerRegistry,
                                  ImmutableMap<IslandPermissions, MemberRank> defaultValues){
        this.islandId = islandId;
        this.islandRegistry = islandRegistry;
        this.permissionsMap = permissionsMap;
        this.settingsMap = settingsMap;
        this.defaultValues = defaultValues;
        this.playersMap = playersMap;
        this.playerRegistry = playerRegistry;
    }

    @Override
    public CompletableFuture<Boolean> isPrivate() {
        return this.settingsMap.getSettingAsync(IslandSetting.PRIVATE).thenApply(Boolean::parseBoolean);
    }

    @Override
    public CompletableFuture<Void> setPrivate(boolean b) {
        CompletableFuture<Void> kickGuest;
        if(b) {
            kickGuest = islandRegistry.getIsland(islandId).thenCompose(island -> {
                if (island != null) {
                    IslandWorld world = island.getComponent(IslandWorld.class);
                    if (world != null) {
                        return world.getWorldHandler().runOperation("kick-guest").thenRun(()->{});
                    }
                }
                return CompletableFuture.completedFuture(null);
            });
        }else{
            kickGuest = CompletableFuture.completedFuture(null);
        }
        val setValue = this.settingsMap.setSetting(IslandSetting.PRIVATE, Boolean.toString(b));
        return CompletableFuture.allOf(kickGuest, setValue);
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
