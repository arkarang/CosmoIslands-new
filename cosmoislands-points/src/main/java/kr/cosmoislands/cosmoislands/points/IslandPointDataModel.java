package kr.cosmoislands.cosmoislands.points;

import kr.cosmoislands.cosmoislands.api.IslandDataModel;
import kr.cosmoislands.cosmoislands.api.IslandRanking;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandPointDataModel implements IslandDataModel {

    final String table;
    final String islandTable;
    final Database database;

    @Override
    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`points` INT, " +
                    "PRIMARY KEY(`island_id`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid) {
        return setPoints(id, 0);
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

    CompletableFuture<Integer> getPoints(int islandId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `points` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, islandId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }else
                return 0;
        });
    }

    CompletableFuture<Void> setPoints(int islandId, int points){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `points`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `points`=VALUES(`points`)");
            ps.setInt(1, islandId);
            ps.setInt(2, points);
            ps.execute();
            return null;
        });
    }

    CompletableFuture<Void> addPoints(int islandId, int points){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `points`) VALUES(?, `points`+?) ON DUPLICATE KEY UPDATE `points`=`points`+?");
            ps.setInt(1, islandId);
            ps.setInt(2, points);
            ps.setInt(3, points);
            ps.execute();
            return null;
        });
    }

    CompletableFuture<List<IslandRanking.RankingData>> getTopOf(int count){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ( SELECT `island_id`, `points`, dense_rank() over (order by `points`) as ranking FROM "+table+") ranks WHERE ranks.ranking <= ?");
            ps.setInt(1, count);
            ResultSet rs = ps.executeQuery();
            List<IslandRanking.RankingData> list = new ArrayList<>();
            while (rs.next()){
                int islandId = rs.getInt(1);
                int points = rs.getInt(2);
                IslandRanking.RankingData data = new IslandRanking.RankingData(islandId, points);
                list.add(data);
            }
            return list;
        });
    }

    CompletableFuture<Integer> getRank(int islandId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ( SELECT `island_id`, `points`, dense_rank() over (order by `points`) as ranking FROM "+table+") ranks WHERE ranks.island_id = ?");
            ps.setInt(1, islandId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(3);
            }
            return -1;
        });
    }
}
