package kr.cosmoisland.cosmoislands.bukkit.test.utils;

import kr.cosmoisland.cosmoislands.core.Database;
import kr.msleague.mslibrary.database.MSLDatabases;
import kr.msleague.mslibrary.database.api.DatabaseConfig;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.NonNull;

import java.util.concurrent.Executors;

public class TestKit {

    @Getter
    private static final String ISLAND_TABLE, USER_TABLE, SETTINGS_TABLE, PERMISSION_TABLE, BANK_TABLE, DATA_TABLE, WARPS_TABLE, INTERNS_TABLE, REWARD_TABLE, REWARD_SETTINGS_TABLE;

    static{
        ISLAND_TABLE = "`cosmoislands_islands`";
        USER_TABLE = "`cosmoislands_users_test`";
        SETTINGS_TABLE = "`cosmoislands_settings_test`";
        PERMISSION_TABLE = "`cosmoislands_permissions_test`";
        DATA_TABLE = "`cosmoislands_island_data_test`";
        BANK_TABLE = "`cosmoislands_bank_test`";
        WARPS_TABLE = "`cosmoislands_warps_test`";
        INTERNS_TABLE = "`cosmoislands_interns_test`";
        REWARD_SETTINGS_TABLE = "`cosmoislands_reward_settings_test`";
        REWARD_TABLE = "`cosmoislands_reward_data_test`";
    }

    static Database db;

    @NonNull
    public static Database getDB(){
        String table = "`cosmoislands_islands`";
        if(db == null) {
            MySQLDatabase database = new MySQLDatabase(Executors.newScheduledThreadPool(4));
            database.connect(getProps());
            db = new Database(database, table);
        }
        db.create();
        return db;
    }

    private static DatabaseConfig getProps(){
        DatabaseConfig props = MSLDatabases.HIKARI.copy();
        props.setAddress("localhost");
        props.setPort(Integer.parseInt("3306"));
        props.setDatabase("test");
        props.setUser("root");
        props.setPassword("test");
        return props;

    }
}
