package kr.cosmoislands.cosmoislands.settings;

import kr.cosmoislands.cosmoislands.api.IslandComponent;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
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
        return getSettingAsync(IslandSetting.DISPLAY_NAME);
    }

    @Override
    public CompletableFuture<Void> setDisplayname(String name) throws IllegalArgumentException {
        return setSetting(IslandSetting.DISPLAY_NAME, name);
    }

    @Override
    public CompletableFuture<String> getSettingAsync(IslandSetting setting) {
        return model.getValue(islandId, setting);
    }

    @Override
    public String getSetting(IslandSetting settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> setSetting(IslandSetting setting, String value) {
        return model.setValue(islandId, setting, value);
    }

    @Override
    public CompletableFuture<Map<IslandSetting, String>> asMap() {
        return model.getSettings(islandId).thenApply(map->{
            HashMap<IslandSetting, String> hashMap = new HashMap<>();
            for (IslandSetting key : map.keySet()) {
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
