package kr.cosmoisland.cosmoislands.bungee;

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

    /**
     * @deprecated CosmoDataSource 사용으로 인해 쓰질 않아요.
     */
    @Deprecated
    DatabaseConfig getDatabaseProperties(){
        DatabaseConfig props = MSLDatabases.HIKARI.copy();
        props.setAddress(config.getString("Database.address"));
        props.setPort(Integer.parseInt(config.getString("Database.port")));
        props.setDatabase(config.getString("Database.database"));
        props.setUser(config.getString("Database.username"));
        props.setPassword(config.getString("Database.password"));
        return props;
    }

    List<String> getStorageServers(){
        return config.getStringList("StorageServers");
    }

    int getMaxIslands(){
        return config.getInt("MaxIslands", 100);
    }
}
