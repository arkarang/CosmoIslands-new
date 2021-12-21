package kr.cosmoislands.cosmoislands.bukkit.config;

import com.minepalm.arkarangutils.bukkit.SimpleConfig;
import kr.cosmoisland.cosmoislands.api.IslandConfiguration;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.bukkit.CosmoIslandsBukkit;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class YamlIslandConfiguration extends SimpleConfig implements IslandConfiguration {

    public YamlIslandConfiguration(CosmoIslandsBukkit plugin) {
        super(plugin, "config.yml");
    }

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
    public Properties getManyWorldsProperties() {
        return null;
    }

    @Override
    public Pattern getLevelLorePattern() {
        return null;
    }
}
