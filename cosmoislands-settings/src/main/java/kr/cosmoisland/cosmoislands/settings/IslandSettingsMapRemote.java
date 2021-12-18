package kr.cosmoisland.cosmoislands.settings;

import kr.cosmoisland.cosmoislands.api.IslandCloud;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;

import java.util.concurrent.CompletableFuture;

public class IslandSettingsMapRemote extends CosmoIslandSettingsMap{

    private final int islandId;
    private final IslandCloud islandCloud;

    public IslandSettingsMapRemote(int islandId, MySQLIslandSettingsMap mysql, RedisIslandSettingsMap redis, IslandCloud cloud) {
        super(mysql, redis);
        this.islandId = islandId;
        this.islandCloud = cloud;
    }

    @Override
    public String getSetting(IslandSetting settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return islandCloud.getLocated(islandId).thenApply(server->{
            if(server != null) {
                server.sync(islandId, IslandSettingsMap.class);
            }
            return (T)IslandSettingsMapRemote.this;
        });
    }
}
