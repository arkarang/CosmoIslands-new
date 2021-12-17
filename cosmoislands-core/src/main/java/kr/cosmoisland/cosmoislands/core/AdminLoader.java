package kr.cosmoisland.cosmoislands.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AdminLoader extends AbstractDataModel {
    public AdminLoader(String table, Database database) {
        super(table, database);
    }

    public static final byte ADMIN = 4, MOD = 2, USER = 0;

    @Override
    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` INT UNIQUE AUTO_INCREMENT, " +
                    "`uuid` VARCHAR(36), " +
                    "`level` TINYINT, "+
                    "PRIMARY KEY(`uuid`)) " +
                    "charset=utf8mb4");
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Boolean> isAdmin(UUID uuid){
        return isAdmin(uuid, ADMIN);
    }

    public CompletableFuture<Boolean> isModerator(UUID uuid){
        return isAdmin(uuid, MOD);
    }

    private CompletableFuture<Boolean> isAdmin(UUID uuid, byte mod) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `level` FROM "+table+" WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getByte(1) >= mod;
            }else
                return false;
        });
    }

    public CompletableFuture<Byte> getRank(UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `level` FROM "+table+" WHERE `uuid`=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getByte(1);
            }else
                return USER;
        });
    }
    
    public CompletableFuture<List<UUID>> getAdmins(byte rank){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `uuid` FROM "+table+" WHERE `level` >= ?");
            ps.setByte(1, rank);
            ResultSet rs = ps.executeQuery();
            List<UUID> list = new ArrayList<>();
            while (rs.next()){
                try{
                    list.add(UUID.fromString(rs.getString(1)));
                }catch (IllegalArgumentException ignored){

                }
            }
            return list;
        });
    }

    public CompletableFuture<Void> setAdmin(UUID uuid, byte rank){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`uuid`, `level`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `level`=?");
            ps.setString(1, uuid.toString());
            ps.setByte(2, rank);
            ps.setByte(3, rank);
            ps.execute();
            return null;
        });
    }
}
