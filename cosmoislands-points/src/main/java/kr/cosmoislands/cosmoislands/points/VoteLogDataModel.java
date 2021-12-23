package kr.cosmoislands.cosmoislands.points;

import kr.cosmoislands.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VoteLogDataModel {


    final String table;
    final Database database;

    public VoteLogDataModel(String table, Database database) {
        this.table = table;
        this.database = database;
    }

    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT AUTO_INCREMENT, " +
                    "`uuid` VARCHAR(36), " +
                    "`voted_island` BIGINT, "+
                    "`voted_time` LONG, " +
                    "PRIMARY KEY(`column_id`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public CompletableFuture<Void> log(UUID uuid, int islandID, long time){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`uuid`, `voted_island`, `voted_time`) VALUES(?, ?, ?)");
            ps.setString(1, uuid.toString());
            ps.setInt(2, islandID);
            ps.setLong(3, time);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Long> getLatestVotedTime(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `voted_time` FROM "+table+" WHERE `uuid`=? ORDER BY `voted_time` DESC LIMIT 1");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return rs.getLong(1);
            return 0L;
        });
    }
}
