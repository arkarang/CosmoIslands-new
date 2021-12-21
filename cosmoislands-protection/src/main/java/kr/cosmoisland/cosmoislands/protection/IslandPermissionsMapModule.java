package kr.cosmoisland.cosmoislands.protection;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.IslandModule;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissionsMap;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class IslandPermissionsMapModule implements IslandModule<IslandPermissionsMap> {

    @Getter
    private final Logger logger;
    @Getter
    private final ImmutableMap<IslandPermissions, MemberRank> defaultValues;
    private final StaticIslandPermissionsMap staticPermissionsMap;

    public IslandPermissionsMapModule(Map<IslandPermissions, MemberRank> defaultValues, Logger logger){
        this.logger = logger;
        this.defaultValues = ImmutableMap.copyOf(defaultValues);
        this.staticPermissionsMap = new StaticIslandPermissionsMap(defaultValues);
    }

    @Override
    public CompletableFuture<IslandPermissionsMap> getAsync(int islandId) {
        return CompletableFuture.completedFuture(staticPermissionsMap);
    }

    @Override
    public IslandPermissionsMap get(int islandId) {
        return this.staticPermissionsMap;
    }

    @Override
    public void invalidate(int islandId) {

    }

    @Override
    public void onEnable(IslandService service) {
        service.getFactory().addLast("permissions", new IslandPermissionsLifecycle(this));
    }

    @Override
    public void onDisable(IslandService service) {

    }

}
