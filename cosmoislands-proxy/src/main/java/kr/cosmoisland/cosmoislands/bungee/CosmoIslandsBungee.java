package kr.cosmoisland.cosmoislands.bungee;

import com.google.common.cache.CacheLoader;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.bungee.HelloBungee;
import com.minepalm.manyworlds.api.BukkitView;
import com.minepalm.manyworlds.bungee.ManyWorldsBungee;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.bungee.executors.IslandCreateExecutor;
import kr.cosmoisland.cosmoislands.bungee.executors.IslandDeleteExecutor;
import kr.cosmoisland.cosmoislands.bungee.executors.IslandPlayerSyncExecutor;
import kr.cosmoisland.cosmoislands.bungee.executors.IslandStatusChangeExecutor;
import kr.cosmoisland.cosmoislands.core.packet.IslandCreatePacket;
import kr.cosmoisland.cosmoislands.core.packet.IslandStatusChangePacket;
import kr.cosmoisland.cosmoislands.core.thread.IslandThreadFactory;
import kr.cosmoislands.cosmoredis.CosmoDataSource;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.LogManager;

public class CosmoIslandsBungee extends Plugin {

    @Getter
    private static CosmoIslandsBungee inst;

    ExecutorService service = Executors.newSingleThreadExecutor(IslandThreadFactory.newFactory("CosmoIslands - Proxy", (t, e) -> e.printStackTrace()).build());

    HelloEveryone networkModule;
    Database database;
    IslandTrackerLoader status;
    @Getter
    IslandCache<IslandPlayer> playersCache;
    Conf conf;
    @Getter
    String name;
    ServerSelector selector;

    @Override
    public void onEnable() {
        inst = this;
        String table = "`cosmoislands_islands`";
        conf = new Conf(this, "config.yml");

        networkModule = HelloBungee.getInst().getMain();
        name = networkModule.getName();
        MySQLDatabase mysql = CosmoDataSource.mysql(conf.getMySQLName());
        if(mysql == null){
            getLogger().severe("CosmoDataSource 에서 mysql:"+conf.getMySQLName()+"을 찾을수 없습니다. 플러그인을 종료합니다.");
            return;
        }
        database = new Database(mysql, table);
        playersCache = new IslandCache<>(new CacheLoader<UUID, IslandPlayer>() {
            @Override
            public IslandPlayer load(@NonNull UUID uuid) throws Exception {
                return database.getLoader(IslandPlayerLoader.class).get(uuid).get();
            }
        });
        status = database.getLoader(IslandTrackerLoader.class);

        selector = new ServerSelector(ManyWorldsBungee.getCore().getGlobalDatabase(), conf.getMaxIslands(), conf.getStorageServers());
        selector.initFilters();

        networkModule.getHandler().registerExecutor(new IslandCreateExecutor());
        networkModule.getHandler().registerExecutor(new IslandDeleteExecutor());
        networkModule.getHandler().registerExecutor(new IslandStatusChangeExecutor());
        networkModule.getHandler().registerExecutor(new IslandPlayerSyncExecutor(this));

        networkModule.getGateway().registerAdapter(new IslandCreateAdapter());
        networkModule.getGateway().registerAdapter(new IslandCreateExecutedAdapter());
        networkModule.getGateway().registerAdapter(new IslandDeleteAdapter());
        networkModule.getGateway().registerAdapter(new IslandDeleteExecutedAdapter());
        networkModule.getGateway().registerAdapter(new IslandLeaveAdapter());
        networkModule.getGateway().registerAdapter(new IslandStatusChangeAdapter());
        networkModule.getGateway().registerAdapter(new IslandStatusChangeExecutedAdapter());
        networkModule.getGateway().registerAdapter(new IslandTransferAdapter());
        networkModule.getGateway().registerAdapter(new IslandPlayerSyncAdapter(10));

        ProxyServer.getInstance().getPluginManager().registerListener(this, new Listener(database));
    }

    public Future<Boolean> loadIsland(int id) {
        return service.submit(()->{
            boolean successful = false;
            try {
                String worldName = "island_" + id;
                boolean isWorldLoaded = ManyWorldsBungee.getDatabase().isWorldLoaded(worldName).get();
                boolean canLoad = database.getLoader(IslandTrackerLoader.class).getStatus(id).get() == IslandTracker.OFFLINE;
                if (!isWorldLoaded && canLoad) {
                    BukkitView view = selector.getAtLeast();
                    if (view != null) {
                        status.updateIsland(id, view.getServerName(), IslandTracker.LOADING).get();
                        sendData(view.getServerName(), new IslandStatusChangePacket(view.getServerName(), id, true));
                        successful = true;
                    }
                }
            }catch (InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
            return successful;
        });
    }

    public void unloadIsland(int islandID){
        service.submit(()->{
            try {
                IslandTrackerLoader statusLoader = database.getLoader(IslandTrackerLoader.class);
                if(statusLoader.getStatus(islandID).get() == IslandTracker.ONLINE) {
                    statusLoader.updateIsland(islandID, IslandTracker.UNLOADING).get();
                    Optional<String> name = statusLoader.getLoadedServer(islandID).get();
                    name.ifPresent(s -> CosmoIslandsBungee.getInst().sendData(s, new IslandStatusChangePacket(s, islandID, false)));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public Future<Boolean> createIsland(UUID uuid){
        return service.submit(()->{
            boolean successful = false;
            try {
                IslandPlayer ip = playersCache.get(uuid);
                if(ip.getIslandId() != Island.NIL_ID){
                    Optional.ofNullable(ProxyServer.getInstance().getPlayer(uuid)).ifPresent(pr->pr.sendMessage(new TextComponent("이미 섬이 존재합니다.")));
                }else {
                    Optional.ofNullable(ProxyServer.getInstance().getPlayer(uuid)).ifPresent(pr->pr.sendMessage(new TextComponent("섬 생성중...")));
                    int id = -1;
                    try {
                        id = playersCache.refreshAndGet(uuid).getIslandId();
                        LogManager.getLogManager().getLogger("global").info("bungee: 1: "+id);
                        if (id == Island.NIL_ID) {
                            BukkitView view = selector.getAtLeast();
                            LogManager.getLogManager().getLogger("global").info("bungee: 2");
                            if (view != null) {
                                successful = true;
                                sendData(view.getServerName(), new IslandCreatePacket(view.getServerName(), uuid));
                                LogManager.getLogManager().getLogger("global").info("bungee: 3"+view.getServerName());
                            }
                            /*
                            else {
                                throw new IllegalStateException("cannot create island");
                            }

                             */
                        }
                    }catch (Exception e){
                        LogManager.getLogManager().getLogger("global").info(e.getMessage());
                        for (StackTraceElement el : e.getStackTrace()) {
                            LogManager.getLogManager().getLogger("global").info("\\n"+el);
                        }
                        database.getLoader(IslandRegistrationLoader.class).delete(id);
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return successful;
        });
    }

    public void sendData(String destination, Object message){
        Optional.ofNullable(networkModule.sender(destination)).ifPresent(client-> client.send(message));
    }

    public void syncPlayer(List<UUID> list){
        service.submit(()->{
            for (UUID uuid : list) {
                playersCache.update(uuid);
            }
        });
    }
}
