package kr.cosmoislands.cosmoislands.api;

import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;

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
