package kr.cosmoisland.cosmoislands.bukkit.test;

import kr.cosmoisland.cosmoislands.api.internship.IslandIntern;
import kr.cosmoisland.cosmoislands.api.internship.IslandInternsMap;
import kr.cosmoisland.cosmoislands.bukkit.database.InternsMapLoader;
import kr.cosmoisland.cosmoislands.bukkit.database.IslandInvitationLoader;
import kr.cosmoisland.cosmoislands.bukkit.test.mock.MockInternsMap;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InternsMapLoaderTest {

    static InternsMapLoader loader;
    static Database database;

    @BeforeClass
    public static void setUp() throws ExecutionException, InterruptedException {
        database = TestKit.getDB();
        loader = new InternsMapLoader(TestKit.getINTERNS_TABLE(), TestKit.getDB());
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
    public void test_02_getInternsTest() throws ExecutionException, InterruptedException {
        MockInternsMap map = new MockInternsMap();
        map.getInterns().get().forEach(ii-> {
            try {
                loader.addIntern(1, ii.getUniqueID()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        List<IslandIntern> list = loader.getInterns(1).get();
        Assert.assertEquals(4, list.size());
        Assert.assertEquals(1, list.get(0).getIslandId());
    }

    @Test
    public void test_03_saveAndLoadTest() throws ExecutionException, InterruptedException {
        MockInternsMap map = new MockInternsMap();
        map.getInterns().get().forEach(ii-> {
            try {
                loader.addIntern(1, ii.getUniqueID()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        IslandInternsMap internsMap;
        internsMap = loader.load(1).get();
        Assert.assertNotNull(internsMap);
        for (IslandIntern ii : internsMap.getInterns().get()) {
            Assert.assertTrue(map.isIntern(ii.getUniqueID()).get());
        }
    }

    @Test
    public void test_05_addAndRemoveTest() throws ExecutionException, InterruptedException {
        UUID random = UUID.randomUUID();
        loader.addIntern(1, random).get();
        Assert.assertTrue(loader.isIntern(1, random).get());
        loader.removeIntern(1, random).get();
        Assert.assertFalse(loader.isIntern(1, random).get());
    }

    @Test
    public void asdf(){
        IslandInvitationLoader loader = new IslandInvitationLoader("cosmoislands_invitations_test", database);
        loader.init();
    }

    public static void resetAll(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+loader.getTable()+ " WHERE `island_id`=?");
            ps.setInt(1, 1);
            ps.execute();
            PreparedStatement ps2 = connection.prepareStatement("DROP TABLE "+loader.getTable());
            ps2.execute();
        });
    }

}
