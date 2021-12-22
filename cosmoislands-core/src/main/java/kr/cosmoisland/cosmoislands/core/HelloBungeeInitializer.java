package kr.cosmoisland.cosmoislands.core;

import com.minepalm.hellobungee.api.CallbackService;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.api.HelloGateway;
import com.minepalm.hellobungee.api.HelloHandler;
import kr.cosmoisland.cosmoislands.core.packet.adapters.IslandCreateAdapter;
import kr.cosmoisland.cosmoislands.core.packet.adapters.IslandDeleteAdapter;
import kr.cosmoisland.cosmoislands.core.packet.adapters.IslandUpdateAdapter;
import kr.cosmoisland.cosmoislands.core.packet.callback.IslandCreateCallback;
import kr.cosmoisland.cosmoislands.core.packet.callback.IslandUpdateCallback;
import kr.cosmoisland.cosmoislands.core.packet.executors.IslandCreatePacketExecutor;
import kr.cosmoisland.cosmoislands.core.packet.executors.IslandDeletePacketExecutor;
import kr.cosmoisland.cosmoislands.core.packet.executors.IslandLoadPacketExecutor;
import kr.cosmoisland.cosmoislands.core.packet.executors.IslandSyncPacketExecutor;

public class HelloBungeeInitializer {

    public static void initBukkit(HelloEveryone networkModule, CosmoIslands service){
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

    public static void initBungee(HelloEveryone networkModule){
        HelloGateway gateway = networkModule.getGateway();

        gateway.registerAdapter(new IslandCreateAdapter());
        gateway.registerAdapter(new IslandDeleteAdapter());
        gateway.registerAdapter(new IslandUpdateAdapter());
    }
}
