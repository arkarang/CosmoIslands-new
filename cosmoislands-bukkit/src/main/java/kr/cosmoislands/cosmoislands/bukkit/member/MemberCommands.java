package kr.cosmoislands.cosmoislands.bukkit.member;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.Pair;
import com.minepalm.helloplayer.core.HelloPlayer;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmochat.core.api.ChatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MemberCommands {

    protected static class User{
        @Subcommand("양도")
        public void transfer(Player player, String member){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), () -> {
                runIslandPlayerExists(player, (ip) -> {
                    runIslandExists(player, ip, (island) -> {
                        IslandPlayer target;
                        Pair<String, List<String>> confirm, reject;
                        try {
                            IslandPlayer owner = island.getPlayersMap().get().getOwner().get();
                            if(owner.getUniqueId().equals(player.getUniqueId())) {
                                UUID targetUID = Bukkit.getOfflinePlayer(member).getUniqueId();
                                target = CosmoIslands.getInst().getIslandPlayer(targetUID);
                                if (target.getIslandID() != ip.getIslandID()) {
                                    player.sendMessage("해당 플레이어는 섬원이 아닙니다.");
                                    return;
                                }
                                confirm = new Pair<>("섬 양도 수락", Collections.singletonList("클릭시 " + member + " 님에게 섬을 양도합니다."));
                                reject = new Pair<>("섬 양도 거절", Collections.singletonList("클릭시 섬 양도를 취소합니다."));
                                ArkarangGUI gui = new AbstractConfirmGUI(new ConfirmCompounds("섬 양도", confirm, reject) {

                                    @Override
                                    public void onConfirm(Player player) {
                                        player.closeInventory();
                                        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), () -> {
                                            try {
                                                Island island = CosmoIslands.getInst().getIslandManager().getIsland(ip.getIslandID());
                                                IslandPlayersMap map = island.getPlayersMap().get();
                                                IslandChat chat = island.getChat().get();
                                                chat.setOwner(target.getUniqueId());
                                                map.setOwner(target);
                                                HelloPlayer hp = HelloPlayers.inst().getProxied(player.getUniqueId());
                                                CosmoIslands.getInst().sendMessage(hp.getUniqueID(), "섬을 " + player.getName() + "님으로부터 양도받았습니다.");
                                                ((IslandComponent) map).sync();
                                                CosmoIslands.getInst().syncPlayer(ip.getUniqueId());
                                                new WrappedPlayersMap(map).sendMessages(member + "님이 섬을 " + player.getName() + "님으로부터 양도받았습니다.");
                                            } catch (InterruptedException | ExecutionException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onReject(Player player) {
                                        player.closeInventory();
                                        player.sendMessage("섬 양도를 거절했습니다.");
                                    }

                                });
                                executor.sync(()->gui.openGUI(player));
                            }else{
                                player.sendMessage("당신은 섬장이 아닙니다.");
                            }
                        } catch (ExecutionException | InterruptedException e) {
                            player.sendMessage("명령어 수행 중 오류가 발생했습니다.");
                        }
                    });
                });
            });
        }

        @Subcommand("목록")
        @CommandAlias("섬원|섬원 목록")
        public void list(Player player){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
                runIslandPlayerExists(player, (ip)->{
                    player.sendMessage("섬원 목록 조회 중...");
                    try {
                        PlayersMapDataModel loader = CosmoIslands.getInst().getDatabase().getLoader(PlayersMapDataModel.class);
                        PlayersMapGUI gui = new PlayersMapGUI(new ArrayList<>(loader.getMembers(ip.getIslandID()).get().keySet()), executor);
                        Bukkit.getScheduler().runTask(CosmoIslands.getInst(), ()->gui.openGUI(player));
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        @Subcommand("초대")
        public class Invite extends BaseCommand {

            @Default
            public void invite(Player player, String name){
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
                    runIslandPlayerExists(player, (ip)->{
                        runIslandExists(player, ip, (island)->{
                            CompletableFuture<IslandPlayersMap> mapFuture = island.getPlayersMap();
                            CompletableFuture<UUID> uniqueId = players.getUniqueID(name);
                            CompletableFuture<String> usernameFuture = uniqueId.thenCompose(players::getUsername);
                            CompletableFuture<HelloPlayer> hp = uniqueId.thenApply(players::getProxied);
                            CompletableFuture<Boolean> isOnlineFuture = hp.thenCompose(HelloPlayer::isOnline);
                            CompletableFuture<IslandPlayer> getOwnerFuture = mapFuture.thenCompose(IslandPlayersMap::getOwner);
                            CompletableFuture<IslandData> dataFuture = island.getData();
                            CompletableFuture<IslandInternsMap> internsMapFuture = island.getInternsMap();
                            CompletableFuture<Integer> maxPlayersFuture = dataFuture.thenCompose(IslandData::getMaxPlayers);
                            Bukkit.getLogger().info("invite: 1");
                            hp.thenCombineAsync(mapFuture, (target, map)->{
                                try {
                                    Bukkit.getLogger().info("invite: 2");
                                    IslandPlayer islandTarget = CosmoIslands.getInst().getIslandPlayer(target.getUniqueID());
                                    if(islandTarget.getIslandID() != Island.NIL_ID){
                                        player.sendMessage("해당 플레이어는 섬이 존재합니다.");
                                        return null;
                                    }
                                    if(!target.isOnline().get()){
                                        player.sendMessage("해당 플레이어는 온라인 상태가 아닙니다.");
                                        return null;
                                    }
                                    IslandInternsMap internsMap = internsMapFuture.get();
                                    IslandPlayersMap playersMap = island.getPlayersMap().get();
                                    if(internsMap.isIntern(target.getUniqueID()).get()){
                                        player.sendMessage("해당 플레이어는 이미 섬 알바에 등록 되어 있습니다.");
                                        return null;
                                    }
                                    if(playersMap.isMember(target.getUniqueID()).get()){
                                        player.sendMessage("해당 플레이어는 이미 섬원입니다.");
                                        return null;
                                    }

                                    isOnlineFuture.thenCombine(getOwnerFuture, (isOnline, owner)->{
                                        if(player.getUniqueId().equals(owner.getUniqueId()) && ip.getIslandID() == owner.getIslandID()){
                                            dataFuture.thenCombine(map.getMembers(), (data, memberMap)->{
                                                maxPlayersFuture.thenAccept(maxPlayers->{
                                                    if(maxPlayers <= memberMap.size()){
                                                        player.sendMessage("섬 인원이 꽉 차 초대할 수 없습니다.");
                                                        return;
                                                    }else{
                                                        memberInvitation.sender(player.getUniqueId()).canInvite(target.getUniqueID()).thenAccept(canInvite->{
                                                            if(canInvite){
                                                                memberInvitation.sender(player.getUniqueId()).invite(target.getUniqueID());
                                                                usernameFuture.thenAccept(username->player.sendMessage("플레이어 "+username+"님에게 섬 초대 메세지를 보냈습니다."));

                                                            }else{
                                                                player.sendMessage("이미 "+name+"님에게 초대를 보냈습니다.");
                                                            }
                                                        });

                                                    }
                                                });
                                                return null;
                                            });
                                        }else{
                                            player.sendMessage("섬장만 섬에 플레이어를 초대할수 있습니다.");
                                        }
                                        return null;
                                    });
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            });
                        });
                    });
                });
            }

            @Subcommand("수락")
            public void accept(Player player, String name){
                execute(player, name, true);
            }

            @Subcommand("거절")
            public void deny(Player player, String name){
                execute(player, name, false);
            }

            private void execute(Player player, String name, boolean accepted){
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
                    CompletableFuture<UUID> senderFuture = players.getUniqueID(name);
                    senderFuture.thenAccept(sender -> {
                        try {
                            final IslandPlayer itself = CosmoIslands.getInst().getIslandPlayer(player.getUniqueId());
                            if(itself.getIslandID() != Island.NIL_ID){
                                player.sendMessage("당신은 섬이 존재합니다.");
                                return;
                            }
                            final IslandPlayer ip = CosmoIslands.getInst().getIslandPlayer(sender);
                            if(ip.getIslandID() == Island.NIL_ID){
                                player.sendMessage("올바르지 않은 플레이어 이름 입니다.");
                            }else{
                                if(accepted) {
                                    memberInvitation.receiver(player.getUniqueId()).accept(sender);
                                }else {
                                    memberInvitation.receiver(player.getUniqueId()).deny(sender);
                                }
                            }
                        } catch (ExecutionException e) {
                            player.sendMessage("플레이어 정보 조회 중 오류가 발생했습니다. 관리자에게 문의 해주세요.");
                        }
                    });
                });
            }
        }

        @Subcommand("탈퇴")
        public void leave(Player player){
            Bukkit.getScheduler().runTask(CosmoIslands.getInst(), ()->{
                runIslandPlayerExists(player, ip->{
                    try {
                        Island island = CosmoIslands.getInst().getIslandManager().getIsland(ip.getIslandID());
                        IslandPlayersMap map = island.getPlayersMap().get();
                        if(map.getOwner().get().getUniqueId().equals(player.getUniqueId())){
                            player.sendMessage("섬장은 탈퇴할수 없습니다.");
                            return;
                        }
                        map.removeMember(CosmoIslands.getInst().getIslandPlayer(player.getUniqueId()));
                        ((IslandComponent)map).sync();
                        CosmoIslands.getInst().syncPlayer(ip.getUniqueId());
                        player.sendMessage("섬에서 나갔습니다.");
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        @Subcommand("추방")
        public void kick(Player player, String name){
            Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
                runIslandPlayerExists(player, ip ->{
                    try {
                        Island island = CosmoIslands.getInst().getIslandManager().getIsland(ip.getIslandID());
                        IslandPlayersMap map = island.getPlayersMap().get();
                        IslandPlayer owner = map.getOwner().get();
                        if(owner.equals(ip)){
                            OfflinePlayer off = Bukkit.getOfflinePlayer(name);
                            if(off.getUniqueId().equals(player.getUniqueId())){
                                player.sendMessage("자기 자신은 강퇴할 수 없습니다.");
                                return;
                            }
                            if(!map.isMember(off.getUniqueId()).get()){
                                player.sendMessage("해당 플레이어는 섬원이 아닙니다.");
                                return;
                            }
                            island.getChat().thenApply(chat->chat.remove(off.getUniqueId()));
                            map.removeMember(CosmoIslands.getInst().getIslandPlayer(off.getUniqueId())).get();
                            ChatPlayer cp = CosmoIslands.getInst().getChat().getChatPlayerRegistry().getPlayer(off.getUniqueId());
                            cp.getSpeaking().thenAccept(speaking->{
                                if(speaking.getCategory().getType().name().equalsIgnoreCase("ISLAND")){
                                    CosmoIslands.getInst().getChat().getDefaultChannel().thenAccept(cp::setSpeaking);
                                }
                            });
                            new WrappedPlayersMap(map).sendMessages(player.getName()+"님이 "+off.getName()+"님을 섬에서 추방했습니다.");
                            ((IslandComponent)map).sync();
                            CosmoIslands.getInst().syncPlayer(off.getUniqueId());
                            player.sendMessage(off.getName()+"님을 추방했습니다.");
                        }else{
                            player.sendMessage("당신은 섬장이 아닙니다.");
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            });
        }
        @Subcommand("알바")
        public class Interns extends BaseCommand {

            @Default
            public void list(Player player) {
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), () -> {
                    runIslandPlayerExists(player, (ip) -> runIslandExists(player, ip, (i) -> {
                        player.sendMessage("알바 목록 조회 중...");
                        try {
                            InternsMapGUI gui = new InternsMapGUI(i, executor);
                            Bukkit.getScheduler().runTask(CosmoIslands.getInst(), () -> gui.openGUI(player));
                        } catch (ExecutionException | InterruptedException e) {
                            player.sendMessage("명령어 실행 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
                        }
                    }));
                });
            }

            @Subcommand("초대")
            public void invite(Player player, String name) {
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), () -> {
                    runIslandPlayerExists(player, (ip) -> {
                        try {
                            if (ip.getIslandID() == Island.NIL_ID) {
                                player.sendMessage("섬이 존재하지 않습니다.");
                                return;
                            }
                            Island island = CosmoIslands.getInst().getIslandManager().getIsland(ip.getIslandID());
                            IslandPlayer owner = island.getPlayersMap().get().getOwner().get();
                            if (!owner.getUniqueId().equals(player.getUniqueId())) {
                                player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                                return;
                            }
                            CompletableFuture<UUID> uuidFuture = players.getUniqueID(name);
                            uuidFuture.thenAccept(target -> {
                                try {
                                    if (target == null) {
                                        player.sendMessage("해당 플레이어는 존재하지 않는 플레이어입니다.");
                                        return;
                                    }
                                    IslandInternsMap map = island.getInternsMap().get();
                                    IslandPlayersMap playersMap = island.getPlayersMap().get();
                                    if (map.isIntern(target).get()) {
                                        player.sendMessage("해당 플레이어는 이미 섬 알바에 등록 되어 있습니다.");
                                        return;
                                    }
                                    if (playersMap.isMember(target).get()) {
                                        player.sendMessage("해당 플레이어는 이미 섬원입니다.");
                                        return;
                                    }
                                    internInvitation.sender(player.getUniqueId()).canInvite(target).thenAccept(canInvite -> {
                                        if (canInvite) {
                                            internInvitation.sender(player.getUniqueId()).invite(target);
                                            player.sendMessage(name + "님에게 섬 알바 초대를 보냈습니다.");
                                        } else {
                                            player.sendMessage("이미 " + name + "님에게 초대를 보냈습니다.");
                                        }
                                    });
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    });
                });
            }

            @Subcommand("추방")
            public void remove(Player player, String name){
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), ()->{
                    runIslandPlayerExists(player, (ip) -> {
                        try {
                            if(ip.getIslandID() == Island.NIL_ID){
                                player.sendMessage("섬이 존재하지 않습니다.");
                                return;
                            }
                            Island island = CosmoIslands.getInst().getIslandManager().getIsland(ip.getIslandID());
                            IslandPlayer owner = island.getPlayersMap().get().getOwner().get();
                            if(!owner.getUniqueId().equals(player.getUniqueId())){
                                player.sendMessage("섬장만 명령어를 실행할수 있습니다.");
                                return;
                            }
                            CompletableFuture<UUID> uuidFuture = players.getUniqueID(name);
                            uuidFuture.thenAccept(target->{
                                try{
                                    if(target == null){
                                        player.sendMessage("해당 플레이어는 존재하지 않는 플레이어입니다.");
                                        return;
                                    }
                                    IslandInternsMap map = island.getInternsMap().get();
                                    if(!map.isIntern(target).get()){
                                        player.sendMessage("해당 플레이어는 섬 알바원이 아닙니다.");
                                        return;
                                    }
                                    map.removeIntern(target).get();
                                    ((IslandComponent)map).sync();
                                    CosmoIslands.sendAll(new IslandPlayerSyncPacket(IslandPlayerSyncPacket.INTERNS, Collections.singletonList(target)));
                                    player.sendMessage("섬 알바원을 추방했습니다.");
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    });
                });

            }

            @Subcommand("수락")
            public void accept(Player player, String username) {
                internExecute(player, username, true);
            }

            @Subcommand("거절")
            public void deny(Player player, String username) {
                internExecute(player, username, false);
            }

            private void internExecute(Player player, String name, boolean accepted) {
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslands.getInst(), () -> {
                    CompletableFuture<UUID> senderFuture = players.getUniqueID(name);
                    senderFuture.thenAccept(sender -> {
                        try {
                            final IslandPlayer itself = CosmoIslands.getInst().getIslandPlayer(player.getUniqueId());
                            if (itself.getIslandID() != Island.NIL_ID) {
                                player.sendMessage("당신은 섬이 존재합니다.");
                                return;
                            }
                            final IslandPlayer ip = CosmoIslands.getInst().getIslandPlayer(sender);
                            if (ip.getIslandID() == Island.NIL_ID) {
                                player.sendMessage("올바르지 않은 플레이어 이름 입니다.");
                            } else {
                                if (accepted) {
                                    internInvitation.receiver(player.getUniqueId()).accept(sender);
                                } else {
                                    internInvitation.receiver(player.getUniqueId()).deny(sender);
                                }
                            }
                        } catch (ExecutionException e) {
                            player.sendMessage("플레이어 정보 조회 중 오류가 발생했습니다. 관리자에게 문의 해주세요.");
                        }
                    });
                });
            }
        }
    }
}
