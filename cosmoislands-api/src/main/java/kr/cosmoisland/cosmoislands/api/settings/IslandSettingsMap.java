package kr.cosmoisland.cosmoislands.api.settings;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IslandSettingsMap extends IslandComponent {

    //generic
    CompletableFuture<String> getDisplayname();

    //generic
    CompletableFuture<Void> setDisplayname(String name) throws IllegalArgumentException;

    CompletableFuture<String> getSettingAsync(IslandSettings setting);

    String getSetting(IslandSettings settings);

    CompletableFuture<Void> setSetting(IslandSettings setting, String value);

    CompletableFuture<Map<IslandSettings, String>> asMap();
}
