package kr.cosmoisland.cosmoislands.bukkit.test;

import kr.cosmoisland.cosmoislands.bukkit.database.WarpsLoader;
import kr.cosmoisland.cosmoislands.bukkit.test.utils.TestKit;
import kr.cosmoisland.cosmoislands.core.Database;
import org.junit.BeforeClass;

public class WarpsLoaderTest {
    static WarpsLoader loader;
    static Database database;

    @BeforeClass
    public static void setUp() {
        database = TestKit.getDB();
        loader = new WarpsLoader(TestKit.getREWARD_TABLE(), TestKit.getDB());
        /*
        loader.set();
        loader.getWarp();
        loader.load();
        loader.create();
        loader.asMap();
        loader.delete();
        loader.getWarpList();
        loader.deleteOne();
        loader.init();
        loader.save();
         */
    }


}
