package kr.cosmoislands.cosmoislands.bungee;

import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.bungee.HelloBungee;
import com.minepalm.helloplayer.core.HelloPlayers;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmochat.bungee.CosmoChatBungee;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.privatechat.CosmoChatPrivateChat;
import kr.cosmoislands.cosmoislands.api.IslandServer;
import kr.cosmoislands.cosmoislands.api.ServerRegistration;
import kr.cosmoislands.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmoislands.chat.IslandChatModule;
import kr.cosmoislands.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmoredis.CosmoDataSource;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import kr.cosmoislands.cosmoteleport.bungee.CosmoTeleportBungee;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CosmoIslandsBungee extends Plugin {

    @Getter
    static CosmoIslands cosmoIslands;

    @Override
    public void onEnable() {
        Conf conf = new Conf(this);

        HelloEveryone networkModule = HelloBungee.getInst().getMain();
        HelloPlayers playersModule = HelloPlayers.getInst();
        MySQLDatabase msLibMySQLDatabase = CosmoDataSource.mysql(conf.getRedisName());
        RedisClient redis = CosmoDataSource.redis(conf.getMySQLName());
        RedisAsyncCommands<String, String> async = redis.connect().async();
        CosmoChat cosmoChat = CosmoChatBungee.getService();
        CosmoChatPrivateChat privateChatAddon = CosmoChatBungee.getPrivateChatAddon();
        CosmoTeleport cosmoTeleport = CosmoTeleportBungee.getService();


        try {
            cosmoIslands = new CosmoIslands(networkModule, redis, msLibMySQLDatabase, this.getLogger());
            IslandChatModule chatModule = new IslandChatModule(cosmoChat, privateChatAddon, cosmoIslands.getPlayerRegistry(), msLibMySQLDatabase, redis.connect().async(), this.getLogger());
            cosmoIslands.registerModule(IslandChat.class, chatModule);
            cosmoIslands.init();
            ProxyServer.getInstance().getPluginManager().registerListener(this, new Listener(cosmoIslands, conf.getMaxIslands()));
            if(conf.updateServerRegistration()) {
                for (Map.Entry<IslandServer.Type, List<String>> entry : conf.getServerList().entrySet()) {
                    ServerRegistration registration = cosmoIslands.getCloud().getServerRegistration();
                    entry.getValue().forEach(name -> registration.registerServer(name, entry.getKey()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {

    }

}
