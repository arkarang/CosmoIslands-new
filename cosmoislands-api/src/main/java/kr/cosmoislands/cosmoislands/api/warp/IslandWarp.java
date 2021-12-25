package kr.cosmoislands.cosmoislands.api.warp;

import kr.cosmoislands.cosmoislands.api.AbstractLocation;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import lombok.Getter;
import lombok.ToString;

@ToString
public class IslandWarp extends AbstractLocation {

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
