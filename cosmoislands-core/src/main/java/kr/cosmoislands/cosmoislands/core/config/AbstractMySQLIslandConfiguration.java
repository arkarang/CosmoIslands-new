package kr.cosmoislands.cosmoislands.core.config;

import kr.cosmoislands.cosmoislands.api.IslandConfiguration;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoislands.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public abstract class AbstractMySQLIslandConfiguration implements IslandConfiguration {

    protected final MySQLPropertyDataModel model;
    protected static final String settingsPrefix,
            permissionsPrefix,
            worldBorderPrefix,
            upgradeSettingsPrefix,
            manyWorldsDatabasePrefix,
            levelLoreKey,
            levelLoreRegexKey;

    static{
        //마지막에 separator 하나 있음. 주의.
        settingsPrefix = "Default.IslandSetting.";
        permissionsPrefix = "Default.IslandPermissions.";
        upgradeSettingsPrefix = "Default.UpgradeSettings.";
        manyWorldsDatabasePrefix = "ManyWorldsDatabase.";
        worldBorderPrefix = "Default.WorldBorder.";

        //separator 없음.
        levelLoreRegexKey = "Default.Level.Regex";
        levelLoreKey = "Default.Level.Lore";
    }

    public CompletableFuture<Void> migrate(IslandConfiguration configuration){
        val future1 = this.setDefaultSettings(configuration.getDefaultSettings());
        val future2 = this.setDefaultUpgradeSettings(configuration.getDefaultUpgradeSettings());
        val future3 = this.setDefaultWorldBorder(configuration.getDefaultWorldBorder());
        val future4 = this.setLevelLoreKey(configuration.getLevelLore());
        val future5 = this.setLevelLorePattern(configuration.getLevelLorePattern().pattern());
        val future6 = this.setManyWorldsProperties(configuration.getManyWorldsProperties());
        val future7 = this.setDefaultPermissions(configuration.getDefaultPermissions());
        return CompletableFuture.allOf(future1, future2, future3, future4, future4, future5, future6, future7);
    }
    
    /**
     * warning: thread blocking
     */
    @Override
    public Map<IslandSetting, String> getDefaultSettings() {
        Map<String, String> map = model.getValuesLike(settingsPrefix);
        Map<IslandSetting, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().replace(settingsPrefix, "");
            try {
                IslandSetting setting = IslandSetting.valueOf(key);
                result.put(setting, entry.getValue());
            }catch (IllegalArgumentException e){
                
            }
        }
        return result;
    }

    public CompletableFuture<Void> setDefaultSettings(Map<IslandSetting, String> map){
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<IslandSetting, String> entry : map.entrySet()) {
                val future = CompletableFuture.runAsync(()->{
                    model.setValue(settingsPrefix+entry.getKey(), entry.getValue());
                }, service);
                futures.add(future);
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }finally {
            service.shutdown();
        }
    }

    /**
     * warning: thread blocking
     */
    @Override
    public Map<IslandPermissions, MemberRank> getDefaultPermissions() {
        Map<String, String> map = model.getValuesLike(permissionsPrefix);
        Map<IslandPermissions, MemberRank> result = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().replace(permissionsPrefix, "");
            String value = entry.getValue();
            try {
                IslandPermissions setting = IslandPermissions.valueOf(key);
                MemberRank rank = MemberRank.valueOf(value);
                result.put(setting, rank);
            }catch (IllegalArgumentException e){

            }
        }
        DebugLogger.log("getDefaultPermissions: "+map.size());
        return result;
    }

    public CompletableFuture<Void> setDefaultPermissions(Map<IslandPermissions, MemberRank> map){
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<IslandPermissions, MemberRank> entry : map.entrySet()) {
                val future = CompletableFuture.runAsync(() -> {
                    model.setValue(permissionsPrefix+entry.getKey().name(), entry.getValue().name()+"");
                }, service);
                futures.add(future);
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }finally {
            service.shutdown();
        }
    }

    /**
     * warning: thread blocking
     */
    @Override
    public Map<String, Integer> getDefaultWorldBorder() {
        Map<String, String> map = model.getValuesLike(worldBorderPrefix);
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().replace(worldBorderPrefix, "");
            int value = Integer.parseInt(entry.getValue());
            result.put(key, value);
        }
        return result;
    }
    
    public CompletableFuture<Void> setDefaultWorldBorder(Map<String, Integer> map){
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                val future = CompletableFuture.runAsync(()->{
                    model.setValue(worldBorderPrefix+entry.getKey(), entry.getValue()+"");
                }, service);
                futures.add(future);
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }finally {
            service.shutdown();
        }
    }

    @Override
    public boolean getUpdateUpgradeSettings() {
        return true;
    }

    protected abstract IslandUpgradeSettings getDefaultUpgradeSettings(IslandUpgradeType type);

    public abstract CompletableFuture<Void> setDefaultUpgradeSettings(Map<IslandUpgradeType, IslandUpgradeSettings> map);

    protected abstract CompletableFuture<Void> setDefaultUpgradeSettings(IslandUpgradeType type, IslandUpgradeSettings settings);

    /**
     * warning: thread blocking
     */
    @Override
    public Properties getManyWorldsProperties() {
        Map<String, String> map = model.getValuesLike(manyWorldsDatabasePrefix);
        Properties props = new Properties();
        props.setProperty("address", map.get(manyWorldsDatabasePrefix+"address"));
        props.setProperty("port", map.get(manyWorldsDatabasePrefix+"port"));
        props.setProperty("database", map.get(manyWorldsDatabasePrefix+"database"));
        props.setProperty("username", map.get(manyWorldsDatabasePrefix+"username"));
        props.setProperty("password", map.get(manyWorldsDatabasePrefix+"password"));
        return props;
    }

    public CompletableFuture<Void> setManyWorldsProperties(Properties properties){
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            List<CompletableFuture<?>> futures = new ArrayList<>();
            futures.add(model.setValue(manyWorldsDatabasePrefix+"address", properties.getProperty("address")));
            futures.add(model.setValue(manyWorldsDatabasePrefix+"port", properties.getProperty("port")));
            futures.add(model.setValue(manyWorldsDatabasePrefix+"database", properties.getProperty("database")));
            futures.add(model.setValue(manyWorldsDatabasePrefix+"username", properties.getProperty("username")));
            futures.add(model.setValue(manyWorldsDatabasePrefix+"password", properties.getProperty("password")));
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }finally {
            service.shutdown();
        }
    }

    /**
     * warning: thread blocking
     */
    @Override
    public Pattern getLevelLorePattern() {
        String pattern = model.getValue(levelLoreRegexKey);
        return Pattern.compile(pattern);
    }

    public CompletableFuture<Void> setLevelLorePattern(String pattern){
        return model.setValue(levelLoreRegexKey, pattern);
    }

    /**
     * warning: thread blocking
     */
    @Override
    public String getLevelLore() {
        return model.getValue(levelLoreKey);
    }

    public CompletableFuture<Void> setLevelLoreKey(String lore){
        return model.setValue(levelLoreKey, lore);
    }
}
