package kr.cosmoislands.cosmoislands.bungee;

import com.minepalm.arkarangutils.bungee.BungeeConfig;
import kr.cosmoisland.cosmoislands.api.IslandServer;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conf extends BungeeConfig {

    protected Conf(CosmoIslandsBungee plugin) {
        super(plugin, "config.yml", true);
    }

    public String getMySQLName(){
        return config.getString("CosmoDataSource.mysql", "default");
    }

    public String getRedisName(){
        return config.getString("CosmoDataSource.redis", "default");
    }

    public Boolean updateServerRegistration(){
        return config.getBoolean("UpdateServerList");
    }

    public Map<IslandServer.Type, List<String>> getServerList(){
        Configuration section = config.getSection("ServerList");
        Map<IslandServer.Type, List<String>> map = new HashMap<>();
        for (String key : section.getKeys()){
            IslandServer.Type type = IslandServer.Type.valueOf(key);
            map.put(type, new ArrayList<>());
            section.getStringList(key).forEach(name->map.get(type).add(name));
        }
        return map;
    }

    int getMaxIslands(){
        return config.getInt("MaxIslands", 100);
    }
}
