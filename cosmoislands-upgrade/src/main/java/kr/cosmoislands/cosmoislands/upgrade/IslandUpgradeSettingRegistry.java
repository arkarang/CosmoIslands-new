package kr.cosmoislands.cosmoislands.upgrade;

import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeType;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandUpgradeSettingRegistry {

    final UpgradeSettingsDataModel model;

    CompletableFuture<IslandUpgradeSettings> getSetting(IslandUpgradeType type){
        return model.get(type);
    }

    CompletableFuture<Void> setSetting(IslandUpgradeSettings settings){
        return model.insert(settings);
    }

}