package kr.cosmoisland.cosmoislands.settings;

import com.google.common.collect.ImmutableMap;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.core.AbstractDataModel;
import kr.cosmoisland.cosmoislands.core.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandSettingsDataModel extends AbstractDataModel {

    private final String islandTable;
    private final ImmutableMap<IslandSetting, String> defaultMap;

    public IslandSettingsDataModel(String table, String islandTable, Database database, Map<IslandSetting, String> map) {
        super(table, database);
        this.defaultMap = ImmutableMap.copyOf(map);
        this.islandTable = islandTable;
    }

    @Override
    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`island_id` BIGINT, " +
                    "`key` VARCHAR(32), " +
                    "`value` VARCHAR(128), "+
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

    CompletableFuture<String> getValue(int islandId, IslandSetting setting){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `value` FROM "+table+" WHERE `island_id`=? AND `key`=?");
            ps.setInt(1, islandId);
            ps.setString(2, setting.name());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1);
            }else
                return defaultMap.get(setting);
        });
    }

    CompletableFuture<Void> setValue(int islandId, IslandSetting setting, String value){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `key`, `value`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `key`=?, `value`=?");
            ps.setInt(1, islandId);
            ps.setString(2, setting.name());
            ps.setString(3, value);
            ps.setString(4, setting.name());
            ps.setString(5, value);
            ps.execute();
            return null;
        });
    }

    CompletableFuture<Map<IslandSetting, String>> getSettings(int islandId){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `key`, `value` FROM "+table+" WHERE `island_id`=?");
            ps.setInt(1, islandId);
            ResultSet rs = ps.executeQuery();
            Map<IslandSetting, String> map = new HashMap<>(defaultMap);
            while (rs.next()){
                try{
                    IslandSetting setting = IslandSetting.valueOf(rs.getString(1));
                    map.put(setting, rs.getString(2));
                }catch (IllegalArgumentException e){
                    continue;
                }
            }
            return map;
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
