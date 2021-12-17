package kr.cosmoisland.cosmoislands.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.SimpleGUI;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.bank.IslandBank;
import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.bukkit.database.RewardSettingLoader;
import kr.cosmoisland.cosmoislands.bukkit.gui.IslandRewardEditGUI;
import kr.cosmoisland.cosmoislands.bukkit.island.RewardSetting;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@CommandAlias("섬관리")
@CommandPermission("cosmoislands.admin")
public class AdminCommands extends BaseCommand {

    private static void template(Player player, String username, Consumer<IslandPlayer> con){
        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
            OfflinePlayer off = Bukkit.getOfflinePlayer(username);
            if(off == null){
                player.sendMessage("그런 플레이어는 존재하지 않아요.");
                return;
            }
            try {
                IslandPlayer ip = CosmoIslandsBukkitBootstrap.getInst().getIslandPlayer(off.getUniqueId());
                if(ip.getIslandId() == Island.NIL_ID){
                    player.sendMessage("해당 플레이어는 섬이 없어요.");
                    return;
                }
                con.accept(ip);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @Subcommand("보상설정")
    public void setRewards(Player player, int slot){
        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
            try {
                RewardSetting setting = CosmoIslandsBukkitBootstrap.getInst().getDatabase().getLoader(RewardSettingLoader.class).getReward(CosmoIslandsBukkitBootstrap.getInst().getYamlIslandConfig(), slot).get();
                SimpleGUI gui = new IslandRewardEditGUI(slot, setting);
                Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()-> gui.openGUI(player));
            } catch (InterruptedException | ExecutionException e) {
                player.sendMessage("명령어 실행 중 오류가 발생했습니다. 아카랑에게 문의해주세요....");
            }
        });
    }

    @Subcommand("강제양도")
    public void forceTransfer(Player player, String owner, String receiver){
        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
            OfflinePlayer from, to;
            from = Bukkit.getOfflinePlayer(owner);
            to = Bukkit.getOfflinePlayer(receiver);
            if(from == null || to == null){
                player.sendMessage("플레이어가 존재하지 않아요.");
                return;
            }
            try {
                IslandPlayer ipOwner, ipReceiver;
                ipOwner = CosmoIslandsBukkitBootstrap.getInst().getPlayersCache().get(from.getUniqueId());
                ipReceiver = CosmoIslandsBukkitBootstrap.getInst().getPlayersCache().get(to.getUniqueId());
                if(ipOwner.getIslandId() == Island.NIL_ID){
                    player.sendMessage("플레이어 "+from.getName()+"님은 섬을 가지고 있지 않습니다.");
                    return;
                }
                if(ipReceiver.getIslandId() != ipOwner.getIslandId()){
                    player.sendMessage("플레이어 "+to.getName()+"님은 "+from.getName()+"님과 같은 섬이 아닙니다.");
                    return;
                }
                Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ipOwner.getIslandId());
                IslandPlayersMap map = island.getPlayersMap().get();
                if(map.getOwner().get().getUniqueID().equals(from.getUniqueId())){
                    map.setOwner(ipReceiver).get();
                    ((IslandComponent)map).sync();
                    new WrappedPlayersMap(map).sendMessages("관리자 "+player.getName()+"님이 섬장을 "+from.getName()+"님에서 "+to.getName()+"님으로 강제양도 했습니다.");
                    player.sendMessage("명령어를 성공적으로 실행했습니다.");
                }else{
                    player.sendMessage("플레이어 "+from.getName()+" 님은 섬장이 아닙니다. 혹시 모르니 제대로 입력해주세요. 돌다리도 두드려보고 건너기.");
                }
            }catch (ExecutionException | InterruptedException e){
                player.sendMessage("명령어 실행 중 오류가 발생했습니다. 아카랑에게 문의해주세요....");
            }
        });
    }

    @Subcommand("채팅스파이")
    public void chatSpy(Player player){
        //todo: 채팅스파이 CosmoChat으로 바꾸기.
        /*
        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
            ChatSpyLoader loader = CosmoIslands.getInst().getDatabase().getLoader(ChatSpyLoader.class);
            try {
                boolean chatMode = loader.get(player.getUniqueId()).get();
                chatMode = !chatMode;
                loader.set(player.getUniqueId(), chatMode);
                if(chatMode){
                    player.sendMessage("채팅 스파이 모드를 활성화 합니다.");
                }else{
                    player.sendMessage("채팅 스파이 모드를 비활성화 합니다.");
                }
            } catch (InterruptedException | ExecutionException e) {
                player.sendMessage("명령어 실행 중 오류가 발생했습니다. 아카랑에게 문의해주세요....");
            }
        });
         */
    }

    @Subcommand("인기도")
    public class Point extends BaseCommand{

        @Subcommand("확인")
        public void info(Player player, String username){
            template(player, username, ip->{
                try {
                    Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                    player.sendMessage(username+" 님의 섬 인기도: "+island.getData().get().getPoints().get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }

        @Subcommand("설정")
        public void set(Player player, String username, int value){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandData data = island.getData().get();
                        data.setPoint(value);
                        ((IslandComponent)data).sync();
                        player.sendMessage("해당 섬의 인기도를 "+value+"로 설정했습니다.");
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        @Subcommand("추가")
        public void add(Player player, String username, int value){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandData data = island.getData().get();
                        data.addPoint(value);
                        ((IslandComponent)data).sync();
                        player.sendMessage("해당 섬의 인기도에 "+value+" 만큼의 인기도를 추가했습니다.");
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });

        }

        @Subcommand("빼기")
        public void take(Player player, String username, int value){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandData data = island.getData().get();
                        data.addPoint(-value);
                        ((IslandComponent)data).sync();
                        player.sendMessage("해당 섬의 인기도에 "+value+" 만큼의 인기도를 삭감했습니다.");
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });

        }
    }

    @Subcommand("금고")
    public class Money extends BaseCommand{
        @Subcommand("확인")
        public void info(Player player, String username){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandBank bank = island.getBank().get();

                        //((IslandComponent)bank).sync();
                        player.sendMessage("해당 섬이 보유하고 있는 잔고는 "+(bank.getMoney().get().intValue()+"G 입니다."));
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        @Subcommand("설정")
        public void set(Player player, String username, int value){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandBank bank = island.getBank().get();
                        bank.setMoney(value);
                        ((IslandComponent)bank).sync();
                        player.sendMessage("해당 섬이 보유하고 있는 잔고를 "+value+"G 로 설정했습니다.");
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        @Subcommand("추가")
        public void add(Player player, String username, int value){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandBank bank = island.getBank().get();
                        bank.addMoney(value);
                        ((IslandComponent)bank).sync();
                        player.sendMessage("해당 섬이 보유하고 있는 잔고에 "+value+"G 를 추가했습니다.");
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        @Subcommand("빼기")
        public void take(Player player, String username, int value){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandBank bank = island.getBank().get();
                        bank.takeMoney(value);
                        ((IslandComponent)bank).sync();
                        player.sendMessage("해당 섬이 보유하고 있는 잔고에 "+value+"G 를 삭감했습니다.");
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });
        }
    }

    @Subcommand("레벨")
    public class Level extends BaseCommand{

        @Subcommand("확인")
        public void info(Player player, String username){
            template(player, username, ip->{
                try {
                    Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                    player.sendMessage(username+" 님의 섬 레벨: "+island.getData().get().getLevel());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }

        @Subcommand("설정")
        public void set(Player player, String username, int value){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandData data = island.getData().get();
                        data.setLevel(value);
                        ((IslandComponent)data).sync();
                        player.sendMessage("해당 섬의 레벨을 "+value+"로 설정했습니다.");
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        @Subcommand("추가")
        public void add(Player player, String username, int value){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandData data = island.getData().get();
                        data.setLevel(data.getLevel().get()+value);
                        ((IslandComponent)data).sync();
                        player.sendMessage("해당 섬의 레벨에 "+value+" 만큼의 레벨을 추가했습니다.");
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });

        }

        @Subcommand("빼기")
        public void take(Player player, String username, int value){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                template(player, username, ip->{
                    try {
                        Island island = CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIsland(ip.getIslandId());
                        IslandData data = island.getData().get();
                        data.setLevel(data.getLevel().get()-value);
                        ((IslandComponent)data).sync();
                        player.sendMessage("해당 섬의 레벨에 "+value+" 만큼의 레벨을 삭감했습니다.");
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            });
        }
    }
}
