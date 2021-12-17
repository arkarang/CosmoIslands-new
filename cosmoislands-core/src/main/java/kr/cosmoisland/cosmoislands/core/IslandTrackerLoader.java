package kr.cosmoisland.cosmoislands.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class IslandTrackerLoader extends AbstractDataModel {
    public IslandTrackerLoader(String table, Database database) {
        super(table, database);
    }

    @Override
    public void init() {
        database.execute(connection ->{
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`server` VARCHAR(32), " +
                    "`status` TINYINT, " +
                    "PRIMARY KEY(`island_id`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES `cosmoislands_islands`(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public CompletableFuture<Void> updateIsland(int id, String server, byte status){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `server`, `status`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `server`=?, `status`=?");
            ps.setInt(1, id);
            ps.setString(2, server);
            ps.setByte(3, status);
            ps.setString(4, server);
            ps.setByte(5, status);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> updateIsland(int id, byte status){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `status`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `status`=?");
            ps.setInt(1, id);
            ps.setByte(2, status);
            ps.setByte(3, status);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> unregisterIsland(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Optional<String>> getLoadedServer(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `server` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            String name = rs.next() ? rs.getString(1) : null;
            return Optional.ofNullable(name);
        });
    }

    public CompletableFuture<Boolean> isExist(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `server` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        });
    }

    public CompletableFuture<Boolean> statusEquals(int id, byte b){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `server` FROM "+table+" WHERE `island_id`=? AND `status`=?");
            ps.setInt(1, id);
            ps.setByte(2, b);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        });
    }

    public CompletableFuture<Byte> getStatus(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `status` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getByte(1) : IslandTracker.OFFLINE;
        });
    }

    public CompletableFuture<Void> unregisterAll(String name){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE  FROM "+table+" WHERE `server`=?");
            ps.setString(1, name);
            ps.execute();
            return null;
        });
    }
}
