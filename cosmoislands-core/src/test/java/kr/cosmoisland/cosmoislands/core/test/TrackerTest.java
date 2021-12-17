package kr.cosmoisland.cosmoislands.core.test;

import kr.cosmoisland.cosmoislands.core.Database;
import kr.cosmoisland.cosmoislands.core.IslandRegistrationLoader;
import kr.cosmoisland.cosmoislands.core.IslandTracker;
import kr.cosmoisland.cosmoislands.core.IslandTrackerLoader;
import kr.cosmoisland.cosmoislands.core.test.utils.TestKit;
import kr.cosmoisland.cosmoislands.core.test.utils.TestUUID;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.sql.PreparedStatement;
import java.util.concurrent.ExecutionException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TrackerTest {

    static IslandTrackerLoader loader;
    static Database database;

    @BeforeClass
    public static void setup() {
        database = TestKit.getDB();
        loader = new IslandTrackerLoader("cosmoislands_loaded_island_test", TestKit.getDB());
        database.execute(connection -> {
            String table = database.getLoader(IslandRegistrationLoader.class).getTable();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO "+table+" (`island_id`, `uuid`) VALUES(?, ?) ON DUPLICATE KEY UPDATE `uuid`=?");
            ps.setInt(1, 1);
            ps.setString(2, TestUUID.HoBread_Man.toString());
            ps.setString(3, TestUUID.HoBread_Man.toString());
            ps.execute();
            return null;
        });
    }

    @Test
    public void test_01_InitTest(){
        loader.init();
    }

    @Test
    public void test_02_updateTest() throws ExecutionException, InterruptedException {
        loader.updateIsland(1, "test", IslandTracker.SOMETHING_WRONG).get();
        IslandTracker tracker = new IslandTracker(1, loader);
        Assert.assertEquals(IslandTracker.SOMETHING_WRONG, (byte) tracker.getStatus().get());
        Assert.assertEquals("test", tracker.getLocatedServer().get().get());
    }

    @Test
    public void test_03_changeStatusTest() throws ExecutionException, InterruptedException {
        loader.updateIsland(1, "test", IslandTracker.SOMETHING_WRONG).get();
        IslandTracker tracker = new IslandTracker(1, loader);
        Assert.assertEquals(IslandTracker.SOMETHING_WRONG, (byte) tracker.getStatus().get());
        Assert.assertEquals("test", tracker.getLocatedServer().get().get());
        loader.updateIsland(1, IslandTracker.ONLINE).get();
        Assert.assertEquals(IslandTracker.ONLINE, (byte) tracker.getStatus().get());
        Assert.assertTrue(tracker.isLoaded().get());
        Assert.assertEquals("test", tracker.getLocatedServer().get().get());
    }

    @Test
    public void test_04_UnregisterTest() throws ExecutionException, InterruptedException {
        loader.unregisterIsland(1).get();
        IslandTracker tracker = new IslandTracker(1, loader);
        Assert.assertFalse(tracker.isLoaded().get());
        Assert.assertFalse(tracker.getLocatedServer().get().isPresent());
        Assert.assertEquals(IslandTracker.OFFLINE, (byte)tracker.getStatus().get());
    }

    @AfterClass
    public static void resetAll(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+loader.getTable()+ " WHERE `island_id`=?");
            ps.setInt(1, 1);
            PreparedStatement ps2 = connection.prepareStatement("DROP TABLE "+loader.getTable());
            ps2.execute();
            return null;
        });
    }

}
