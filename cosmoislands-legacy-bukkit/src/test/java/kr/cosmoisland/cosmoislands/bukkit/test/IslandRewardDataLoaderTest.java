package kr.cosmoisland.cosmoislands.bukkit.test;

import kr.cosmoisland.cosmoislands.api.level.IslandAchievements;
import kr.cosmoisland.cosmoislands.bukkit.database.IslandRewardDataLoader;
import kr.cosmoisland.cosmoislands.bukkit.test.utils.TestKit;
import kr.cosmoisland.cosmoislands.bukkit.test.utils.TestUUID;
import kr.cosmoisland.cosmoislands.core.Database;
import kr.cosmoisland.cosmoislands.core.IslandRegistrationLoader;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IslandRewardDataLoaderTest {

    static IslandRewardDataLoader loader;
    static Database database;

    @BeforeClass
    public static void setUp() throws ExecutionException, InterruptedException {
        database = TestKit.getDB();
        loader = new IslandRewardDataLoader(TestKit.getREWARD_TABLE(), TestKit.getDB());
        database.execute(connection -> {
            String table = database.getLoader(IslandRegistrationLoader.class).getTable();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `uuid`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `uuid`=?");
            ps.setInt(1, 1);
            ps.setString(2, TestUUID.HoBread_Man.toString());
            ps.setString(3, TestUUID.HoBread_Man.toString());
            ps.execute();
        });
    }

    @Test
    public void test_01_initTest(){
        loader.init();
    }

    @Test
    public void test_02_createTest() throws ExecutionException, InterruptedException {
        loader.create(1);
        Assert.assertNotNull(loader.load(1).get());
    }

    @Test
    public void test_03_mapTest() throws ExecutionException, InterruptedException {
        loader.update(1, 1, true).get();
        loader.update(1, 2, false).get();
        loader.update(1, 3, true).get();
        loader.update(1, 4, false).get();
        Map<Integer, Boolean> map = loader.asMap(1).get();
        Assert.assertSame(map.size(), 4);
        Assert.assertTrue(map.get(1));
        Assert.assertFalse(map.get(2));
        Assert.assertTrue(map.get(3));
        Assert.assertFalse(map.get(4));

    }

    @Test
    public void test_04_updateAndRemoveTest() throws ExecutionException, InterruptedException {
        loader.update(1, 1, true).get();
        IslandAchievements data = loader.load(1).get();
        Assert.assertTrue(data.isAchieved(1).get());
        loader.remove(1, 1).get();
        data = loader.load(1).get();
        Assert.assertFalse(data.isAchieved(1).get());
    }

    public static void resetAll(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+loader.getTable()+ " WHERE `island_id`=?");
            ps.setInt(1, 1);
            PreparedStatement ps2 = connection.prepareStatement("DROP TABLE "+loader.getTable());
            ps2.execute();
        });
    }
}
