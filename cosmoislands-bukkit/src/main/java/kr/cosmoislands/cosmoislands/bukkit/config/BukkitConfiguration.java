package kr.cosmoislands.cosmoislands.bukkit.config;

import com.minepalm.arkarangutils.bukkit.SimpleConfig;
import kr.cosmoislands.cosmoislands.api.IslandConfiguration;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoislands.cosmoislands.bukkit.CosmoIslandsBukkit;
import kr.cosmoislands.cosmoislands.upgrade.UpgradeSettingImpl;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class BukkitConfiguration extends SimpleConfig {

    public BukkitConfiguration(CosmoIslandsBukkit plugin) {
        super(plugin, "config.yml");
    }

    public boolean isDebug(){
        return config.getBoolean("Debug", false);
    }

    public String getRedisName(){
        return config.getString("CosmoDataSource.redis");
    }

    public String getMySQLName(){
        return config.getString("CosmoDataSource.mysql");
    }

}
