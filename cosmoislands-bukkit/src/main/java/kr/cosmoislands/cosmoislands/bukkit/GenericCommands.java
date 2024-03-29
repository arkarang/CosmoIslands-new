package kr.cosmoislands.cosmoislands.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import io.lettuce.core.api.async.RedisAsyncCommands;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.member.MemberRank;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class GenericCommands {

    public static void init(PaperCommandManager manager, IslandService service, RedisAsyncCommands<String, String> async, BukkitExecutor executor){
        UserOperationCooldown cooldown = new UserOperationCooldown(async);
        manager.registerCommand(new User(service, cooldown, executor));
    }

    @CommandAlias("섬")
    @RequiredArgsConstructor
    protected static class User extends BaseCommand {

        private final IslandService service;
        private final UserOperationCooldown cooldown;
        private final BukkitExecutor executor;

        @Subcommand("도움말")
        @Default
        public void help(Player player, @Default("1") int i){
            switch (i){
                default:
                case 1:
                    player.sendMessage("/섬 정보 - 자기가 속해있는 섬의 정보를 봅니다.");
                    player.sendMessage("/섬 알바 - 속한 섬 알바원 목록을 봅니다.");
                    player.sendMessage("/섬 알바 초대 <닉네임> - 섬 알바를 초대합니다.");
                    player.sendMessage("/섬 알바 추방 <닉네임> - 섬 알바를 추방합니다.");
                    player.sendMessage("/섬 창고 - 섬 창고를 엽니다.");
                    player.sendMessage("/섬 레벨 랭킹 - 섬 레벨 랭킹을 봅니다.");
                    break;
                case 2:
                    player.sendMessage("/섬 강화 - 섬 강화 GUI를 엽니다.");
                    player.sendMessage("/섬 생성 - 섬을 생성합니다.");
                    player.sendMessage("/섬 잠금 - 섬을 잠그거나 엽니다.");
                    player.sendMessage("/섬 가기 - 자신의 섬으로 갑니다.");
                    player.sendMessage("/섬 레벨 - 섬 레벨 정보를 봅니다.");
                    player.sendMessage("/섬 레벨 보상 - 섬 레벨 보상을 획득합니다.");
                    break;
                case 3:
                    player.sendMessage("/섬 양도 <섬원> - 섬을 양도합니다.");
                    player.sendMessage("/섬 목록 - 섬원 목록을 봅니다.");
                    player.sendMessage("/섬 초대 <유저> - 해당 유저를 자신의 섬에 초대합니다.");
                    player.sendMessage("/섬 탈퇴 - 섬을 탈퇴합니다.");
                    player.sendMessage("/섬 추방 <플레이어> - 섬에서 해당 유저를 강퇴합니다.");
                    player.sendMessage("/섬 금고 - 섬 금고의 잔액을 확인합니다.");
                    break;
                case 4:
                    player.sendMessage("/섬 금고 넣기 <액수> - 자신의 돈을 섬에 입금합니다.");
                    player.sendMessage("/섬 금고 빼기 <액수> - 섬에서 돈을 출금합니다.");
                    player.sendMessage("/섬 인기도 랭킹 - 섬 인기도 랭킹을 확인합니다.");
                    player.sendMessage("/섬 인기도 올리기 - 자신이 서 있는 섬의 인기도를 올립니다.");
                    player.sendMessage("/섬 인기도 내리기 - 자신이 서 있는 섬의 인기도를 내립니다.");
                    player.sendMessage("/섬 채팅 - 채팅 모드를 전환합니다.");
                    break;
                case 5:
                    player.sendMessage("/섬 스폰설정 - 자신이 서 있는 자리를 섬의 스폰으로 설정합니다.");
                    player.sendMessage("/섬 워프 <닉네임> - 해당 워프 지점으로 텔레포트 합니다.");
                    player.sendMessage("/섬 워프설정 <이름> - 지금 서 있는 자리를 워프 지점으로 등록합니다.");
                    player.sendMessage("/섬 설정 - 섬 설정 GUI를 엽니다.");
                    player.sendMessage("/섬 삭제 - 자신의 섬을 삭제합니다.");
                    break;
            }
        }

        @Subcommand("생성")
        public void create(Player player){
            val canExecuteFuture = cooldown.canExecute(player, "create");
            val hasIslandFuture = PlayerPreconditions
                    .of(player.getUniqueId())
                    .hasIsland();
            val execution = hasIslandFuture.thenCombine(canExecuteFuture, (hasIsland, canExecute)->{
                if (hasIsland){
                    player.sendMessage("당신은 이미 섬에 소속되어 있습니다.");
                    return null;
                }

                if(canExecute){
                    cooldown.submit(player.getUniqueId(), "create");
                    val innerExecution
                            = service.getCloud().getLeastLoadedServer(100).thenCompose(islandServer->{
                        DebugLogger.log("create 1");
                        return islandServer.create(player.getUniqueId()).thenAccept(island->{
                            if(island != null){
                                player.sendMessage("섬을 성공적으로 생성했습니다 !");
                            }else{
                                player.sendMessage("섬을 생성하는데 실패했습니다... 관리자에게 문의해주세요.");
                            }
                            DebugLogger.log("command create completed: "+System.currentTimeMillis());
                        });
                    });
                    DebugLogger.handle("inner creation: ", innerExecution);
                    DebugLogger.timeout("inner creation: ", innerExecution, 30000L);
                } else{
                    player.sendMessage("아직은 섬을 다시 생성할수 없어요. (쿨타임 10분)");
                }
                return null;
            });
            DebugLogger.handle("create command", execution);
            DebugLogger.timeout("create command timeout", execution, 30000L);
        }

        @Subcommand("삭제")
        public void delete(Player player){
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            val canExecuteFuture = cooldown.canExecute(player, "delete");
            preconditions.hasIsland()
                    .thenCombine(canExecuteFuture, (hasIsland, canExecute)->{
                        if (!hasIsland){
                            player.sendMessage("당신은 섬에 소속되어 있지 않습니다..");
                            return CompletableFuture.completedFuture((Boolean)null);
                        }else if(!canExecute) {
                            player.sendMessage("아직은 섬을 삭제할수 없어요. (쿨타임 10분)");
                            return CompletableFuture.completedFuture((Boolean)null);
                        } else{
                            return preconditions
                                    .hasRank(player.getUniqueId(), MemberRank.OWNER);
                        }
                    })
                    .thenCompose(future -> future)
                    .thenApply(hasRank -> {
                        if(hasRank != null){
                            if(hasRank){
                                cooldown.submit(player.getUniqueId(), "delete");
                                return preconditions.getIsland().thenApply(island -> {
                                    val future = service.deleteIsland(island.getId());
                                    future.thenAccept(completed->{
                                        if(completed){
                                            player.sendMessage("섬을 삭제했습니다.");
                                        }else{
                                            player.sendMessage("섬을 삭제할수 없습니다.");
                                        }
                                    });
                                    return future;
                                });
                            }else{
                                player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    });
        }

        @Subcommand("정보")
        public void info(Player player){
            PlayerPreconditions
                    .of(player.getUniqueId())
                    .getIsland().thenAccept(island -> {
                        if( island == null ){
                            player.sendMessage("당신은 섬이 없습니다.");
                        }else {
                            IslandInfoGUI gui = new IslandInfoGUI(island, executor);
                            executor.sync(()->gui.openGUI(player));
                        }
            });
        }
    }
}
