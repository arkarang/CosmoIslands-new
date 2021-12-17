package kr.cosmoisland.cosmoislands.bukkit.world;

import com.minepalm.manyworlds.api.bukkit.LoadPhase;
import com.minepalm.manyworlds.api.bukkit.WorldDatabase;
import com.minepalm.manyworlds.api.bukkit.WorldStorage;
import com.minepalm.manyworlds.bukkit.ManyWorldLoader;
import com.minepalm.manyworlds.bukkit.strategies.WorldStrategy;

public class IslandWorldLoader extends ManyWorldLoader {
    public IslandWorldLoader(WorldDatabase database, WorldStorage storage) {
        super(database, storage);
        WorldStrategy chunk = this.strategies.get(LoadPhase.CHUNK);
        //this.strategies.put(LoadPhase.CHUNK, new WrappedChunkStrategy(chunk));
    }
}
