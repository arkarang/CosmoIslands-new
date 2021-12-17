package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandPlayerLoader extends AbstractDataModel {
    public IslandPlayerLoader(String table, Database database) {
        super(table, database);
    }

    @Override
    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`uuid` VARCHAR(36), " +
                    "`member_rank` TINYINT, "+
                    "PRIMARY KEY(`island_id`, `uuid`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES `cosmoislands_islands`(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public CompletableFuture<IslandPlayer> get(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `island_id` FROM "+table+" WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            int id = Island.NIL_ID;
            if(rs.next()){
                id = rs.getInt(1);
            }
            return new IslandPlayer(id, uuid);
        });
    }

    public CompletableFuture<List<IslandPlayer>> getPlayers(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `uuid` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            List<IslandPlayer> list = new ArrayList<>();
            while (rs.next()){
                try {
                    list.add(new IslandPlayer(id, UUID.fromString(rs.getString(1))));
                }catch (IllegalArgumentException ignored){

                }
            }
            return list;
        });
    }

    public CompletableFuture<Void> add(int islandID, UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps1 = connection.prepareStatement("INSERT INTO "+table+" (`uuid`, `island_id`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `uuid`=?, `island_id`=?");
            ps1.setString(1, uuid.toString());
            ps1.setInt(2, islandID);
            ps1.execute();
            return null;
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

    public CompletableFuture<Void> delete(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ps.execute();
            return null;
        });
    }

}
