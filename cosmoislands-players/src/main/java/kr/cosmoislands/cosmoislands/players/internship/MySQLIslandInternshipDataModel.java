package kr.cosmoislands.cosmoislands.players.internship;

import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandInternshipDataModel {

    private final MySQLDatabase database;
    private final String playersTable, maxInternTable;
    private final String islandTable;

    void init(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+playersTable+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`uuid` VARCHAR(36), " +
                    "`member_rank` TINYINT, "+
                    "PRIMARY KEY(`island_id`, `uuid`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
            PreparedStatement ps2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+maxInternTable+ " "+
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`max_internship` TINYINT DEFAULT 5, " +
                    "PRIMARY KEY(`island_id`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps2.execute();
        });
    }

    CompletableFuture<List<Integer>> getInternships(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `island_id` FROM "+playersTable+" " +
                    "WHERE `uuid`=? AND `member_rank`=?");
            ps.setString(1, uuid.toString());
            ps.setInt(2, MemberRank.INTERN.getPriority());
            ResultSet rs = ps.executeQuery();
            List<Integer> result = new ArrayList<>();
            while (rs.next()){
                result.add(rs.getInt(1));
            }
            return result;
        });
    }

    CompletableFuture<Integer> getMaxInternships(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `max_internship` FROM "+maxInternTable+" " +
                    "WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
            return 5;
        });
    }

    CompletableFuture<Void> setMaxInternships(UUID uuid, int value){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+maxInternTable+" " +
                    "(`uuid`, `max_internship`) VALUES(?, ?) " +
                    "ON DUPLICATE KEY UPDATE `max_internship`=VALUES(`max_internship`)");
            ps.setString(1, uuid.toString());
            ps.setInt(2, value);
            ps.execute();
            return null;
        });
    }

    CompletableFuture<Void> reset(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+maxInternTable+" " +
                    "WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ps.execute();
            return null;
        });
    }
}
