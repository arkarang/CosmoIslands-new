package kr.cosmoisland.cosmoislands.protection.hellobungee;

import com.minepalm.hellobungee.api.HelloExecutor;
import kr.cosmoisland.cosmoislands.api.protection.IslandProtection;
import kr.cosmoisland.cosmoislands.protection.CachedIslandProtection;
import kr.cosmoisland.cosmoislands.protection.IslandProtectionModule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProtectionUpdatePacketExecutor implements HelloExecutor<ProtectionUpdatePacket> {

    private final IslandProtectionModule module;

    @Override
    public String getIdentifier() {
        return ProtectionUpdatePacket.class.getSimpleName();
    }

    @Override
    public void executeReceived(ProtectionUpdatePacket packet) {
        IslandProtection protection = module.get(packet.getIslandId());
        if(protection instanceof CachedIslandProtection){
            protection.update(packet.getUuid());
        }
    }
}
