package kr.cosmoisland.cosmoislands.api;

import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeType;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public interface IslandConfiguration {

    Map<IslandSetting, String> getDefaultSettings();

    Map<IslandPermissions, MemberRank> getDefaultPermissions();

    Map<String, Integer> getDefaultWorldBorder();

    boolean getUpdateUpgradeSettings();

    Map<IslandUpgradeType, IslandUpgradeSettings> getDefaultUpgradeSettings();

    Properties getManyWorldsProperties();

    Pattern getLevelLorePattern();
}
