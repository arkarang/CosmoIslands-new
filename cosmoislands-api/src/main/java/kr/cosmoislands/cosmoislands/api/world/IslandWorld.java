package kr.cosmoislands.cosmoislands.api.world;

import kr.cosmoislands.cosmoislands.api.AbstractLocation;
import kr.cosmoislands.cosmoislands.api.IslandComponent;

import java.util.concurrent.CompletableFuture;

public interface IslandWorld extends IslandComponent {

    byte COMPONENT_ID = 13;

    IslandWorldHandler getWorldHandler();

    int getMaxX();

    int getMaxZ();

    int getMinX();

    int getMinZ();

    CompletableFuture<Void> setBorder(AbstractLocation min, AbstractLocation max);

    int getWeight();

    int getLength();
}
