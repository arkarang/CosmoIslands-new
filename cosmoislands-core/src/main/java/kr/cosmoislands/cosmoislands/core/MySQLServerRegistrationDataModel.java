package kr.cosmoislands.cosmoislands.core;

import kr.cosmoislands.cosmoislands.api.IslandServer;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class MySQLServerRegistrationDataModel {

    final String table;
    final Database database;

    void init(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` INT AUTO_INCREMENT UNIQUE, " +
                    "`serverName` VARCHAR(64), " +
                    "`serverType` VARCHAR(64), " +
                    "PRIMARY KEY(`serverName`)) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    CompletableFuture<Void> insert(String server, IslandServer.Type type){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`serverName`, `serverType`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `serverType`=?");
            ps.setString(1, server);
            ps.setString(2, type.name());
            ps.setString(3, type.name());
            ps.execute();
            return null;
        });
    }

    CompletableFuture<IslandServer.Type> selectType(String serverName){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `serverType` FROM "+table+" WHERE `serverName`=?");
            ps.setString(1, serverName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                String typeName = rs.getString(1);
                return IslandServer.Type.valueOf(typeName);
            }
            return null;
        });
    }

    CompletableFuture<List<String>> selectServers(IslandServer.Type type){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `serverName` FROM "+table+" WHERE `serverType`=?");
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            List<String> list = new ArrayList<>();
            while (rs.next()){
                list.add(rs.getString(1));
            }
            return list;
        });
    }

    CompletableFuture<Void> delete(String serverName){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `serverName`=?");
            ps.setString(1, serverName);
            ps.execute();
            return null;
        });
    }

    CompletableFuture<Map<String, IslandServer.Type>> all(){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `serverName`, `serverType` FROM "+table);
            ResultSet rs = ps.executeQuery();
            Map<String, IslandServer.Type> map = new HashMap<>();
            while (rs.next()){
                try{
                    map.put(rs.getString(1), IslandServer.Type.valueOf(rs.getString(2)));
                }catch (IllegalArgumentException ignored){

                }
            }
            return map;
        });
    }

}
