package kr.cosmoisland.cosmoislands.bukkit.test.mock;

import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.bukkit.island.view.PlayersMapView;
import kr.cosmoisland.cosmoislands.bukkit.test.utils.TestUUID;

import java.util.HashMap;

public class MockIslandPlayersMap extends PlayersMapView {
    public MockIslandPlayersMap() {
        super(new HashMap<>());
        players.put(new IslandPlayer(1, TestUUID.HoBread_Man), MemberRank.OWNER);
        players.put(new IslandPlayer(1, TestUUID.Arkarang), MemberRank.MEMBER);
        players.put(new IslandPlayer(1, TestUUID.ChuYong), MemberRank.MEMBER);
        players.put(new IslandPlayer(1, TestUUID.UnA_DayBear), MemberRank.MEMBER);
        players.put(new IslandPlayer(1, TestUUID.koreaBeom), MemberRank.MEMBER);
    }
}
