package kr.cosmoislands.cosmoislands.level;

import kr.cosmoislands.cosmoislands.api.IslandRanking;
import kr.cosmoislands.cosmoislands.core.AbstractDataModel;
import kr.cosmoislands.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
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

    CompletableFuture<Void> addLevel(int islandId, int value){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("UPDATE " + table + " SET `level`=`level`+" + value + " WHERE `island_id`=?");
            ps.setInt(1, islandId);
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

    CompletableFuture<List<IslandRanking.RankingData>> getTopOf(int count){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ( SELECT `island_id`, `level`, dense_rank() over (order by `level`) as ranking FROM "+table+") ranks WHERE ranks.ranking <= ?");
            ps.setInt(1, count);
            ResultSet rs = ps.executeQuery();
            List<IslandRanking.RankingData> list = new ArrayList<>();
            while (rs.next()){
                int islandId = rs.getInt(1);
                int level = rs.getInt(2);
                IslandRanking.RankingData data = new IslandRanking.RankingData(islandId, level);
                list.add(data);
            }
            return list;
        });
    }

    CompletableFuture<Integer> getRank(int islandId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ( SELECT `island_id`, `level`, dense_rank() over (order by `level`) as ranking FROM "+table+") ranks WHERE ranks.island_id = ?");
            ps.setInt(1, islandId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(3);
            }
            return -1;
        });
    }
}
