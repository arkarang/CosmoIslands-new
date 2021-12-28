package kr.cosmoislands.cosmoislands.world;

import com.google.common.collect.ImmutableMap;
import kr.cosmoislands.cosmoislands.api.IslandDataModel;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLIslandWorldDataModel implements IslandDataModel {

    private final String table;
    private final String islandTable;
    private final Database database;
    private final ImmutableMap<String, Integer> defaultValues;

    @Override
    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`key` VARCHAR(36), " +
                    "`value` INT, "+
                    "PRIMARY KEY(`island_id`, `key`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid) {
        return CompletableFuture.completedFuture(null);
    }

    CompletableFuture<Integer> getMaxX(int islandId){
        return getValue(islandId, "maxX");
    }

    CompletableFuture<Integer> getMinX(int islandId){
        return getValue(islandId, "minX");
    }

    CompletableFuture<Integer> getMaxZ(int islandId){
        return getValue(islandId, "maxZ");
    }

    CompletableFuture<Integer> getMinZ(int islandId){
        return getValue(islandId, "minZ");
    }

    CompletableFuture<Void> setMaxX(int islandId, int value){
        return setValue(islandId, "maxX", value);
    }

    CompletableFuture<Void> setMinX(int islandId, int value){
        return setValue(islandId, "minX", value);
    }

    CompletableFuture<Void> setMaxZ(int islandId, int value){
        return setValue(islandId, "maxZ", value);
    }

    CompletableFuture<Void> setMinZ(int islandId, int value){
        return setValue(islandId, "minZ", value);
    }

    CompletableFuture<Integer> getLength(int islandId, int defaultValue){
        return sub(islandId, "maxX", "minX", defaultValue);
    }

    CompletableFuture<Integer> getWeight(int islandId, int defaultValue){
        return sub(islandId, "maxZ", "minZ", defaultValue);
    }

    CompletableFuture<Integer> getValue(int islandId, String key){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `value` FROM "+table+" WHERE `island_id`=? AND `key`=?");
            ps.setInt(1, islandId);
            ps.setString(2, key);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return rs.getInt(1);
            else
                return defaultValues.get(key);
        });
    }

    /*
    SELECT
		a - b as total
    FROM
		(SELECT
			`key` AS kay,
			(SELECT `value` FROM `test` WHERE `key`='key1') as a,
			(SELECT `value` FROM `test` WHERE `key`='key2') as b
			FROM test t WHERE `key`='key1'
		) q
     */
    CompletableFuture<Integer> add(int islandId, String key1, String key2, int defaultValue){
        return database.executeAsync(connection -> {
            PreparedStatement hasKey1 = connection.prepareStatement("SELECT `key` FROM "+table+" WHERE `key`=?");
            PreparedStatement hasKey2 = connection.prepareStatement("SELECT `key` FROM "+table+" WHERE `key`=?");
            hasKey1.setString(1, key1);
            hasKey2.setString(1, key2);
            ResultSet hasKey1ResultSet = hasKey1.executeQuery();
            ResultSet hasKey2ResultSet = hasKey2.executeQuery();
            if(hasKey1ResultSet.next() && hasKey2ResultSet.next()){
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT "+
                                "a + b AS total "+
                                "FROM "+
                                "(SELECT " +
                                "(SELECT `value` FROM "+table+" WHERE `key`=?) as a, "+
                                "(SELECT `value` FROM "+table+" WHERE `key`=?) as b "+
                                "FROM "+table+" WHERE `key`=? AND `island_id`=?) q");
                ps.setString(1, key1);
                ps.setString(2, key2);
                ps.setString(3, key1);
                ps.setInt(4, islandId);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt(1);
                }
            }
            return defaultValue;
        });
    }

    CompletableFuture<Integer> sub(int islandId, String key1, String key2, int defaultValue){
        return database.executeAsync(connection -> {
            PreparedStatement hasKey1 = connection.prepareStatement("SELECT `key` FROM "+table+" WHERE `key`=?");
            PreparedStatement hasKey2 = connection.prepareStatement("SELECT `key` FROM "+table+" WHERE `key`=?");
            hasKey1.setString(1, key1);
            hasKey2.setString(1, key2);
            ResultSet hasKey1ResultSet = hasKey1.executeQuery();
            ResultSet hasKey2ResultSet = hasKey2.executeQuery();
            if(hasKey1ResultSet.next() && hasKey2ResultSet.next()){
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT "+
                                "a - b AS total "+
                                "FROM "+
                                "(SELECT " +
                                "(SELECT `value` FROM "+table+" WHERE `key`=?) as a, "+
                                "(SELECT `value` FROM "+table+" WHERE `key`=?) as b "+
                                "FROM "+table+" WHERE `key`=? AND `island_id`=?) q");
                ps.setString(1, key1);
                ps.setString(2, key2);
                ps.setString(3, key1);
                ps.setInt(4, islandId);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt(1);
                }
            }
            return defaultValue;
        });
    }

    CompletableFuture<Void> setValue(int islandId, String key, int value){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `key`, `value`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `key`=?, `value`=?");
            ps.setInt(1, islandId);
            ps.setString(2, key);
            ps.setInt(3, value);
            ps.setString(4, key);
            ps.setInt(5, value);
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
