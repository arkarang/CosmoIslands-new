package kr.cosmoisland.cosmoislands.bukkit;

import com.minepalm.arkarangutils.bukkit.SimpleConfig;
import kr.msleague.mslibrary.database.MSLDatabases;
import kr.msleague.mslibrary.database.api.DatabaseConfig;

public class Conf extends SimpleConfig {

    protected Conf(CosmoIslandsBukkitBootstrap plugin, String fileName) {
        super(plugin, fileName);
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
    public int getGCInterval(){
        return 60;
    }

}
