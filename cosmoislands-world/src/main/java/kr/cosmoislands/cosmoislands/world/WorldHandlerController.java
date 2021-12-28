package kr.cosmoislands.cosmoislands.world;

import com.minepalm.hellobungee.api.HelloEveryone;
import kr.cosmoislands.cosmoislands.api.IslandCloud;
import kr.cosmoislands.cosmoislands.api.world.IslandWorldHandler;
import kr.cosmoislands.cosmoislands.api.world.WorldOperationRegistry;
import kr.cosmoislands.cosmoislands.world.hellobungee.WorldHandlerResolver;
import kr.cosmoislands.cosmoislands.world.hellobungee.WorldOperationPacket;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class WorldHandlerController {

    private final IslandCloud cloud;
    private final HelloEveryone network;
    private final WorldHandlerResolver resolver;
    private final WorldOperationRegistry operationRegistry;

    public CompletableFuture<Boolean> sendOperation(int islandId, IslandWorldHandler handler, String operationKey){
        /**
         * 1. 섬 로드 되어있는지 찾는다.
         * 2. 로드가 되어있으면 network 에서 helloSender를 찾는다.
         * 3. 보낸다.
         * 4. 콜백을 받는다.
         */
        throw new UnsupportedOperationException("not implemented");
    }

    public CompletableFuture<Boolean> executeOperation(WorldOperationPacket packet){
        /**
         * 1. island 를 찾는다.
         * 2. local island 가 존재하는지 확인한다.
         * 3. WorldHandler 타입을 체크한다.
         * 3. 없으면 false
         * 4. 있으면 WorldOperation#execute
         * 5. 결과에 오류 뱉거나 문제 생기면 false
         */
        throw new UnsupportedOperationException("not implemented");
    }

}
