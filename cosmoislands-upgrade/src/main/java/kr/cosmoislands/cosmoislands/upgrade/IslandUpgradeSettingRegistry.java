package kr.cosmoislands.cosmoislands.upgrade;

import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandUpgradeSettingRegistry {

    final UpgradeSettingsDataModel model;

    public CompletableFuture<IslandUpgradeSettings> getSetting(IslandUpgradeType type){
        return model.get(type);
    }

    public CompletableFuture<Void> setSetting(IslandUpgradeSettings settings){
        return model.insert(settings);
    }

}
