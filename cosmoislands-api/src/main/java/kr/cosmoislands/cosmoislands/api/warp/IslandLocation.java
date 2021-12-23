package kr.cosmoislands.cosmoislands.api.warp;

import kr.cosmoislands.cosmoislands.api.AbstractLocation;
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
