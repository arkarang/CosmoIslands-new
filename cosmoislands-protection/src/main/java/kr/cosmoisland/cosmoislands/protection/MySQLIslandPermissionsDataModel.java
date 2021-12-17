package kr.cosmoisland.cosmoislands.protection;

import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.core.AbstractDataModel;
import kr.cosmoisland.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MySQLIslandPermissionsDataModel extends AbstractDataModel {

    private final Map<IslandPermissions, MemberRank> defaultPermissions;

    public MySQLIslandPermissionsDataModel(String table,
                                           Database database,
                                           Map<IslandPermissions, MemberRank> defaultPermissions){
        super(table, database);
        this.defaultPermissions = defaultPermissions;
    }

    @Override
    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`permission` VARCHAR(16), " +
                    "`required` TINYINT, " +
                    "PRIMARY KEY(`island_id`, `permission`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES `cosmoislands_islands`(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid) {
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> delete(int id) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> setPermission(int id, IslandPermissions perm, MemberRank rank){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `permission`, `required`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `permission`=?, `required`=?");
            ps.setInt(1, id);
            for(int i = 0; i < 2; i++) {
                ps.setString(2+i*2, perm.name());
                ps.setInt(3+i*2, rank.getPriority());
            }
            return null;
        });
    }

    public CompletableFuture<List<IslandPermissions>> getPermissions(int id, MemberRank rank){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `permission` FROM "+table+" WHERE `island_id`=? AND `required`=?");
            ps.setInt(1, id);
            ps.setInt(2, rank.getPriority());
            ResultSet rs = ps.executeQuery();
            Set<IslandPermissions> set = new HashSet<>();
            while (rs.next()){
                try {
                    IslandPermissions perm = IslandPermissions.valueOf(rs.getString(1));
                    set.add(perm);
                }catch (IllegalArgumentException e){
                    continue;
                }
            }

            for(Map.Entry<IslandPermissions, MemberRank> entry : defaultPermissions.entrySet()){
                if(entry.getValue().getPriority() <= rank.getPriority())
                    set.add(entry.getKey());
            }

            return new ArrayList<>(set);
        });
    }

    public CompletableFuture<Map<IslandPermissions, MemberRank>> asMap(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `permission`, `required` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Map<IslandPermissions, MemberRank> map = new HashMap<>(defaultPermissions);
            while (rs.next()){
                try {
                    IslandPermissions perm = IslandPermissions.valueOf(rs.getString(1));
                    MemberRank rank = MemberRank.get(rs.getInt(2));
                    map.put(perm, rank);
                }catch (IllegalArgumentException e){
                    continue;
                }
            }

            return map;
        });
    }

    public CompletableFuture<Boolean> hasPermission(int id, IslandPermissions perm, MemberRank rank){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `required` FROM "+table+" WHERE `island_id`=? AND `permission`=? ");
            ps.setInt(1, id);
            ps.setString(2, perm.name());
            ResultSet rs = ps.executeQuery();
            MemberRank req = defaultPermissions.getOrDefault(perm, MemberRank.NONE);
            if (rs.next()){
                req = MemberRank.get(rs.getInt(1));
            }
            return rank.getPriority() >= req.getPriority();
        });
    }

}
