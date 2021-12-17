package kr.cosmoisland.cosmoislands.level;

import kr.cosmoisland.cosmoislands.api.IslandDataModel;
import kr.cosmoisland.cosmoislands.core.AbstractDataModel;
import kr.cosmoisland.cosmoislands.core.Database;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandAchievementsDataModel extends AbstractDataModel {

    private final String islandTable;

    public IslandAchievementsDataModel(String table, String islandTable, Database database) {
        super(table, database);
        this.islandTable = islandTable;
    }

    public void init() {
        database.execute(connection -> {
            PreparedStatement ps2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`achievement_id` INT, " +
                    "`status` BOOLEAN DEFAULT FALSE, " +
                    "PRIMARY KEY(`island_id`, `achievement_id`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps2.execute();
        });
    }

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid) {
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Boolean> isAchieved(int islandId, int achievementId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `status` FROM "+table+" WHERE `island_id`=? AND `achievement_id`=?");
            ps.setInt(1, islandId);
            ps.setInt(2, achievementId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getBoolean(1);
            }else
                return false;
        });
    }

    public CompletableFuture<Void> setAchieved(int islandId, int achievementId, boolean achieved){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `achievement_id`, `status`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `status`=?");
            ps.setInt(1, islandId);
            ps.setInt(2, achievementId);
            ps.setBoolean(3, achieved);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Map<Integer, Boolean>> getAchievements(int islandId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `achievement_id`, `status` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, islandId);
            ResultSet rs = ps.executeQuery();
            Map<Integer, Boolean> map = new HashMap<>();
            while (rs.next()){
                map.put(rs.getInt(1), rs.getBoolean(2));
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
