package kr.cosmoisland.cosmoislands.api.generic;

import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IslandSettingsMap extends IslandComponent {

    //generic
    CompletableFuture<String> getDisplayname();

    //generic
    CompletableFuture<Void> setDisplayname(String name) throws IllegalArgumentException;

    CompletableFuture<String> getSetting(IslandSettings setting);

    CompletableFuture<Void> setSetting(IslandSettings setting, String value);

    CompletableFuture<Map<IslandSettings, String>> asMap();
}
