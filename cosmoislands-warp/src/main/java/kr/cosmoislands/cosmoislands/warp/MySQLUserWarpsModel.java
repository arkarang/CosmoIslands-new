package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoislands.cosmoislands.api.warp.IslandLocation;
import kr.cosmoislands.cosmoislands.core.Database;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLUserWarpsModel extends MySQLAbstractLocationDataModel{

    String islandTable;

    public MySQLUserWarpsModel(Database database, String table, String islandTable) {
        super(database, table);
        this.islandTable = islandTable;
    }

    void init(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`uuid` VARCHAR(36), " +
                    "`name` VARCHAR(32), " +
                    "`island_id` BIGINT, " +
                    "`x` DOUBLE, " +
                    "`y` DOUBLE, " +
                    "`z` DOUBLE, " +
                    "`yaw` FLOAT, " +
                    "`pitch` FLOAT, " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE, " +
                    "PRIMARY KEY(`uuid`, `name`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    CompletableFuture<IslandLocation> getLocation(UUID uuid, String name){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `x`, `y`, `z`, `yaw`, `pitch`, `island_id` WHERE `name`=? AND `uuid`=?");
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return getIslandLocation(rs);
        });
    }

    CompletableFuture<Void> setLocation(UUID uuid, String name, IslandLocation location){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`uuid`, `name`, `island_id`, `x`, `y`, `z`, `yaw`, `pitch`) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?)"+" " +
                    "ON DUPLICATE KEY UPDATE `island_id`=VALUES(`island_id`), `x`=VALUES(`x`), `y`=VALUES(`y`), `z`=VALUES(`z`), `yaw`=VALUES(`yaw`), `pitch`=VALUES(`pitch`)");
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setInt(3, location.getIslandID());
            ps.setDouble(4, location.getX());
            ps.setDouble(5, location.getY());
            ps.setDouble(6, location.getZ());
            ps.setFloat(7, location.getYaw());
            ps.setFloat(8, location.getPitch());
            ps.execute();
            return null;
        });
    }

    @Nullable
    protected IslandLocation getIslandLocation(ResultSet rs) throws SQLException {
        if(rs.next()){
            double x, y, z;
            float yaw, pitch;
            x = rs.getDouble(1);
            y = rs.getDouble(2);
            z = rs.getDouble(3);
            yaw = rs.getFloat(4);
            pitch = rs.getFloat(5);
            int islandId = rs.getInt(6);
            return new IslandLocation(islandId, x, y, z, yaw, pitch);
        }else
            return null;
    }

    public CompletableFuture<Void> delete(UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `name`=?");
            ps.setString(1, uuid.toString());
            ps.execute();
            return null;
        });
    }
}
