package kr.cosmoislands.cosmoislands.ignite;

import com.minepalm.manyworlds.bungee.utils.BungeeConfig;
import kr.msleague.mslibrary.database.MSLDatabases;
import kr.msleague.mslibrary.database.api.DatabaseConfig;

import java.util.List;

public class Conf extends BungeeConfig {

    protected Conf(CosmoIslandsBungee plugin, String fileName) {
        super(plugin, fileName, true);
    }


    public String getMySQLName(){
        return config.getString("CosmoDataSource.mysql", "default");
    }

    public String getRedisName(){
        return config.getString("CosmoDataSource.redis", "default");
    }

    List<String> getStorageServers(){
        return config.getStringList("StorageServers");
    }

    int getMaxIslands(){
        return config.getInt("MaxIslands", 100);
    }
}
