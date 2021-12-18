package kr.cosmoisland.cosmoislands.settings;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.core.utils.Cached;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CachedIslandSettingsMap extends CosmoIslandSettingsMap{

    ImmutableMap<IslandSetting, String> defaultValues;
    ConcurrentHashMap<IslandSetting, Cached<String>> cached = new ConcurrentHashMap<>();

    public CachedIslandSettingsMap(MySQLIslandSettingsMap mysql, RedisIslandSettingsMap redis, ImmutableMap<IslandSetting, String> defaultValues) {
        super(mysql, redis);
        this.defaultValues = defaultValues;
    }

    @Override
    CompletableFuture<Void> migrate() {
        return super.migrate()
                .thenCompose(ignored-> this.asMap())
                .thenAccept(map -> {
                    for (Map.Entry<IslandSetting, String> entry : map.entrySet()) {
                        cached.put(entry.getKey(), new Cached<>(entry.getValue(), ()-> this.getSettingAsync(entry.getKey())));
                    }
                    for (IslandSetting islandSetting : defaultValues.keySet()) {
                        if(!cached.containsKey(islandSetting)){
                            cached.put(islandSetting, new Cached<>(defaultValues.get(islandSetting), ()->this.getSettingAsync(islandSetting)));
                        }
                    }
                });
    }

    @Override
    public CompletableFuture<Void> setSetting(IslandSetting setting, String value) {
        if(this.cached.containsKey(setting)) {
            this.cached.get(setting).set(value);
        }
        return super.setSetting(setting, value);
    }

    @Override
    public String getSetting(IslandSetting setting) {
        if(cached.containsKey(setting)) {
            return cached.get(setting).get();
        }else{
            return defaultValues.get(setting);
        }
    }

}
