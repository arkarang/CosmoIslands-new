package kr.cosmoislands.cosmoislands.core.packet.callback;

import com.minepalm.hellobungee.api.CallbackTransformer;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.packet.IslandUpdatePacket;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class IslandUpdateCallback implements CallbackTransformer<IslandUpdatePacket, Boolean> {

    private final CosmoIslands service;

    @Override
    public String getIdentifier() {
        return IslandUpdatePacket.class.getSimpleName();
    }

    @Override
    public Boolean transform(IslandUpdatePacket packet) {
        try {
            if(packet.isLoad()){
                return service.loadIsland(packet.getIslandID(), true).get() != null;
            }else{
                return service.unloadIsland(packet.getIslandID()).get();
            }
        }catch (InterruptedException | ExecutionException e){
            return false;
        }
    }
}