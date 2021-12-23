package kr.cosmoislands.cosmoislands.protection;

import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.settings.IslandSettingsModule;
import kr.cosmoislands.cosmoislands.api.IslandCloud;
import kr.cosmoislands.cosmoislands.api.IslandModule;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMap;
import kr.cosmoislands.cosmoislands.api.member.IslandPlayersMapModule;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoislands.cosmoislands.api.protection.IslandProtection;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class IslandProtectionModule implements IslandModule<IslandProtection> {

    final IslandPermissionsMapModule permissionsMapModule;
    final IslandPlayersMapModule playersMapModule;
    final IslandSettingsModule settingsModule;
    final IslandPlayerRegistry playerRegistry;
    final IslandCloud cloud;
    @Getter(AccessLevel.PUBLIC)
    final Logger logger;

    ConcurrentHashMap<Integer, IslandProtection> map = new ConcurrentHashMap<>();

    IslandProtection createLocalProtection(int islandId){
        IslandProtection protection;
        IslandPlayersMap playersMap = playersMapModule.get(islandId);
        IslandPermissionsMap permissionsMap = permissionsMapModule.get(islandId);
        ImmutableMap<IslandPermissions, MemberRank> defaultValues = permissionsMapModule.getDefaultValues();
        IslandSettingsMap settingsMap = settingsModule.get(islandId);
        protection = new CachedIslandProtection(islandId, permissionsMap, playersMap, settingsMap, playerRegistry, defaultValues);
        return protection;
    }

    IslandProtection createRemoteProtection(int islandId){
        IslandProtection protection;
        IslandPlayersMap playersMap = playersMapModule.get(islandId);
        IslandPermissionsMap permissionsMap = permissionsMapModule.get(islandId);
        ImmutableMap<IslandPermissions, MemberRank> defaultValues = permissionsMapModule.getDefaultValues();
        IslandSettingsMap settingsMap = settingsModule.get(islandId);
        protection = new IslandProtectionRemote(islandId, permissionsMap, playersMap, settingsMap, playerRegistry, defaultValues, cloud);
        return protection;
    }

    void register(int islandId, IslandProtection protection){
        map.put(islandId, protection);
    }

    @Override
    @SneakyThrows
    public CompletableFuture<IslandProtection> getAsync(int islandId) {
        return CompletableFuture.completedFuture(get(islandId));
    }

    @Override
    public IslandProtection get(int islandId) {
        return map.get(islandId);
    }

    @Override
    public void invalidate(int islandId){
        map.remove(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        service.getFactory().addLast("protection", new IslandProtectionLifecycle(this));
        playersMapModule.getStrategyRegistry().addStrategy("protection", new IslandProtectionPlayerModificationStrategy());
    }

    @Override
    public void onDisable(IslandService service) {
        this.map.clear();
    }
}
