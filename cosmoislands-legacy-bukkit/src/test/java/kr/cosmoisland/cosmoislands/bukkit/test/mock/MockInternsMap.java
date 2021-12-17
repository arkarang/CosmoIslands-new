package kr.cosmoisland.cosmoislands.bukkit.test.mock;

import kr.cosmoisland.cosmoislands.api.internship.IslandIntern;
import kr.cosmoisland.cosmoislands.bukkit.island.view.InternsMapView;
import kr.cosmoisland.cosmoislands.bukkit.test.utils.TestUUID;

import java.util.HashMap;

public class MockInternsMap extends InternsMapView {

    public MockInternsMap(){
        super(new HashMap<>());
        map.put(TestUUID.UnA_DayBear, new IslandIntern(1, TestUUID.UnA_DayBear));
        map.put(TestUUID.koreaBeom, new IslandIntern(1, TestUUID.koreaBeom));
        map.put(TestUUID.ChuYong, new IslandIntern(1, TestUUID.ChuYong));
        map.put(TestUUID.Arkarang, new IslandIntern(1, TestUUID.Arkarang));
    }

}
