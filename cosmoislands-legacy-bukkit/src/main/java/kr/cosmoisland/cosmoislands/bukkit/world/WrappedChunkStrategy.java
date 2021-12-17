package kr.cosmoisland.cosmoislands.bukkit.world;

import com.minepalm.manyworlds.api.bukkit.LoadPhase;
import com.minepalm.manyworlds.api.util.WorldInputStream;
import com.minepalm.manyworlds.api.util.WorldOutputStream;
import com.minepalm.manyworlds.bukkit.strategies.WorldBuffer;
import com.minepalm.manyworlds.bukkit.strategies.WorldStrategy;
import kr.cosmoisland.cosmoislands.core.utils.XZPair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WrappedChunkStrategy implements WorldStrategy {

    private final WorldStrategy handle;

    WrappedChunkStrategy(WorldStrategy strategy){
        handle = strategy;
    }

    @Override
    public WorldBuffer serialize(WorldOutputStream stream, WorldBuffer buffer) throws IOException {
        if(buffer.getPhase().equals(LoadPhase.CHUNK)) {
            handle.serialize(stream, buffer);
            //trim(buffer, 16, 16);
        }else{
            handle.serialize(stream, buffer);
        }
        return buffer;
    }

    @Override
    public WorldBuffer deserialize(WorldInputStream stream, WorldBuffer buffer) throws IOException {
        if(buffer.getPhase().equals(LoadPhase.CHUNK)) {
            handle.deserialize(stream, buffer);
            //trim(buffer, 16, 16);
        }else{
            handle.deserialize(stream, buffer);
        }
        return buffer;
    }

    private long convert(int x, int z){
        return (long) z * Integer.MAX_VALUE + (long)x;
    }

    private XZPair convert(long l){
        int x, z;
        x = (int)l%Integer.MAX_VALUE;
        z = (int)l/Integer.MAX_VALUE;
        return new XZPair(x, z);
    }

    private void trim(WorldBuffer buffer, int xRange, int zRange){
        List<Long> remove = new ArrayList<>();
        for(long l : buffer.getChunks().keySet()){
            XZPair pair = convert(l);
            if(xRange < pair.getX() || pair.getX() < -xRange || zRange < pair.getZ() || pair.getZ() < -zRange){
                remove.add(l);
            }
        }

        for (Long l : remove) {
            buffer.getChunks().remove(l);
        }
    }
}
