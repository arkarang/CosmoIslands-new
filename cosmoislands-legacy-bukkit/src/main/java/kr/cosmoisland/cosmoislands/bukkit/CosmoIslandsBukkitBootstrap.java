package kr.cosmoisland.cosmoislands.bukkit;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableMap;
import com.minepalm.arkarangutils.bukkit.ArkarangGUIListener;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.hellobungee.bukkit.HelloBukkit;
import com.minepalm.helloplayer.core.HelloPlayers;
import com.minepalm.manyworlds.api.bukkit.WorldDatabase;
import com.minepalm.manyworlds.api.bukkit.WorldLoader;
import com.minepalm.manyworlds.api.bukkit.WorldStorage;
import com.minepalm.manyworlds.bukkit.ManyWorlds;
import com.minepalm.manyworlds.bukkit.strategies.WorldBuffer;
import com.minepalm.manyworlds.core.WorldToken;
import kr.cosmoisland.cosmoislands.api.warp.IslandUserWarp;
import kr.cosmoisland.cosmoislands.bukkit.config.YamlIslandConfig;
import kr.cosmoisland.cosmoislands.bukkit.database.*;
import kr.cosmoisland.cosmoislands.bukkit.hellobungee.IslandCreateExecutor;
import kr.cosmoisland.cosmoislands.bukkit.hellobungee.IslandDeleteExecutor;
import kr.cosmoisland.cosmoislands.bukkit.hellobungee.IslandStatusChangeExecutor;
import kr.cosmoisland.cosmoislands.bukkit.island.CosmoIslandWarp;
import kr.cosmoisland.cosmoislands.bukkit.listeners.HeadDatabaseListener;
import kr.cosmoisland.cosmoislands.bukkit.listeners.IslandGenericListener;
import kr.cosmoisland.cosmoislands.bukkit.listeners.IslandLoadingListener;
import kr.cosmoisland.cosmoislands.bukkit.world.IslandWorldLoader;
import kr.cosmoisland.cosmoislands.core.*;
import kr.cosmoisland.cosmoislands.core.packet.adapters.*;
import kr.cosmoisland.cosmoislands.core.thread.IslandThreadFactory;
import kr.cosmoislands.cosmochat.bukkit.CosmoChatBukkit;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.core.helper.CosmoChatHelper;
import kr.cosmoislands.cosmoredis.CosmoDataSource;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.Getter;
import lombok.SneakyThrows;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class CosmoIslandsBukkitBootstrap extends JavaPlugin {

    private static final String ISLAND_TABLE, USER_TABLE, SETTINGS_TABLE, PERMISSION_TABLE, BANK_TABLE, DATA_TABLE, WARPS_TABLE, INTERNS_TABLE, REWARD_TABLE, REWARD_SETTINGS_TABLE, INVITATION_TABLE;

    static{
        ISLAND_TABLE = "`cosmoislands_islands`";
        USER_TABLE = "`cosmoislands_users`";
        SETTINGS_TABLE = "`cosmoislands_settings`";
        PERMISSION_TABLE = "`cosmoislands_permissions`";
        DATA_TABLE = "`cosmoislands_island_data`";
        BANK_TABLE = "`cosmoislands_bank`";
        WARPS_TABLE = "`cosmoislands_warps`";
        INTERNS_TABLE = "`cosmoislands_interns`";
        REWARD_SETTINGS_TABLE = "`cosmoislands_reward_settings`";
        REWARD_TABLE = "`cosmoislands_reward_data`";
        INVITATION_TABLE = "`cosmoislands_invitation`";
    }

    private static final ExecutorService service = Executors.newSingleThreadExecutor(IslandThreadFactory.newFactory("CosmoIslands - Player Sync", (t, e) -> e.printStackTrace()).build());

    @Getter
    private static CosmoIslandsBukkitBootstrap inst;
    private Database database;
    private CosmoIslands cosmoIslands;
    private Conf conf;
    private YamlIslandConfig yamlIslandConfig;
    private String serverName;
    private Economy economy;
    private ImmutableMap<UUID, Boolean> admins;
    private CosmoChat chat;
    private CosmoChatHelper helper;
    private HelloEveryone networkModule;
    private HelloPlayers players;

    @SneakyThrows
    @Override
    public void onEnable() {
        RegisteredServiceProvider<Economy> pr = Bukkit.getServicesManager().getRegistration(Economy.class);
        if(pr != null)
            economy = pr.getProvider();
        else {
            getLogger().severe("이코노미 플러그인을 찾을수 없습니다. 플러그인을 종료합니다.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        inst = this;
        conf = new Conf(this, "config.yml");
        chat = CosmoChatBukkit.getService();
        players = HelloPlayers.getInst();
        helper = new CosmoChatHelper(chat);
        yamlIslandConfig = new YamlIslandConfig(this);
        serverName = ManyWorlds.getInst().getServerName();

        MySQLDatabase mysql = CosmoDataSource.mysql(conf.getMySQLName());
        if(mysql == null){
            getLogger().severe("CosmoDataSource 에서 mysql:"+conf.getMySQLName()+"을 찾을수 없습니다. 플러그인을 종료합니다.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        database = new Database(mysql, ISLAND_TABLE);
        database.register(IslandPlayerLoader.class, new IslandPlayerLoader(USER_TABLE, database));
        database.register(IslandDataLoader.class, new IslandDataLoader(DATA_TABLE, database, yamlIslandConfig.getDefaultSpawnLocation()));
        database.register(BankMoneyLoader.class, new BankMoneyLoader(BANK_TABLE, database));
        database.register(PermissionsMapLoader.class, new PermissionsMapLoader(PERMISSION_TABLE, database, yamlIslandConfig.getDefaultPermissions()));
        database.register(SettingsLoader.class, new SettingsLoader(SETTINGS_TABLE, database, yamlIslandConfig.getDefaultSettings()));
        database.register(WarpsLoader.class, new WarpsLoader(WARPS_TABLE, database));
        database.register(RewardSettingLoader.class, new RewardSettingLoader(REWARD_SETTINGS_TABLE, database));
        database.register(IslandRewardDataLoader.class, new IslandRewardDataLoader(REWARD_TABLE, database));
        database.register(InternsMapLoader.class, new InternsMapLoader(INTERNS_TABLE, database));
        database.register(IslandInvitationLoader.class, new IslandInvitationLoader(INVITATION_TABLE, database));
        database.register(VoteLogLoader.class, new VoteLogLoader("`cosmoislands_votes`", database));
        database.register(IslandChatLoader.class, new IslandChatLoader("`cosmoislands_islandchat`", database, CosmoChatBukkit.getService(), CosmoChatBukkit.getPrivateChatAddon()));

        this.cosmoIslands = new CosmoIslands(database, Executors.newSingleThreadExecutor());

        WorldBuffer doesNothing = new WorldBuffer();
        doesNothing.release();

        WorldDatabase worldDatabase = ManyWorlds.getCore().newMySQL(WorldToken.get("ISLAND"), ManyWorlds.getInst().getConf().getUserTableName());
        WorldStorage storage = ManyWorlds.getWorldStorage();
        WorldLoader loader = new IslandWorldLoader(worldDatabase, storage);
        if(ManyWorlds.getCore().registerWorldLoader(WorldToken.get("ISLAND"), loader))
            getLogger().info("successfully registered island world loader");
        else
            Bukkit.getPluginManager().disablePlugin(this);

        database.getLoader(IslandTrackerLoader.class).unregisterAll(serverName).get();

        networkModule = HelloBukkit.getInst().getMain();
        networkModule.getHandler().registerExecutor(new IslandCreateExecutor());
        networkModule.getHandler().registerExecutor(new IslandDeleteExecutor());
        networkModule.getHandler().registerExecutor(new IslandStatusChangeExecutor());
        networkModule.getGateway().registerAdapter(new IslandCreateAdapter());
        networkModule.getGateway().registerAdapter(new IslandCreateExecutedAdapter());
        networkModule.getGateway().registerAdapter(new IslandDeleteAdapter());
        networkModule.getGateway().registerAdapter(new IslandDeleteExecutedAdapter());
        networkModule.getGateway().registerAdapter(new IslandLeaveAdapter());
        networkModule.getGateway().registerAdapter(new IslandStatusChangeAdapter());
        networkModule.getGateway().registerAdapter(new IslandStatusChangeExecutedAdapter());
        networkModule.getGateway().registerAdapter(new IslandTransferAdapter());
        networkModule.getGateway().registerAdapter(new IslandPlayerSyncAdapter(10));

        loadingListener = new IslandLoadingListener();
        Bukkit.getPluginManager().registerEvents(loadingListener, this);
        Bukkit.getPluginManager().registerEvents(new IslandGenericListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new HeadDatabaseListener(), this);

        PaperCommandManager manager = new PaperCommandManager(this);
        //todo: add InvitationService;
        manager.registerCommand(new Commands(players, null));
        manager.registerCommand(new AdminCommands());

        HashMap<UUID, Boolean> map = new HashMap<>();
        List<UUID> adminList = database.getLoader(AdminLoader.class).getAdmins(AdminLoader.ADMIN).get();
        adminList.forEach(uuid->map.put(uuid, true));
        admins = ImmutableMap.copyOf(map);
        ArkarangGUIListener.init();
    }

    @SneakyThrows
    @Override
    public void onDisable() {
        cosmoIslands.shutdown();
    }

    public static void sendAll(Object msg){
        getInst().networkModule.all().forEach(sender->sender.send(msg));
    }

    public void sendMessage(UUID uuid, String text){
        helper.system(uuid).send(text);
    }

    public IslandUserWarp getWarp(String name){
        return new CosmoIslandWarp(name);
    }

}
