package kr.cosmoisland.cosmoislands.api.warp;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import lombok.Getter;

public class IslandLocation extends AbstractLocation {

    @Getter
    final int islandID;

    public IslandLocation(int id, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z, yaw, pitch);
        this.islandID = id;
    }

    public IslandLocation(int id, AbstractLocation loc){
        super(loc);
        this.islandID = id;
    }
}
