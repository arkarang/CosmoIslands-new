package kr.comsoislands.comsoislands.member;

import kr.cosmoisland.cosmoislands.api.IslandDataModel;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.core.AbstractDataModel;
import kr.cosmoisland.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayersMapDataModel extends AbstractDataModel implements IslandDataModel {

    final String islandTable;

    public PlayersMapDataModel(String table, String islandTable, Database database) {
        super(table, database);
        this.islandTable = islandTable;
    }

    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`uuid` VARCHAR(36), " +
                    "`member_rank` TINYINT, "+
                    "PRIMARY KEY(`island_id`, `uuid`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `uuid`, `member_rank`) VALUES(?, ?, ?) ");
            ps.setInt(1, id);
            ps.setString(2, uuid.toString());
            ps.setInt(3, MemberRank.OWNER.getPriority());
            ps.execute();
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> delete(int id) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ps.execute();;
            return null;
        });
    }
    
    public CompletableFuture<UUID> getOwner(int id) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `uuid` FROM "+table+" WHERE `island_id`=? AND `member_rank`=?");
            ps.setInt(1, id);
            ps.setInt(2, MemberRank.OWNER.getPriority());
            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    return UUID.fromString(rs.getString(1));
                }
            }catch (IllegalArgumentException e){
                return null;
            }
            return null;
        });
    }
    
    public CompletableFuture<Void> setOwner(int islandId, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("UPDATE "+table+" SET `island`=?, `uuid`=?");
            ps.setInt(1, islandId);
            ps.setString(2, uuid.toString());
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> setRank(int islandId, UUID uuid, MemberRank rank) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `uuid`, `member_rank`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `member_rank`=?");
            ps.setInt(1, islandId);
            ps.setString(2, uuid.toString());
            ps.setInt(3, rank.getPriority());
            ps.setInt(4, rank.getPriority());
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<MemberRank> getRank(int islandId, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `member_rank` FROM "+table+" WHERE `island_id`=? AND `uuid`=?");
            ps.setInt(1, islandId);
            ps.setString(2, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return MemberRank.get(rs.getInt(1));
            }
            return MemberRank.NONE;
        });
    }
    
    public CompletableFuture<Void> removeMember(int islandId, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" WHERE `island_id`=? AND `uuid`=? AND `member_rank` < ?");
            ps.setInt(1, islandId);
            ps.setString(2, uuid.toString());
            ps.setInt(3, MemberRank.OWNER.getPriority());
            ps.execute();
            return null;
        });
    }
    
    public CompletableFuture<Void> addMember(int islandId, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `uuid`, `member_rank`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `member_rank`=?");
            ps.setInt(1, islandId);
            ps.setString(2, uuid.toString());
            ps.setInt(3, MemberRank.MEMBER.getPriority());
            ps.setInt(4, MemberRank.MEMBER.getPriority());
            ps.execute();
            return null;
        });
    }
    
    public CompletableFuture<Boolean> isMember(int islandId, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `island_id` FROM "+table+" WHERE `island_id`=? AND `uuid`=?");
            ps.setInt(1, islandId);
            ps.setString(2, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        });
    }
    
    public CompletableFuture<Map<UUID, MemberRank>> getMembers(int islandId) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `uuid`, `member_rank` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, islandId);
            Map<UUID, MemberRank> map = new HashMap<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                try{
                    UUID uuid = UUID.fromString(rs.getString(1));
                    MemberRank rank = MemberRank.get(rs.getInt(2));
                    map.put(uuid, rank);
                }catch (IllegalArgumentException e){
                    continue;
                }
            }
            return map;
        });
    }

    public CompletableFuture<Void> removeIntern(int islandId, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+table+" " +
                    "WHERE `island_id`=? AND `uuid`=? AND `member_rank` <= ?");
            ps.setInt(1, islandId);
            ps.setString(2, uuid.toString());
            ps.setInt(3, MemberRank.INTERN.getPriority());
            ps.execute();
            return null;
        });
    }
    
    public CompletableFuture<Void> addIntern(int islandId, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" " +
                    "(`island_id`, `uuid`, `member_rank`) " +
                    "VALUES(?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE `member_rank`=VALUES(`member_rank`)");
            ps.setInt(1, islandId);
            ps.setString(2, uuid.toString());
            ps.setInt(3, MemberRank.INTERN.getPriority());
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<List<UUID>> getInterns(int islandId) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `uuid` FROM "+table+" " +
                    "WHERE `island_id`=? AND `member_rank`=?");
            ps.setInt(1, islandId);
            ps.setInt(2, MemberRank.INTERN.getPriority());
            ResultSet rs = ps.executeQuery();
            List<UUID> list = new ArrayList<>();
            while (rs.next()){
                try {
                    list.add(UUID.fromString(rs.getString(1)));
                }catch (IllegalArgumentException e){
                    continue;
                }
            }
            return list;
        });
    }

    public CompletableFuture<Boolean> isIntern(int islandId, UUID uuid) {
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `uuid` FROM "+table+" " +
                    "WHERE `island_id`=? AND `uuid`=? AND `member_rank`=?");
            ps.setInt(1, islandId);
            ps.setString(2, uuid.toString());
            ps.setInt(3, MemberRank.INTERN.getPriority());
            return ps.executeQuery().next();
        });
    }

}
