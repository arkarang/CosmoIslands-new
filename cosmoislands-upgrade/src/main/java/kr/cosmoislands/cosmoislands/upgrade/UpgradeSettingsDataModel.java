package kr.cosmoislands.cosmoislands.upgrade;

import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoisland.cosmoislands.core.Database;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class UpgradeSettingsDataModel {

    final String table;
    final Database database;

    public void init() {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS "+table+" " +
                    "(`column_id` BIGINT UNIQUE AUTO_INCREMENT, " +
                    "`type` VARCHAR(16), " +
                    "`level` INT DEFAULT 0, " +
                    "`value` INT, " +
                    "`required_cost` INT DEFAULT 0, " +
                    "PRIMARY KEY(`type`), " +
                    "charset=utf8mb4");
            ps.execute();
        });
    }

    public CompletableFuture<Void> insert(IslandUpgradeSettings setting){
        return database.executeAsync(connection -> {
            int maxLevel = setting.getMaxLevel();
            for(int i = 1; i <= maxLevel; i++){
                PreparedStatement ps = connection.prepareStatement("INSERT INTO " + table + " (`type`, `level`, `value`, `required_cost`) " +
                        "VALUES(?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE `required_cost`=?");
                ps.setString(1, setting.getType().name());
                ps.setInt(2, i);
                ps.setInt(3, setting.getValue(i));
                ps.setInt(4, setting.getRequiredCost(i));
                ps.execute();
            }
            return null;
        });
    }

    public CompletableFuture<IslandUpgradeSettings> get(IslandUpgradeType type){
        return database.executeAsync(connection -> {
            PreparedStatement ps = connection.prepareStatement("SELECT `level`, `value`, `required_cost` FROM "+table+" WHERE `type`=?");
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            Map<Integer, Integer> values, costs;
            values = new HashMap<>();
            costs = new HashMap<>();
            while (rs.next()){
                int level = rs.getInt(1);
                int value = rs.getInt(2);
                int requiredCost = rs.getInt(3);
                values.put(level, value);
                values.put(level, requiredCost);
            }
            return new UpgradeSettingImpl(type, values, costs);
        });
    }
}
