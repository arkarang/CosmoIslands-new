package kr.cosmoislands.cosmoislands.core.packet.callback;

import com.minepalm.hellobungee.api.CallbackTransformer;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import kr.cosmoislands.cosmoislands.core.packet.IslandUpdatePacket;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class IslandUpdateCallback implements CallbackTransformer<IslandUpdatePacket, Integer> {

    private final CosmoIslands service;

    @Override
    public String getIdentifier() {
        return IslandUpdatePacket.class.getSimpleName();
    }

    @Override
    public Integer transform(IslandUpdatePacket packet) {
        DebugLogger.log("received update packet: "+packet.getIslandID()+", "+ packet.getDestination()+", "+packet.isLoad());
        try {
            if(packet.isLoad()){
                DebugLogger.log("received update packet: 1");
                return service.loadIsland(packet.getIslandID(), true).get().getId();
            }else{
                if(service.unloadIsland(packet.getIslandID()).get()){
                    DebugLogger.log("received update packet: 2");
                    return packet.getIslandID();
                }else {
                    DebugLogger.log("received update packet: 3");
                    return null;
                }
            }
        }catch (InterruptedException | ExecutionException e){
            return null;
        }
    }
}
