package kr.cosmoislands.cosmoislands.core.config;

import kr.cosmoislands.cosmoislands.api.IslandConfiguration;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class MySQLIslandConfiguration implements IslandConfiguration {

    @Override
    public Map<IslandSetting, String> getDefaultSettings() {
        return null;
    }

    @Override
    public Map<IslandPermissions, MemberRank> getDefaultPermissions() {
        return null;
    }

    @Override
    public Map<String, Integer> getDefaultWorldBorder() {
        return null;
    }

    @Override
    public boolean getUpdateUpgradeSettings() {
        return true;
    }

    @Override
    public Map<IslandUpgradeType, IslandUpgradeSettings> getDefaultUpgradeSettings() {
        return null;
    }

    @Override
    public Properties getManyWorldsProperties() {
        return null;
    }

    @Override
    public Pattern getLevelLorePattern() {
        return null;
    }
}
