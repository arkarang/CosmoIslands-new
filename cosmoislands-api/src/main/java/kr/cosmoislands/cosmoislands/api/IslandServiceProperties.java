package kr.cosmoislands.cosmoislands.api;

import lombok.Builder;

@Builder
public class IslandServiceProperties {

    private final long autoSavePeriod;
    private final long garbageCollectingPeriod;

}
