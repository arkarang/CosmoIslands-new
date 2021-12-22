package kr.cosmoislands.cosmoislands.bukkit;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.hellobungee.api.HelloEveryone;
import com.minepalm.helloplayer.core.HelloPlayers;
import com.minepalm.manyworlds.bukkit.ManyWorlds;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.comsoisland.cosmoislands.points.IslandPointsModule;
import kr.comsoislands.comsoislands.member.PlayersMapModule;
import kr.cosmoisland.cosmoislands.api.ExternalRepository;
import kr.cosmoisland.cosmoislands.api.IslandCloud;
import kr.cosmoisland.cosmoislands.api.IslandConfiguration;
import kr.cosmoisland.cosmoislands.api.IslandRegistry;
import kr.cosmoisland.cosmoislands.api.bank.IslandBank;
import kr.cosmoisland.cosmoislands.api.bank.IslandVault;
import kr.cosmoisland.cosmoislands.api.chat.IslandChat;
import kr.cosmoisland.cosmoislands.api.level.IslandAchievements;
import kr.cosmoisland.cosmoislands.api.level.IslandLevel;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayerRegistry;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMapModule;
import kr.cosmoisland.cosmoislands.api.points.IslandPoints;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoisland.cosmoislands.api.protection.IslandProtection;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgrade;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeSettings;
import kr.cosmoisland.cosmoislands.api.upgrade.IslandUpgradeType;
import kr.cosmoisland.cosmoislands.api.warp.IslandWarpsMap;
import kr.cosmoisland.cosmoislands.api.world.IslandWorld;
import kr.cosmoisland.cosmoislands.chat.IslandChatModule;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoisland.cosmoislands.core.Database;
import kr.cosmoisland.cosmoislands.level.IslandAchievementsModule;
import kr.cosmoisland.cosmoislands.level.IslandLevelModule;
import kr.cosmoisland.cosmoislands.protection.IslandPermissionsMapModule;
import kr.cosmoisland.cosmoislands.protection.IslandProtectionModule;
import kr.cosmoisland.cosmoislands.settings.IslandSettingsModule;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.privatechat.CosmoChatPrivateChat;
import kr.cosmoislands.cosmoislands.bank.IslandInventoryModule;
import kr.cosmoislands.cosmoislands.bank.IslandVaultModule;
import kr.cosmoislands.cosmoislands.upgrade.IslandUpgradeModule;
import kr.cosmoislands.cosmoislands.warp.IslandWarpModule;
import kr.cosmoislands.cosmoteleport.CosmoTeleport;
import kr.cosomoisland.cosmoislands.world.IslandWorldModule;
import kr.msleague.mslibrary.database.impl.internal.MySQLDatabase;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class CosmoIslandsLauncher {

    private final CosmoIslands service;
    private final RedisClient client;
    private final MySQLDatabase msLibMySQLDatabase;
    private final Logger logger;

    public <T> void registerExternalDependency(Class<T> clazz, T instance){
        service.getExternalRepository().registerService(clazz, instance);
    }
    public void initializeModules(IslandConfiguration configuration){
        RedisAsyncCommands<String, String> async = client.connect().async();
        ExternalRepository repo = service.getExternalRepository();
        IslandRegistry islandRegistry = service.getRegistry();
        Database database = service.getDatabase();
        IslandPlayerRegistry playerRegistry = service.getPlayerRegistry();
        IslandCloud cloud = service.getCloud();

        HelloEveryone networkModule = repo.getRegisteredService(HelloEveryone.class);
        HelloPlayers playersModule = repo.getRegisteredService(HelloPlayers.class);
        CosmoChat cosmoChat = repo.getRegisteredService(CosmoChat.class);
        CosmoChatPrivateChat privateChat = repo.getRegisteredService(CosmoChatPrivateChat.class);
        CosmoTeleport cosmoTeleport = repo.getRegisteredService(CosmoTeleport.class);
        ManyWorlds manyWorlds = repo.getRegisteredService(ManyWorlds.class);
        BukkitExecutor executor = repo.getRegisteredService(BukkitExecutor.class);

        IslandChatModule chatModule = new IslandChatModule(cosmoChat, privateChat, playerRegistry, msLibMySQLDatabase, async, logger);
        IslandLevelModule levelModule = new IslandLevelModule(database, logger);
        IslandAchievementsModule achievementsModule = new IslandAchievementsModule(database, logger);
        IslandSettingsModule settingsModule = new IslandSettingsModule(database, cloud, async, configuration.getDefaultSettings(), logger);
        IslandPlayersMapModule playersMapModule = new PlayersMapModule(islandRegistry, playerRegistry, settingsModule, database, async, logger);

        IslandPointsModule pointsModule = new IslandPointsModule(database, logger);
        IslandPermissionsMapModule permissionsModule = new IslandPermissionsMapModule(configuration.getDefaultPermissions(), logger);
        IslandProtectionModule protectionModule = new IslandProtectionModule(permissionsModule, playersMapModule, settingsModule, playerRegistry, cloud, logger);
        IslandUpgradeModule upgradeModule = new IslandUpgradeModule(islandRegistry, database, logger);
        IslandWorldModule worldModule = new IslandWorldModule(manyWorlds, database, configuration.getManyWorldsProperties(), configuration.getDefaultWorldBorder(), logger);

        IslandInventoryModule bankModule = new IslandInventoryModule(database, executor, logger);
        IslandVaultModule vaultModule = new IslandVaultModule(database, logger);
        IslandWarpModule warpModule = new IslandWarpModule(database, islandRegistry, playerRegistry, cosmoTeleport, settingsModule, logger);


        if(configuration.getUpdateUpgradeSettings()){
            for (IslandUpgradeSettings setting : configuration.getDefaultUpgradeSettings().values()) {
                upgradeModule.getSettingsRegistry().setSetting(setting);
            }
        }

        service.registerModule(IslandWorld.class, worldModule);
        service.registerModule(IslandSettingsMap.class, settingsModule);
        service.registerModule(IslandWarpsMap.class, warpModule);
        service.registerModule(IslandPlayersMap.class, playersMapModule);
        service.registerModule(IslandBank.class, bankModule);

        service.registerModule(IslandVault.class, vaultModule);
        service.registerModule(IslandChat.class, chatModule);
        service.registerModule(IslandLevel.class, levelModule);
        service.registerModule(IslandAchievements.class, achievementsModule);
        service.registerModule(IslandPoints.class, pointsModule);

        service.registerModule(IslandPermissionsMap.class, permissionsModule);
        service.registerModule(IslandProtection.class, protectionModule);
        service.registerModule(IslandUpgrade.class, upgradeModule);

        service.getRegistry().registerComponentId(IslandWorld.class, IslandWorld.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandSettingsMap.class, IslandSettingsMap.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandPlayersMap.class, IslandPlayersMap.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandBank.class, IslandBank.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandVault.class, IslandVault.COMPONENT_ID);

        service.getRegistry().registerComponentId(IslandChat.class, IslandChat.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandLevel.class, IslandLevel.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandAchievements.class, IslandAchievements.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandPoints.class, IslandPoints.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandPermissionsMap.class, IslandPermissionsMap.COMPONENT_ID);

        service.getRegistry().registerComponentId(IslandProtection.class, IslandProtection.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandUpgrade.class, IslandUpgrade.COMPONENT_ID);
        service.getRegistry().registerComponentId(IslandWarpsMap.class, IslandWarpsMap.COMPONENT_ID);
    }

    public void launch(){
        service.init();
    }
}
