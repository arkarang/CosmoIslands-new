package kr.cosmoislands.cosmoislands.world.hellobungee;

import com.minepalm.hellobungee.api.CallbackTransformer;
import kr.cosmoislands.cosmoislands.world.WorldHandlerController;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorldOperationExecutor implements CallbackTransformer<WorldOperationPacket, Boolean> {

    private final WorldHandlerController controller;

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public Boolean transform(WorldOperationPacket packet) {
        return null;
    }
}
