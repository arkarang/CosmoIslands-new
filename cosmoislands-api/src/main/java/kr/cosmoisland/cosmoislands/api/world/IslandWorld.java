package kr.cosmoisland.cosmoislands.api.world;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandWorld extends IslandComponent {

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
