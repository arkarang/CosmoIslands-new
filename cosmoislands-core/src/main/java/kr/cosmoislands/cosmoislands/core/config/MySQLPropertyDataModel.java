package kr.cosmoislands.cosmoislands.core.config;

import kr.cosmoislands.cosmoislands.core.Database;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLPropertyDataModel {

    private final String table;
    private final MySQLDatabase database;

    public void init(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` INT AUTO_INCREMENT UNIQUE, " +
                    "`key` VARCHAR(128), " +
                    "`value` TEXT, " +
                    "PRIMARY KEY(`key`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public String getValue(String key){
        return database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `value` FROM "+table+" WHERE `key`=?");
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1);
            }
            return null;
        });
    }

    public Map<String, String> getValuesLike(String keyLike){
        return database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `key`, `value` FROM "+table+" WHERE `key` LIKE ?");
            ps.setString(1, keyLike + "%");
            ResultSet rs = ps.executeQuery();
            Map<String, String> map = new HashMap<>();
            while (rs.next()){
                String key = rs.getString(1);
                String value = rs.getString(2);
                map.put(key, value);
            }
            return map;
        });
    }

    public Map<String, String> getAll(){
        return database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `key`, `value` WHERE "+table);
            ResultSet rs = ps.executeQuery();
            Map<String, String> map = new HashMap<>();
            while (rs.next()){
                String key = rs.getString(1);
                String value = rs.getString(2);
                map.put(key, value);
            }
            return map;
        });
    }

    public CompletableFuture<Void> setValue(String key, String value){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+
                    " (`key`, `value`) " +
                    "VALUES(?, ?) " +
                    "ON DUPLICATE KEY UPDATE `value`=VALUES(`value`)");
            ps.setString(1, key);
            ps.setString(2, value);
            ps.execute();
            return null;
        });
    }

}
