package kr.cosmoisland.cosmoislands.core;


import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;

import java.sql.PreparedStatement;
import java.util.HashMap;

public class Database extends AbstractDatabase {

    private final HashMap<Class<? extends AbstractDataModel>, AbstractDataModel> loaders = new HashMap<>();
    private final String islandTable;

    public Database(MySQLDatabase database, String islandTable) {
        super(database);
        this.islandTable = islandTable;
        create();

        register(IslandRegistrationLoader.class, new IslandRegistrationLoader(islandTable, this));
        register(IslandPlayerLoader.class, new IslandPlayerLoader("cosmoislands_users", this));
        register(IslandTrackerLoader.class, new IslandTrackerLoader("cosmoislands_loaded_islands", this));
        register(AdminLoader.class, new AdminLoader("cosmoislands_admins", this));

    }

    @Override
    public void create() {
        execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+islandTable+" " +
                    "(`island_id` BIGINT AUTO_INCREMENT, " +
                    "`uuid` VARCHAR(36) UNIQUE, " +
                    "PRIMARY KEY(`island_id`, `uuid`)) " +
                    "charset=utf8mb4");
            ps.execute();
            return null;
        });

    }

    public void register(Class<? extends AbstractDataModel> clazz, AbstractDataModel loader){
        loaders.put(clazz, loader);
        loader.init();
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDataModel> T getLoader(Class<T> clazz){
        return (T)loaders.get(clazz);
    }

}
