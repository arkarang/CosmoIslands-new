package kr.cosmoislands.cosmoislands.level;

import kr.cosmoislands.cosmoislands.api.level.IslandRewardData;
import kr.cosmoislands.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IslandRewardDataModel {

    private final String table;
    private final Database database;
    private final RewardDataFactory factory;

    public IslandRewardDataModel(Database database, String table, RewardDataFactory factory) {
        this.database = database;
        this.table = table;
        this.factory = factory;
    }

    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`id` INT, " +
                    "`requiredLevel` TINYINT, " +
                    "`type` VARCHAR(32), " +
                    "`data` TEXT, " +
                    "PRIMARY KEY(`id`) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    CompletableFuture<IslandRewardData> get(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `requiredLevel`, `type`, `data` FROM "+table+" WHERE `id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                int requiredLevel = rs.getInt(1);
                String type = rs.getString(2);
                String data = rs.getString(3);
                Class<?> clazz = factory.getClassKey(type);
                return factory.build(id, requiredLevel, clazz, data);
            }else
                return null;
        });
    }

    CompletableFuture<Void> insert(IslandRewardData data){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`id`, `requiredLevel`, `type`, `data`) VALUES(?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE `requiredLevel`=VALUES(`requiredLevel`), `type`=VALUES(`type`), `data`=VALUES(`data`)");
            ps.setInt(1, data.getId());
            ps.setInt(2, data.getRequiredLevel());
            ps.setString(3, data.getClass().getSimpleName());
            ps.setString(4, factory.serialize(data));
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> setRequiredLevel(int id, int level) {
        return get(id).thenAccept(data->{
            if(data != null){
                database.executeAsync(connection -> {
                    PreparedStatement ps = connection.prepareStatement("UPDATE "+table+" SET `requiredLevel`=? WHERE `island_id`=?");
                    ps.setInt(1, level);
                    ps.setInt(2, id);
                    ps.execute();
                    return null;
                });
            }
        });
    }

    CompletableFuture<Void> delete(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `id`=?");
            ps.setInt(1, id);
            ps.execute();
            return null;
        });
    }

    CompletableFuture<List<IslandRewardData>> getAll(){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `id`, `requiredLevel`, `type`, `data` FROM "+table);
            ResultSet rs = ps.executeQuery();
            List<IslandRewardData> list = new ArrayList<>();
            while (rs.next()){
                int id = rs.getInt(1);
                int requiredLevel = rs.getInt(2);
                String type = rs.getString(3);
                String data = rs.getString(4);
                Class<?> clazz = factory.getClassKey(type);
                IslandRewardData rewardData = factory.build(id, requiredLevel, clazz, data);
                list.add(rewardData);
            }
            return list;
        });
    }
}
