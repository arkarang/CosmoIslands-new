package kr.cosmoisland.cosmoislands.bukkit.test;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.bukkit.database.IslandDataLoader;
import kr.cosmoisland.cosmoislands.bukkit.island.view.IslandDataView;
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
public class IslandDataLoaderTest {

    static IslandDataLoader loader;
    static Database database;

    @BeforeClass
    public static void setUp() throws ExecutionException, InterruptedException {
        database = TestKit.getDB();
        loader = new IslandDataLoader(TestKit.getDATA_TABLE(), TestKit.getDB(), new AbstractLocation(0, 0, 0, 0, 0));
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
    public void test_01_Init() {
        loader.init();
    }

    @Test
    public void test_02_Create() throws ExecutionException, InterruptedException {
        int id = 1;
        loader.create(id).get();
        Assert.assertSame(50, loader.getLength(id).get());
        Assert.assertSame(50, loader.getWeight(id).get());
        Assert.assertSame(-25, loader.getMinX(id).get());
        Assert.assertSame(-25, loader.getMinZ(id).get());
        Assert.assertSame(0, loader.getLevel(id).get());
        Assert.assertSame(0, loader.getPoints(id).get());
        Assert.assertSame(5, loader.getMaxInterns(id).get());
        Assert.assertSame(5, loader.getMaxPlayers(id).get());
    }

    @Test
    public void test_03_LoadAndSaveTest() throws ExecutionException, InterruptedException {
        IslandDataView view = new IslandDataView("TestIsland", false, 0, 0, -25, -25, 50, 50, 5, 5, new AbstractLocation(0, 64, 0, 0f, 0f));
        loader.save(1, view).get();
        IslandData data = loader.load(1).get();
        Assert.assertTrue(data instanceof IslandDataView);
        assertData(data, view);
    }

    @Test
    public void test_04_DeleteTest() throws ExecutionException, InterruptedException {
        loader.delete(1).get();
        IslandData data = loader.load(1).get();
        Assert.assertNull(data);
    }

    public static void resetAll(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+loader.getTable()+ " WHERE `island_id`=?");
            ps.setInt(1, 1);
            PreparedStatement ps2 = connection.prepareStatement("DROP TABLE "+loader.getTable());
            //ps2.execute();
        });
    }

    /*
    @Test
    public void testtt() throws ExecutionException, InterruptedException {
        long asdf = System.currentTimeMillis();
        loader.create(1).get();
        System.out.println((System.currentTimeMillis() - asdf)+" ms");
    }
    */

    private void assertData(IslandData data) throws ExecutionException, InterruptedException {
        Assert.assertSame(50, data.getLength().get());
        Assert.assertSame(50, data.getWeight().get());
        Assert.assertSame(-25, data.getMinX().get());
        Assert.assertSame(-25, data.getMinZ().get());
        Assert.assertSame(0, data.getLevel().get());
        Assert.assertSame(0, data.getPoints().get());
        Assert.assertSame(5, data.getMaxInterns().get());
        Assert.assertSame(5, data.getMaxPlayers().get());
    }

    private void assertData(IslandData data2, IslandData data1) throws ExecutionException, InterruptedException {
        Assert.assertSame(data1.getLength().get(), data2.getLength().get());
        Assert.assertSame(data1.getWeight().get(), data2.getWeight().get());
        Assert.assertSame(data1.getMinX().get(), data2.getMinX().get());
        Assert.assertSame(data1.getMinZ().get(), data2.getMinZ().get());
        Assert.assertSame(data1.getLevel().get(), data2.getLevel().get());
        Assert.assertSame(data1.getPoints().get(), data2.getPoints().get());
        Assert.assertSame(data1.getMaxPlayers().get(), data2.getMaxPlayers().get());
        Assert.assertSame(data1.getMaxInterns().get(), data2.getMaxInterns().get());
    }

}
