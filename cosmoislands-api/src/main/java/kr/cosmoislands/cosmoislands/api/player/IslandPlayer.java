package kr.cosmoislands.cosmoislands.api.player;


import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.member.IslandInternship;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IslandPlayer {

    UUID getUniqueId();

    CompletableFuture<Island> getIsland();

    CompletableFuture<Integer> getIslandId();

    IslandInternship getInternship();

    /**
     * @deprecated 나중에 플레이어 기반 하위 컴포넌트가 IslandInternship 말고 더 생기면 구현해주세요.
     */
    @Deprecated
    default Object getComponent(Class<?> clazz){
        throw new UnsupportedOperationException("not implemented");
    }

    void invalidate();

}
