package kr.cosmoislands.cosmoislands.bank;

import com.minepalm.arkarangutils.bukkit1_16.v1_16InventorySerializer;
import kr.cosmoislands.cosmoislands.api.IslandDataModel;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class IslandInventoryDataModel implements IslandDataModel {

    final String table;
    final String islandTable;
    final Database database;

    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`level` INT DEFAULT 1, " +
                    "`contents` TEXT, " +
                    "PRIMARY KEY(`island_id`), " +
                    "FOREIGN KEY (`island_id`) REFERENCES "+islandTable+"(`island_id`) ON DELETE CASCADE) " +
                    "charset=utf8mb4");
            ps.execute();
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

    @Override
    public CompletableFuture<Void> create(int id, UUID uuid){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `level`, `contents`) VALUES(?, ?, ?); ");
            ps.setInt(1, id);
            ps.setInt(2, 1);
            ps.setString(3, serialize(new ArrayList<>()));
            return null;
        });
    }

    CompletableFuture<Integer> getLevel(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `level` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }else
                return 0;
        });
    }

    CompletableFuture<Void> setLevel(int id, int level){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `level`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `level`=VALUES(`level`)");
            ps.setInt(1, id);
            ps.setInt(2, level);
            ps.execute();
            return null;
        });
    }

    CompletableFuture<Void> saveInventory(int id, List<ItemStack> list){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `contents`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `contents`=VALUES(`contents`)");
            ps.setInt(1, id);
            ps.setString(2, serialize(list));
            ps.execute();
            return null;
        });
    }

    CompletableFuture<List<ItemStack>> getInventory(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `contents` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return deserialize(rs.getString(1));
            }else
                return new ArrayList<>();
        });
    }

    CompletableFuture<IslandInventoryView> getView(int id){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `level`, `contents` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new IslandInventoryView(rs.getInt(1), deserialize(rs.getString(1)));
            }else
                return new IslandInventoryView(0, new ArrayList<>());
        });
    }

    public static List<ItemStack> deserialize(String base64){
        try {
            return Arrays.asList(v1_16InventorySerializer.itemStackArrayFromBase64(base64));
        }catch (IOException ignored){

        }
        return new ArrayList<>();
    }

    public static String serialize(List<ItemStack> list){
        return v1_16InventorySerializer.itemStackArrayToBase64(list.toArray(new ItemStack[0]));
    }

}
