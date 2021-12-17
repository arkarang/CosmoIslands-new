package kr.cosmoisland.cosmoislands.bukkit.test;

import kr.cosmoisland.cosmoislands.bukkit.database.RewardSettingLoader;
import kr.cosmoisland.cosmoislands.bukkit.test.utils.TestKit;
import kr.cosmoisland.cosmoislands.core.Database;
import org.junit.BeforeClass;

//todo: 나중에 Test 구현
public class RewardSettingsLoaderTest {
    static RewardSettingLoader loader;
    static Database database;

    @BeforeClass
    public void setUp() {
        database = TestKit.getDB();
        loader = new RewardSettingLoader(TestKit.getREWARD_TABLE(), TestKit.getDB());
    }
}
