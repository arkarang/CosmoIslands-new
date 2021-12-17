package kr.cosmoisland.cosmoislands.bukkit.test;

import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.bukkit.database.PlayersMapLoader;
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
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PlayersMapLoaderTest {
    static PlayersMapLoader loader;
    static Database database;

    @BeforeClass
    public static void setUp() throws ExecutionException, InterruptedException {
        database = TestKit.getDB();
        loader = new PlayersMapLoader(TestKit.getUSER_TABLE(), TestKit.getDB());
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
    public void test_02_CreateAndDeleteTest() throws ExecutionException, InterruptedException {
        loader.create(1, TestUUID.HoBread_Man).get();
        Assert.assertNotNull(loader.load(1).get());
        loader.delete(1).get();
        Assert.assertNull(loader.load(1).get());
    }

    @Test
    public void test_04_GetOwnerTest() throws ExecutionException, InterruptedException {
        loader.create(1, TestUUID.HoBread_Man).get();
        UUID uuid = loader.getOwner(1).get().getUniqueID();
        Assert.assertEquals(uuid, TestUUID.HoBread_Man);
    }

    @Test
    public void test_05_GetPlayersAndDeleteTest() throws ExecutionException, InterruptedException {
        loader.create(1, TestUUID.HoBread_Man).get();
        loader.setPlayer(1, TestUUID.Arkarang, MemberRank.MEMBER).get();
        loader.setPlayer(1, TestUUID.ChuYong, MemberRank.MEMBER).get();
        Assert.assertEquals(1, loader.getPlayersMap(1, MemberRank.OWNER).get().size());
        Assert.assertEquals(2, loader.getPlayersMap(1, MemberRank.MEMBER).get().size());
        loader.delete(1).get();
        Assert.assertEquals(0, loader.getPlayersMap(0, MemberRank.OWNER).get().size());
        Assert.assertEquals(0, loader.getPlayersMap(0, MemberRank.MEMBER).get().size());
    }

    public static void resetAll(){
        database.execute(connection -> {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM "+loader.getTable()+ " WHERE `island_id`=?");
            ps.setInt(1, 1);
            PreparedStatement ps2 = connection.prepareStatement("DROP TABLE "+loader.getTable());
            //ps2.execute();
        });
    }

}
