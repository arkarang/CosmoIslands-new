package kr.cosmoisland.cosmoislands.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandRegistrationLoader extends AbstractDataModel {
    public IslandRegistrationLoader(String table, Database database) {
        super(table, database);
    }

    @Override
    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`island_id` BIGINT AUTO_INCREMENT, " +
                    "`uuid` VARCHAR(36) UNIQUE, " +
                    "PRIMARY KEY(`island_id`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public CompletableFuture<Integer> create(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps1 = connection.prepareStatement("INSERT INTO "+table+" (`uuid`) VALUES(?)");
            PreparedStatement ps2 = connection.prepareStatement("SELECT LAST_INSERT_ID();");
            ps1.setString(1, uuid.toString());
            ps1.execute();
            ResultSet rs = ps2.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }else
                return -1;
        });
    }

    public CompletableFuture<Boolean> exists(int islandID){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `island_id` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, islandID);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        });
    }

    public CompletableFuture<Boolean> exists(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `island_id` FROM "+table+" WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        });
    }

    public CompletableFuture<Void> delete(int islandID){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, islandID);
            ps.execute();
            return null;
        });
    }
}
