package kr.cosmoislands.cosmoislands.core;

import com.minepalm.hellobungee.api.CallbackService;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.api.HelloGateway;
import com.minepalm.hellobungee.api.HelloHandler;
import kr.cosmoislands.cosmoislands.core.packet.adapters.IslandCreateAdapter;
import kr.cosmoislands.cosmoislands.core.packet.adapters.IslandDeleteAdapter;
import kr.cosmoislands.cosmoislands.core.packet.adapters.IslandUpdateAdapter;
import kr.cosmoislands.cosmoislands.core.packet.callback.IslandCreateCallback;
import kr.cosmoislands.cosmoislands.core.packet.callback.IslandUpdateCallback;
import kr.cosmoislands.cosmoislands.core.packet.executors.IslandCreatePacketExecutor;
import kr.cosmoislands.cosmoislands.core.packet.executors.IslandDeletePacketExecutor;
import kr.cosmoislands.cosmoislands.core.packet.executors.IslandLoadPacketExecutor;
import kr.cosmoislands.cosmoislands.core.packet.executors.IslandSyncPacketExecutor;

public class HelloBungeeInitializer {

    public static void init(HelloEveryone networkModule, CosmoIslands service){
        HelloHandler handler = networkModule.getHandler();
        HelloGateway gateway = networkModule.getGateway();
        CallbackService callbackService = networkModule.getCallbackService();

        gateway.registerAdapter(new IslandCreateAdapter());
        gateway.registerAdapter(new IslandDeleteAdapter());
        gateway.registerAdapter(new IslandUpdateAdapter());

        handler.registerExecutor(new IslandCreatePacketExecutor());
        handler.registerExecutor(new IslandDeletePacketExecutor());
        handler.registerExecutor(new IslandLoadPacketExecutor());
        handler.registerExecutor(new IslandSyncPacketExecutor(service.getRegistry()));

        callbackService.registerTransformer(new IslandCreateCallback(service));
        callbackService.registerTransformer(new IslandUpdateCallback(service));
        callbackService.registerTransformer(new IslandUpdateCallback(service));
    }
}
