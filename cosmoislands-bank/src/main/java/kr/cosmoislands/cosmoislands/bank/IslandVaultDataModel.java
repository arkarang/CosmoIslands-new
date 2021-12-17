package kr.cosmoislands.cosmoislands.bank;

import kr.cosmoisland.cosmoislands.api.IslandDataModel;
import kr.cosmoisland.cosmoislands.core.Database;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandVaultDataModel implements IslandDataModel {

    final String table;
    final String islandTable;
    final Database database;

    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`money` DOUBLE DEFAULT 0, " +
                    "PRIMARY KEY(`island_id`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("IF NOT EXISTS (SELECT 1=1 FROM "+table+" WHERE `island_id`= ?) THEN "
                    +"BEGIN "
                    +"INSERT INTO "+table+" (`island_id`) VALUES(?); "
                    +"END; "
                    +"END IF; ");
            ps.setInt(1, id);
            ps.setInt(2, id);
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


    public CompletableFuture<Double> getMoney(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `money` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return rs.getDouble(1);
            else
                return -1d;
        });
    }

    public CompletableFuture<Void> addMoney(int id, double amount){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("UPDATE "+table+" SET `money`=`money`+"+amount+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> takeMoney(int id, double amount){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("UPDATE "+table+" SET `money`=`money`-"+amount+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ps.execute();
            return null;
        });
    }

    public CompletableFuture<Void> setMoney(int id, double amount){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("UPDATE "+table+" SET `money`=? WHERE `island_id`=?");
            ps.setDouble(1, amount);
            ps.setInt(2, id);
            ps.execute();
            return null;
        });
    }

}
