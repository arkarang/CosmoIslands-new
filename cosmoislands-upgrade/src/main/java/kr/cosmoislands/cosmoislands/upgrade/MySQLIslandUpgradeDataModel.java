package kr.cosmoislands.cosmoislands.upgrade;

import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoisland.cosmoislands.core.AbstractDataModel;
import kr.cosmoisland.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLIslandUpgradeDataModel extends AbstractDataModel {

    final String islandTable;

    public MySQLIslandUpgradeDataModel(String table, String islandTable, Database database) {
        super(table, database);
        this.islandTable = islandTable;
    }

    @Override
    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`type` VARCHAR(16), " +
                    "`level` INT DEFAULT 0, " +
                    "PRIMARY KEY(`island_id`, `type`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid) {
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Integer> getLevel(int islandId, IslandUpgradeType type){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `level` FROM "+table+" WHERE `island_id`=? AND `type`=?");
            ps.setInt(1, islandId);
            ps.setString(2, type.name());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }else
                return 0;
        });
    }

    public CompletableFuture<Void> setLevel(int islandId, IslandUpgradeType type, int level){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `type`, `level`) " +
                    "VALUES(?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE `type`=VALUES(`type`), `level`=VALUES(`level`)");
            ps.setInt(1, islandId);
            ps.setString(2, type.name());
            ps.setInt(3, level);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Map<IslandUpgradeType, Integer>> getLevels(int islandId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `type`, `level` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, islandId);
            ResultSet rs = ps.executeQuery();
            Map<IslandUpgradeType, Integer> map = new HashMap<>();
            while (rs.next()){
                try{
                    IslandUpgradeType type = IslandUpgradeType.valueOf(rs.getString(1));
                    int level = rs.getInt(2);
                    map.put(type, level);
                }catch (IllegalArgumentException ignored){

                }
            }
            return map;
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
