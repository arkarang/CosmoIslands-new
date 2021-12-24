package kr.cosmoislands.cosmoislands.core.packet.callback;

import com.minepalm.hellobungee.api.CallbackTransformer;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import kr.cosmoislands.cosmoislands.core.packet.IslandCreatePacket;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class IslandCreateCallback implements CallbackTransformer<IslandCreatePacket, Integer> {

    private final CosmoIslands service;

    @Override
    public String getIdentifier() {
        return IslandCreatePacket.class.getSimpleName();
    }

    @Override
    public Integer transform(IslandCreatePacket packet) {
        try {
            DebugLogger.log("create packet callback: "+packet.getUuid()+", "+packet.getDestination());
            return service.createIsland(packet.getUuid()).get().getId();
        }catch (InterruptedException | ExecutionException e){
            return null;
        }
    }

}
