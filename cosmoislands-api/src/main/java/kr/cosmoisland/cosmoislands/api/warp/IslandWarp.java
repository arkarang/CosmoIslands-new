package kr.cosmoisland.cosmoislands.api.warp;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import lombok.Data;
import lombok.Getter;

public class IslandWarp extends AbstractLocation{

    @Getter
    final String name;
    @Getter
    final MemberRank rank;

    public IslandWarp(String name, MemberRank rank, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z, yaw, pitch);
        this.name = name;
        this.rank = rank;
    }

    public IslandWarp(String name, MemberRank rank, AbstractLocation location){
        super(location);
        this.name = name;
        this.rank = rank;
    }
}
