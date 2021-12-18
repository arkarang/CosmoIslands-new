package kr.cosmoisland.cosmoislands.bukkit.config;

import com.google.common.collect.ImmutableMap;
import com.minepalm.arkarangutils.bukkit.Pair;
import com.minepalm.arkarangutils.bukkit.SimpleConfig;
import com.minepalm.helloteleport.LocationData;
import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import lombok.Getter;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class YamlIslandConfig extends SimpleConfig {

    public static final String INTERN = "interns", MEMBER = "members", CHEST= "chest", SIZE = "size";
    static AtomicBoolean hdb = new AtomicBoolean(false);

    @Getter
    static HeadDatabaseAPI HDBAPI;

    @Getter
    int defaultIslandSize;
    @Getter
    Pair<String, LocationData> fallback;
    @Getter
    AbstractLocation defaultSpawnLocation;
    HashMap<String, Boolean> enabled = new HashMap<>();

    HashMap<String, ReinforceSetting> map = new HashMap<>();
    HashMap<Integer, Integer> achievementLevels = new HashMap<>();

    @Getter
    final ImmutableMap<IslandPermissions, MemberRank> defaultPermissions;
    @Getter
    final ImmutableMap<IslandSetting, String> defaultSettings;

    public YamlIslandConfig(JavaPlugin plugin) throws IllegalStateException{
        super(plugin, "globalconfig.yml");
        this.defaultIslandSize = readDefaultIslandSize();
        this.defaultSpawnLocation = readSpawnLocation();
        readReinforceSetting(INTERN);
        readReinforceSetting(MEMBER);
        readReinforceSetting(CHEST);
        readReinforceSetting(SIZE);
        readAchievementLevels();
        defaultPermissions = readDefaultPermissions();
        defaultSettings = readDefaultSettings();
        fallback = readFallback();
    }

    public ReinforceSetting getReinforceSetting(String type){
        return map.get(type);
    }

    public void setHeads(){
        map.values().forEach(rs-> rs.initIcons(HDBAPI));
    }

    public boolean isEnable(String type){
        return enabled.getOrDefault(type, false);
    }

    public int getAchievementLevel(int index){
        return achievementLevels.getOrDefault(index, -1);
    }

    private ImmutableMap<IslandSetting, String> readDefaultSettings(){
        HashMap<IslandSetting, String> map = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("default.settings");
        for(String key : section.getKeys(false)) {
            try{
                IslandSetting setting = IslandSetting.valueOf(key);
                map.put(setting, section.getString(key));
            }catch (IllegalArgumentException ignored){

            }
        }
        return ImmutableMap.copyOf(map);
    }

    private ImmutableMap<IslandPermissions, MemberRank> readDefaultPermissions(){
        HashMap<IslandPermissions, MemberRank> map = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("default.permissions");
        for(String key : section.getKeys(false)) {
            try{
                IslandPermissions perm = IslandPermissions.valueOf(key);
                map.put(perm, MemberRank.get(section.getInt(key)));
            }catch (IllegalArgumentException ignored){

            }
        }
        return ImmutableMap.copyOf(map);
    }

    private void readAchievementLevels(){
        ConfigurationSection section = config.getConfigurationSection("default.rewards.level");
        for(String key : section.getKeys(false)) {
            int lv = config.getInt(key);
            achievementLevels.put(Integer.parseInt(key), lv);
        }
    }

    private void readReinforceSetting(String type){
        int maxLevel = readMaxLevel(type);
        try {
            ReinforceSetting setting = new ReinforceSetting(type, maxLevel, readReinforceComponents(maxLevel, type));
            map.put(type, setting);
            enabled.put(type, true);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            enabled.put(type, false);
        }
    }

    private List<ReinforceSetting.Component> readReinforceComponents(int maxLevel, String type) throws IllegalArgumentException{
        List<ReinforceSetting.Component> list = new ArrayList<>();
        for(int i = 0; i <= maxLevel; i++){
            list.add(readReinforceComponent(type, i));
        }
        return list;
    }

    private ReinforceSetting.Component readReinforceComponent(String str, int level) throws IllegalArgumentException{
        Optional<ConfigurationSection> section = Optional.ofNullable(config.getConfigurationSection("default.reinforce."+str));
        if(section.isPresent()){
            ConfigurationSection subSection = section.get().getConfigurationSection(level+"");
            String head = subSection.getString("head", "null");
            double cost = subSection.getDouble("cost");
            int value = subSection.getInt("value");
            ReinforceSetting.Component comp = new ReinforceSetting.Component(head, level, value, cost);
            comp.setItem(new ItemStack(Material.GRASS));
            return comp;
        }else
            throw new IllegalArgumentException("the configuration section does not exist: level: "+level+" type: "+str);
    }

    private int readMaxLevel(String type){
        return config.getInt("default.reinforce_max_level."+type);
    }

    private int readDefaultIslandSize(){
        return config.getInt("default.initial_island_size");
    }

    private AbstractLocation readSpawnLocation(){
        double x, y, z;
        float pitch = 0, yaw = 0;
        ConfigurationSection section = config.getConfigurationSection("default.spawn_location");
        x = section.getDouble("x");
        y = section.getDouble("y");
        z = section.getDouble("z");
        pitch = (float)section.getDouble("pitch");
        yaw = (float)section.getDouble("yaw");
        return new AbstractLocation(x, y, z, yaw, pitch);
    }

    private Pair<String, LocationData> readFallback(){
        String server, world;
        double x, y, z;
        float pitch = 0, yaw = 0;
        ConfigurationSection section = config.getConfigurationSection("default.fallback");
        server = section.getString("server");
        world = section.getString("world");
        x = section.getDouble("x");
        y = section.getDouble("y");
        z = section.getDouble("z");
        pitch = (float)section.getDouble("pitch");
        yaw = (float)section.getDouble("yaw");
        return new Pair<>(server, new LocationData(world, x, y, z, pitch, yaw));
    }

    public static void setAPI(HeadDatabaseAPI api){
        if(api == null){
            hdb.set(false);
        }else{
            YamlIslandConfig.HDBAPI = api;
            hdb.set(true);
        }
    }

}
