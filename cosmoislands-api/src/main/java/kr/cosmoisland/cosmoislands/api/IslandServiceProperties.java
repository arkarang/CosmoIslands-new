package kr.cosmoisland.cosmoislands.api;

import lombok.Builder;

@Builder
public class IslandServiceProperties {

    private final long autoSavePeriod;
    private final long garbageCollectingPeriod;

}
