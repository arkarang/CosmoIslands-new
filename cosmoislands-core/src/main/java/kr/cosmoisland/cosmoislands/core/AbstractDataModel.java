package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.IslandDataModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public abstract class AbstractDataModel implements IslandDataModel {

    @Getter
    protected final String table;
    protected final Database database;

    public abstract void init();

    protected CompletableFuture<Integer> getInteger(int id, String column){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `"+column+"` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }else
                return -1;
        });
    }


    protected CompletableFuture<Void> setInteger(int id, String column, int value){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `"+column+"`)"+" VALUES(?, ?)"+" ON DUPLICATE KEY UPDATE `"+column+"`=?");
            ps.setInt(1, id);
            ps.setInt(2, value);
            ps.setInt(3, value);
            ps.execute();
            return null;
        });
    }

    protected CompletableFuture<String> getString(int id, String column){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `"+column+"` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1);
            }else
                return null;
        });
    }

    protected CompletableFuture<Void> setString(int id, String column, String value){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `"+column+"`)"+" VALUES(?, ?)"+" ON DUPLICATE KEY UPDATE `"+column+"`=?");
            ps.setInt(1, id);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.execute();
            return null;
        });
    }

    protected CompletableFuture<Boolean> getBoolean(int id, String column){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `"+column+"` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getBoolean(1);
            }else
                return null;
        });
    }

    protected CompletableFuture<Void> setBoolean(int id, String column, boolean value){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `"+column+"`)"+" VALUES(?, ?)"+" ON DUPLICATE KEY UPDATE `"+column+"`=?");
            ps.setInt(1, id);
            ps.setBoolean(2, value);
            ps.setBoolean(3, value);
            ps.execute();
            return null;
        });
    }
}
