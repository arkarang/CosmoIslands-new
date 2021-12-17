package kr.cosmoisland.cosmoislands.protection;

import com.google.common.collect.ImmutableMap;
import com.minepalm.hellobungee.api.HelloEveryone;
import kr.cosmoisland.cosmoislands.api.IslandCloud;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMapModule;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoisland.cosmoislands.api.protection.IslandProtection;
import kr.cosmoisland.cosmoislands.protection.hellobungee.ProtectionUpdatePacketAdapter;
import kr.cosmoisland.cosmoislands.protection.hellobungee.ProtectionUpdatePacketExecutor;
import kr.cosmoisland.cosmoislands.settings.IslandSettingsModule;
import lombok.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class IslandProtectionModule implements IslandModule<IslandProtection> {

    HelloEveryone networkModule;
    IslandPermissionsMapModule permissionsMapModule;
    IslandPlayersMapModule playersMapModule;
    IslandSettingsModule settingsModule;
    IslandPlayerRegistry playerRegistry;
    IslandCloud cloud;
    @Getter(AccessLevel.PUBLIC)
    Logger logger;

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
    
    void invalidate(int islandId){
        map.remove(islandId);
    }

    @Override
    public void onEnable(IslandService service) {
        networkModule.getGateway().registerAdapter(new ProtectionUpdatePacketAdapter());
        networkModule.getHandler().registerExecutor(new ProtectionUpdatePacketExecutor(this));
        service.getFactory().addLast("protection", new IslandProtectionLifecycle(this));
        playersMapModule.getStrategyRegistry().addStrategy("protection", new IslandProtectionPlayerModificationStrategy());
    }

    @Override
    public void onDisable(IslandService service) {
        this.map.clear();
    }
}
