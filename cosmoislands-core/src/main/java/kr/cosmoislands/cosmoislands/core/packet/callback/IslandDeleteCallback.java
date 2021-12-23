package kr.cosmoislands.cosmoislands.core.packet.callback;

import com.minepalm.hellobungee.api.CallbackTransformer;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.packet.IslandDeletePacket;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class IslandDeleteCallback implements CallbackTransformer<IslandDeletePacket, Boolean> {

    private final CosmoIslands service;

    @Override
    public String getIdentifier() {
        return IslandDeletePacket.class.getSimpleName();
    }

    @Override
    public Boolean transform(IslandDeletePacket packet) {
        try {
            return service.deleteIsland(packet.getId()).get();
        }catch (InterruptedException | ExecutionException e){
            return false;
        }
    }
}
