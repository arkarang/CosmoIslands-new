package kr.cosmoislands.cosmoislands.core.packet.callback;

import com.minepalm.hellobungee.api.CallbackTransformer;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoislands.core.packet.IslandCreatePacket;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class IslandCreateCallback implements CallbackTransformer<IslandCreatePacket, Boolean> {

    private final CosmoIslands service;

    @Override
    public String getIdentifier() {
        return IslandCreatePacket.class.getSimpleName();
    }

    @Override
    public Boolean transform(IslandCreatePacket packet) {
        try {
            return service.createIsland(packet.getUuid()).get() != null;
        }catch (InterruptedException | ExecutionException e){
            return false;
        }
    }

}
