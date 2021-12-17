package kr.cosmoisland.cosmoislands.settings;

import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettings;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandSettingsMap implements IslandSettingsMap {

    final int islandId;
    final IslandSettingsDataModel model;

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public CompletableFuture<String> getDisplayname() {
        return getSettingAsync(IslandSettings.DISPLAY_NAME);
    }

    @Override
    public CompletableFuture<Void> setDisplayname(String name) throws IllegalArgumentException {
        return setSetting(IslandSettings.DISPLAY_NAME, name);
    }

    @Override
    public CompletableFuture<String> getSettingAsync(IslandSettings setting) {
        return model.getValue(islandId, setting);
    }

    @Override
    public CompletableFuture<Void> setSetting(IslandSettings setting, String value) {
        return model.setValue(islandId, setting, value);
    }

    @Override
    public CompletableFuture<Map<IslandSettings, String>> asMap() {
        return model.getSettings(islandId).thenApply(map->{
            HashMap<IslandSettings, String> hashMap = new HashMap<>();
            for (IslandSettings key : map.keySet()) {
                try {
                    hashMap.put(key, map.get(key));
                }catch (IllegalArgumentException e){
                    continue;
                }
            }
            return hashMap;
        });
    }
}
