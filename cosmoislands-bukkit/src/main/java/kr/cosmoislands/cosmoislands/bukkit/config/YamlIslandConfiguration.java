package kr.cosmoislands.cosmoislands.bukkit.config;

import com.minepalm.arkarangutils.bukkit.SimpleConfig;
import com.minepalm.manyworlds.bukkit.ManyWorldsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.api.IslandConfiguration;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoislands.cosmoislands.bukkit.CosmoIslandsBukkit;
import kr.cosmoislands.cosmoislands.upgrade.UpgradeSettingImpl;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class YamlIslandConfiguration extends SimpleConfig implements IslandConfiguration {

    public YamlIslandConfiguration(CosmoIslandsBukkit plugin) {
        super(plugin, "config.yml");
    }

    public String getRedisName(){
        return config.getString("CosmoDataSource.redis");
    }

    public String getMySQLName(){
        return config.getString("CosmoDataSource.mysql");
    }

    @Override
    public Map<IslandSetting, String> getDefaultSettings() throws IllegalArgumentException{
        ConfigurationSection section = config.getConfigurationSection("DefaultIslandSettings");
        Map<IslandSetting, String> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            try{
                map.put(IslandSetting.valueOf(key), section.getString(key));
            }catch (IllegalArgumentException ignored){

            }
        }
        validateDefaultSettings(map);
        return map;
    }

    private void validateDefaultSettings(Map<IslandSetting, String> map) throws IllegalArgumentException{
        for (IslandSetting value : IslandSetting.values()) {
            if(!map.containsKey(value)){
                throw new IllegalArgumentException("IslandSetting "+value.name()+" is not found!");
            }
        }
    }

    @Override
    public Map<IslandPermissions, MemberRank> getDefaultPermissions() {
        ConfigurationSection section = config.getConfigurationSection("DefaultIslandPermissions");
        Map<IslandPermissions, MemberRank> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            IslandPermissions permission = null;
            MemberRank rank = null;

            try {
                permission = IslandPermissions.valueOf(key);
            }catch (IllegalArgumentException ignored){

            }

            try {
                rank = MemberRank.valueOf(section.getString(key));
            }catch (IllegalArgumentException e){
                throw new IllegalArgumentException("MemberRank "+section.getString(key)+" is not exist");
            }

            if(permission != null){
                map.put(permission, rank);
            }
        }
        validateDefaultPermissions(map);
        return map;
    }

    void validateDefaultPermissions(Map<IslandPermissions, MemberRank> map){
        for (IslandPermissions value : IslandPermissions.values()) {
            if(!map.containsKey(value)){
                throw new IllegalArgumentException("IslandPermission "+value.name()+" is not found!");
            }
        }
    }

    @Override
    public Map<String, Integer> getDefaultWorldBorder() {
        int weight, length;
        weight = config.getInt("DefaultWorldBorder.weight");
        length = config.getInt("DefaultWorldBorder.length");
        int maxX, maxZ;
        int minX, minZ;
        maxX = weight/2;
        maxZ = length/2;
        minX = - (weight/2);
        minZ = - (length/2);
        Map<String, Integer> map = new HashMap<>();
        map.put("maxX", maxX);
        map.put("maxZ", maxZ);
        map.put("minX", minX);
        map.put("minZ", minZ);
        map.put("weight", weight);
        map.put("length", length);
        return map;
    }

    @Override
    public boolean getUpdateUpgradeSettings() {
        return config.getBoolean("UpdateUpgradeSettings");
    }

    @Override
    public Map<IslandUpgradeType, IslandUpgradeSettings> getDefaultUpgradeSettings() {
        Map<IslandUpgradeType, IslandUpgradeSettings> map = new HashMap<>();
        for (IslandUpgradeType value : IslandUpgradeType.values()) {
            if(config.contains("DefaultUpgradeSettings."+value.name())){
                map.put(value, new UpgradeSettingImpl(value, getDefaultUpgradeSettings(value)));
            }
        }
        return map;
    }

    Map<Integer, IslandUpgradeSettings.PairData> getDefaultUpgradeSettings(IslandUpgradeType type) {
        ConfigurationSection section = config.getConfigurationSection("DefaultUpgradeSettings."+type.name());
        if(section == null){
            throw new NullPointerException("Configuration of IslandUpgradeSettings "+type+" is null");
        }

        Map<Integer, IslandUpgradeSettings.PairData> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection subSection = section.getConfigurationSection(key);
            int value = subSection.getInt("value");
            int cost = subSection.getInt("cost");
            IslandUpgradeSettings.PairData pairData = new IslandUpgradeSettings.PairData(value, cost);
            map.put(Integer.parseInt(key), pairData );
        }
        return map;
    }

    @Override
    public Properties getManyWorldsProperties() {
        Properties props = new Properties();
        props.setProperty("address", config.getString("ManyWorldsDatabase.address"));
        props.setProperty("port", config.getString("ManyWorldsDatabase.port"));
        props.setProperty("database", config.getString("ManyWorldsDatabase.database"));
        props.setProperty("username", config.getString("ManyWorldsDatabase.username"));
        props.setProperty("password", config.getString("ManyWorldsDatabase.password"));
        return props;
    }

    @Override
    public Pattern getLevelLorePattern() {
        return Pattern.compile(config.getString("LevelLorePattern"));
    }
}
