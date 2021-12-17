package kr.cosmoisland.cosmoislands.bukkit.test;

import kr.cosmoisland.cosmoislands.bukkit.database.BankMoneyLoader;
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
import java.util.concurrent.ExecutionException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BankLoaderTest {

    static BankMoneyLoader loader;
    static Database database;

    @BeforeClass
    public static void setUp() throws ExecutionException, InterruptedException {
        database = TestKit.getDB();
        loader = new BankMoneyLoader(TestKit.getBANK_TABLE(), database);
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
    public void test_02_moneyTest() throws ExecutionException, InterruptedException {
        loader.create(1).get();
        loader.setMoney(1, 100d).get();
        Assert.assertEquals(100d, loader.getMoney(1).get(), 0.0);
        loader.delete(1).get();
    }

    @Test
    public void test_03_CreateAndDeleteTest() throws ExecutionException, InterruptedException {
        loader.create(1).get();
        Assert.assertEquals( 0d, loader.getMoney(1).get(), 0.0);
        loader.delete(1).get();
        Assert.assertEquals( -1d, loader.getMoney(1).get(), 0.0);
    }

    public static void resetAll() throws ExecutionException, InterruptedException {
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+loader.getTable()+ " WHERE `island_id`=?");
            ps.setInt(1, 1);
            ps.execute();
            PreparedStatement ps2 = connection.prepareStatement("DROP TABLE "+loader.getTable());
            //ps2.execute();
        });
    }

}
