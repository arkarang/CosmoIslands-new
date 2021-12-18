package kr.cosmoisland.cosmoislands.api.settings;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IslandSettingsMap extends IslandComponent {

    //generic
    CompletableFuture<String> getDisplayname();

    //generic
    CompletableFuture<Void> setDisplayname(String name) throws IllegalArgumentException;

    CompletableFuture<String> getSettingAsync(IslandSetting setting);

    String getSetting(IslandSetting settings);

    CompletableFuture<Void> setSetting(IslandSetting setting, String value);

    CompletableFuture<Map<IslandSetting, String>> asMap();
}
