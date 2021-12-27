package kr.cosmoislands.cosmoislands.bukkit.level;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.level.IslandAchievements;
import kr.cosmoislands.cosmoislands.api.level.IslandLevel;
import kr.cosmoislands.cosmoislands.api.level.IslandRewardData;
import kr.cosmoislands.cosmoislands.api.level.IslandRewardsRegistry;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import kr.cosmoislands.cosmoislands.bukkit.utils.AbstractRankingGUI;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import kr.cosmoislands.cosmoislands.level.IslandAchievementsModule;
import kr.cosmoislands.cosmoislands.level.IslandLevelModule;
import kr.cosmoislands.cosmoislands.level.bukkit.MinecraftItemRewardData;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LevelCommands {

    public static void init(IslandService service,
                            PaperCommandManager manager,
                            HelloPlayers players,
                            BukkitExecutor executor,
                            Pattern pattern,
                            String lore){
        IslandAchievementsModule achievementsModule = (IslandAchievementsModule)service.getModule(IslandAchievements.class);
        IslandLevelModule levelModule = (IslandLevelModule) service.getModule(IslandLevel.class);
        IslandRewardsRegistry registry = achievementsModule.getRewardDataRegistry();
        AbstractRankingGUI.Factory factory = new AbstractRankingGUI.Factory(players, new AbstractRankingGUI.RankingComponents() {
            @Override
            public String getTitle() {
                return "섬 랭킹";
            }

            @Override
            public String formatData(int value) {
                return "§a§l섬 §b§l레벨 §f§l: "+value+" §f§l레벨";
            }
        }, service.getRegistry(), island -> {
            IslandLevel level = island.getComponent(IslandLevel.class);
            return level.getLevel();
        });
        manager.registerCommand(new User(factory, executor, levelModule, registry, pattern));
        manager.registerCommand(new Admin(registry, pattern, lore));
    }

    @RequiredArgsConstructor
    @CommandAlias("섬")
    protected static class User extends BaseCommand {

        private final AbstractRankingGUI.Factory factory;
        private final BukkitExecutor executor;
        private final IslandLevelModule module;
        private final IslandRewardsRegistry registry;
        private final Pattern pattern;

        @Subcommand("레벨")
        public void info(Player player){
            PlayerPreconditions.of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island -> {
                        if(island == null) {
                            player.sendMessage("당신은 섬이 없습니다.");
                            return;
                        }
                        IslandLevel level = island.getComponent(IslandLevel.class);
                        level.getLevel().thenAccept(value -> {
                            player.sendMessage("섬 레벨: "+value);
                        });
                    });
        }

        @Subcommand("레벨업")
        public void levelUp(Player player){
            PlayerPreconditions.of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island -> {
                        if(island == null){
                            player.sendMessage("당신은 섬이 없습니다.");
                            return;
                        }else{
                            IslandLevel level = island.getComponent(IslandLevel.class);
                            executor.sync(()->{
                                LevelUpGUI gui = new LevelUpGUI(level, pattern);
                                gui.openGUI(player);
                            });
                        }
                    });
        }

        @Subcommand("레벨 보상")
        public void reward(Player player){
            PlayerPreconditions.of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island -> {
                        if(island == null) {
                            player.sendMessage("당신은 섬이 없습니다.");
                            return;
                        }

                        IslandLevel islandLevel = island.getComponent(IslandLevel.class);
                        val levelFuture = islandLevel.getLevel();
                        val listFuture = registry.getAll();
                        IslandAchievements achievements = island.getComponent(IslandAchievements.class);
                        val mapFuture = achievements.asMap();

                        val executionFuture = mapFuture.thenCombine(listFuture, (map, list)->{
                            Map<Integer, IslandRewardData> rewardDataView = new HashMap<>();
                            list.forEach(data-> {
                                rewardDataView.put(data.getId(), data);
                            });

                            for(int i = 0 ; i < 7; i++){
                                if(!rewardDataView.containsKey(i)){
                                    player.sendMessage("아직 레벨 보상이 설정되어 있지 않았어요.");
                                    return null;
                                }
                            }

                            val guiFuture = levelFuture.thenAccept(level ->{
                                AchievementGUI gui = new AchievementGUI(rewardDataView, map, level, achievements, islandLevel, executor);
                                executor.sync(()-> gui.openGUI(player));
                            });
                            DebugLogger.handle("achievementGUI construct", guiFuture);
                            return null;
                        });
                        DebugLogger.handle("achievementGUI", executionFuture);
                    });
        }

        @Subcommand("레벨 랭킹")
        public void ranking(Player player){
            player.sendMessage("랭킹 로드중...");
            module.getRanking().getTopOf(3).thenAccept(list ->{
                factory.build(list).thenAccept(gui-> executor.sync(()->gui.openGUI(player)));
            });
        }
    }

    @CommandAlias("섬관리")
    @RequiredArgsConstructor
    protected static class Admin extends BaseCommand{

        private final IslandRewardsRegistry registry;
        private final Pattern pattern;
        private final String lore;

        @Subcommand("보상설정")
        public void modifyAchievementItem(Player player, int id){
            MinecraftItemRewardData data = (MinecraftItemRewardData) registry.getRewardData(id);
            if(data == null) {
                data = new MinecraftItemRewardData(id, 1, new ItemStack[0]);
                registry.insertRewardData(data).thenRun(()->{
                    player.sendMessage("해당 id가 존재하지 않아, 새로운 데이터를 생성했습니다.");
                    player.sendMessage("다시 명령어를 입력해서 보상을 설정해주세요.");
                });
            }else {
                AchievementEditGUI editGUI = new AchievementEditGUI(registry, data);
                editGUI.openGUI(player);
            }
        }

        @Subcommand("보상레벨")
        public void modifyAchievementLevel(Player player, int id, int level){
            registry.setRequiredLevel(id, level);
            player.sendMessage("보상 id: "+id+"번의 요구 레벨을 "+level+"로 설정했습니다.");
        }

        @Subcommand("로어")
        public void addLore(Player player, int level){
            String loreText = lore.replace("%level%", level+"");
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                List<Component> list = new ArrayList<>();
                if (meta.hasLore()) {
                    list = meta.lore();
                }
                list.add(Component.text(loreText));
                meta.lore(list);
                item.setItemMeta(meta);
                player.sendMessage("로어 설정 완료!");
            }else{
                player.sendMessage("로어 ㄴㄴ 불가");
            }
        }

        @Subcommand("레벨검증")
        public void checkLevel(Player player){
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                boolean isValidItem = false;
                if (meta != null) {
                    List<String> list = meta.getLore();
                    if(list != null) {
                        for (String lore : list) {
                            Matcher matcher = pattern.matcher(lore);
                            isValidItem = regexCheck(matcher);
                            if (isValidItem) {
                                player.sendMessage("레벨아이템 ㅇㅇ 값: "+getProvidingLevel(matcher));
                            }else{
                                player.sendMessage("ㄴㄴ 레벨아이템");
                            }
                        }
                    }
                }
                player.sendMessage("로어 설정 완료!");
            }else{
                player.sendMessage("로어 ㄴㄴ 불가 없음");
            }
        }

        private boolean regexCheck(Matcher matcher){
            return matcher.find();
        }

        private int getProvidingLevel(Matcher matcher){
            String value = matcher.group(0);
            try {
                return Integer.parseInt(value);
            }catch (IllegalArgumentException e){
                return 0;
            }
        }
    }
}
