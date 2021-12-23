package kr.cosmoislands.cosmoislands.api.world;

import kr.cosmoislands.cosmoislands.api.AbstractLocation;
import kr.cosmoislands.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandWorld extends IslandComponent {

    byte COMPONENT_ID = 13;

    int getMaxX();

    //world or protection
    int getMaxZ();
    //world or protection
    int getMinX();

    //world or protection
    int getMinZ();

    //world or protection
    CompletableFuture<Void> setBorder(AbstractLocation min, AbstractLocation max);

    //world or protection
    int getWeight();

    //world or protection
    int getLength();
}
