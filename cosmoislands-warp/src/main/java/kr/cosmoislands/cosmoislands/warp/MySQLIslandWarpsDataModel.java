package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.warp.IslandWarp;
import kr.cosmoisland.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
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
                    "`permission_level` TINYINT DEFAULT " + MemberRank.MEMBER.getPriority()+", " +
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

    CompletableFuture<IslandWarp> getWarp(int islandId, String name){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `x`, `y`, `z`, `yaw`, `pitch`, `permission_level` WHERE `name`=? AND `island_id`=?");
            ps.setString(1, name);
            ps.setInt(2, islandId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                AbstractLocation location = getLocation(rs);
                int permissionLevel = rs.getInt(6);
                return new IslandWarp(name, MemberRank.get(permissionLevel), location);
            }else {
                return null;
            }
        });
    }

    CompletableFuture<List<IslandWarp>> getWarps(int islandId, MemberRank required){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `name`, `x`, `y`, `z`, `yaw`, `pitch`, `permission_level` WHERE `island_id`=? AND `permission_level` <= ?");
            ps.setInt(1, islandId);
            ps.setInt(2, required.getPriority());
            ResultSet rs = ps.executeQuery();
            List<IslandWarp> warps = new ArrayList<>();
            while (rs.next()){
                String name;
                MemberRank rank;
                double x, y, z;
                float yaw, pitch;
                name = rs.getString(1);
                x = rs.getDouble(2);
                y = rs.getDouble(3);
                z = rs.getDouble(4);
                yaw = rs.getFloat(5);
                pitch = rs.getFloat(6);
                rank = MemberRank.get(rs.getInt(7));
                if(!name.equals(MySQLIslandWarpsMap.SPAWN)) {
                    warps.add(new IslandWarp(name, rank, x, y, z, yaw, pitch));
                }
            }
            return warps;
        });
    }

    CompletableFuture<Boolean> updateWarpPermission(int islandId, String name, MemberRank newPermission){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `name` FROM "+table+" WHERE `island_id`=? AND `permission_level`=?");
            ps.setInt(1, islandId);
            ps.setInt(2, newPermission.getPriority());
            ResultSet rs = ps.executeQuery();
            boolean found = rs.next();
            if(found){
                PreparedStatement updateQuery = connection.prepareStatement("UPDATE "+table+" SET `permission_level`=? WHERE `island_id`=? AND `name`=?");
                updateQuery.setInt(1, newPermission.getPriority());
                updateQuery.setInt(2, islandId);
                updateQuery.setString(3, name);
                updateQuery.execute();
            }
            ps.execute();
            return found;
        });
    }

    CompletableFuture<Void> insertWarp(int islandId, IslandWarp location){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table + " " +
                    "(`island_id`, `name`, `permission_level`, `x`, `y`, `z`, `yaw`, `pitch`) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?) "+
                    "ON DUPLICATE KEY UPDATE `permission_level`=VALUES(`permission_level`), `x`=VALUES(`x`), `y`=VALUES(`y`), `z`=VALUES(`z`), `yaw`=VALUES(`yaw`), `pitch`=VALUES(`pitch`)");
            ps.setInt(1, islandId);
            ps.setString(2, location.getName());
            ps.setInt(3, location.getRank().getPriority());
            ps.setDouble(4, location.getX());
            ps.setDouble(5, location.getY());
            ps.setDouble(6, location.getZ());
            ps.setFloat(7, location.getYaw());
            ps.setFloat(8, location.getPitch());
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

    public CompletableFuture<Integer> count(int islandId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) AS total FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, islandId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }else
                return 0;
        });
    }
}
