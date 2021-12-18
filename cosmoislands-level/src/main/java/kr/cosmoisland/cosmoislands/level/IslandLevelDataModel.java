package kr.cosmoisland.cosmoislands.level;

import kr.cosmoisland.cosmoislands.core.AbstractDataModel;
import kr.cosmoisland.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandLevelDataModel extends AbstractDataModel {

    private final String islandTable;

    public IslandLevelDataModel(String table, String islandTable, Database database) {
        super(table, database);
        this.islandTable = islandTable;
    }

    @Override
    public void init() {
        database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`level` INT DEFAULT 0, " +
                    "PRIMARY KEY(`island_id`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`) VALUES(?)");
            ps.setInt(1, id);
            ps.execute();
            return null;
        });
    }

    CompletableFuture<Integer> getLevel(int islandId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `level` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, islandId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }else
                return 0;
        });
    }

    CompletableFuture<Void> setLevel(int islandId, int level){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `level`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `level`=VALUES(`level`)");
            ps.setInt(1, islandId);
            ps.setInt(2, level);
            ps.execute();
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> delete(int id) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ps.execute();
            return null;
        });
    }
}
