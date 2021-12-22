package kr.cosmoisland.cosmoislands.core;


import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;

import java.sql.PreparedStatement;
import java.util.HashMap;

public class Database extends AbstractDatabase {

    private final String islandTable;

    public Database(MySQLDatabase database, String islandTable) {
        super(database);
        this.islandTable = islandTable;
        create();

    }

    @Override
    public void create() {
        execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+islandTable+" " +
                    "(`island_id` BIGINT AUTO_INCREMENT, " +
                    "PRIMARY KEY(`island_id`, `uuid`)) " +
                    "charset=utf8mb4");
            ps.execute();
            return null;
        });

    }

}
