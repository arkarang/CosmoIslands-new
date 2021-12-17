package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

public class MySQLIslandWarpsDataModel extends MySQLAbstractLocationDataModel{

    private final String islandTable;

    public MySQLIslandWarpsDataModel(Database database, String table, String islandTable) {
        super(database, table);
        this.islandTable = islandTable;
    }

    void init(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` INT, " +
                    "`name` VARCHAR(64), " +
                    "`x` DOUBLE, " +
                    "`y` DOUBLE, " +
                    "`z` DOUBLE, " +
                    "`yaw` FLOAT, " +
                    "`pitch` FLOAT, " +
                    "PRIMARY KEY(`island_id`, `name`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    CompletableFuture<AbstractLocation> getLocation(int islandId, String name){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `x`, `y`, `z`, `yaw`, `pitch` WHERE `name`=? AND `island_id`=?");
            ps.setString(1, name);
            ps.setInt(2, islandId);
            ResultSet rs = ps.executeQuery();
            return getLocation(rs);
        });
    }

    CompletableFuture<Void> setLocation(int islandId, String name, AbstractLocation location){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `name`, `x`, `y`, `z`, `yaw`, `pitch`) VALUES(?, ?, ?, ?, ?, ?, ?)"+" " +
                    "ON DUPLICATE KEY UPDATE `x`=VALUES(`x`), `y`=VALUES(`y`), `z`=VALUES(`z`), `yaw`=VALUES(`yaw`), `pitch`=VALUES(`pitch`)");
            ps.setInt(1, islandId);
            ps.setString(2, name);
            ps.setDouble(3, location.getX());
            ps.setDouble(4, location.getY());
            ps.setDouble(5, location.getZ());
            ps.setFloat(6, location.getYaw());
            ps.setFloat(7, location.getPitch());
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> delete(int islandId, String name) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `island_id`=? AND `name`=?");
            ps.setInt(1, islandId);
            ps.setString(2, name);
            ps.execute();
            return null;
        });
    }
}
