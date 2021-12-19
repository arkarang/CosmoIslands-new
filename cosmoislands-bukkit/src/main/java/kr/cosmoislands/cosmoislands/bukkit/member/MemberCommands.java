package kr.cosmoislands.cosmoislands.bukkit.member;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.bukkit.Pair;
import com.minepalm.arkarangutils.invitation.InvitationService;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.chat.IslandChat;
import kr.cosmoisland.cosmoislands.api.player.*;
import kr.cosmoisland.cosmoislands.core.CosmoIslands;
import kr.cosmoislands.cosmochat.core.api.ChatPlayer;
import kr.cosmoislands.cosmochat.core.helper.CosmoChatHelper;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import kr.cosmoislands.cosmoislands.bukkit.utils.AbstractConfirmGUI;
import kr.cosmoislands.cosmoislands.bukkit.utils.ConfirmCompounds;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MemberCommands {

    @CommandAlias("섬")
    @RequiredArgsConstructor
    protected static class User{

        private final IslandPlayerRegistry playerRegistry;
        private final HelloPlayers playersModule;
        private final InvitationService memberInvitation;
        private final InvitationService internInvitation;
        private final CosmoChatHelper helper;
        private final BukkitExecutor executor;

        @Subcommand("양도")
        public void transfer(Player player, String member){
            CompletableFuture<UUID> toTransferUniqueIdFuture = playersModule.getUniqueID(member);
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());

            preconditions.hasIsland().thenCompose(hasIsland->{
                if(hasIsland){
                    return preconditions.isOwner(player.getUniqueId());
                }else{
                    player.sendMessage("당신은 섬에 소속되어 있지 않습니다.");
                    return CompletableFuture.completedFuture(null);
                }
            }).thenCombine(toTransferUniqueIdFuture, (isOwner, toTransfer)->{
                if(isOwner != null){
                    if(toTransfer == null){
                        player.sendMessage("해당 플레이어는 존재하지 않습니다.");
                        return null;
                    }

                    if(isOwner){
                        PlayerPreconditions toTransferPreconditions = PlayerPreconditions.of(toTransfer);
                        toTransferPreconditions.hasIsland().thenAccept(hasIsland -> {
                            if(hasIsland){
                                player.sendMessage("해당 플레이어는 섬이 존재합니다.");
                            }else{
                                preconditions.getIsland()
                                        .thenCompose(island->provideGUI(island, player.getUniqueId(), toTransfer))
                                        .thenAccept(gui->{
                                            executor.sync(()->gui.openGUI(player));
                                        });
                            }
                        });
                    }else{
                        player.sendMessage("당신은 섬장이 아닙니다.");
                    }
                }
                return null;
            });
        }

        private CompletableFuture<AbstractConfirmGUI> provideGUI(Island island, UUID owner, UUID toTransfer){
            CompletableFuture<String> ownerNameFuture, toTransferNameFuture;
            ownerNameFuture = playersModule.getUsername(owner);
            toTransferNameFuture = playersModule.getUsername(toTransfer);

            return ownerNameFuture.thenCombine(toTransferNameFuture, (ownerName, toTransferName)->{
                Pair<String, List<String>> confirm, reject;
                confirm = new Pair<>("섬 양도 수락", Collections.singletonList("클릭시 " + toTransferName + " 님에게 섬을 양도합니다."));
                reject = new Pair<>("섬 양도 거절", Collections.singletonList("클릭시 섬 양도를 취소합니다."));

                return new AbstractConfirmGUI(new ConfirmCompounds("섬 양도", confirm, reject) {

                    @Override
                    public void onConfirm(Player player) {
                        player.closeInventory();
                        IslandPlayersMap map = island.getComponent(IslandPlayersMap.class);
                        IslandChat chat = island.getComponent(IslandChat.class);
                        map.setOwner(playerRegistry.get(toTransfer));
                        helper.system(toTransfer).send("섬을 " + ownerName + "님으로부터 양도받았습니다.");
                        chat.sendSystem(toTransferName + "님이 섬을 " + ownerName + "님으로부터 양도받았습니다.");
                    }

                    @Override
                    public void onReject(Player player) {
                        player.closeInventory();
                        player.sendMessage("섬 양도를 거절했습니다.");
                    }

                });
            });
        }

        @Subcommand("목록")
        @CommandAlias("섬원|섬원 목록")
        public void listMember(Player player){
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            preconditions.hasIsland().thenCompose(hasIsland->{
                if(hasIsland){
                    return preconditions.getIsland();
                }else{
                    player.sendMessage("당신은 섬에 소속되어 있지 않습니다.");
                    return CompletableFuture.completedFuture(null);
                }
            }).thenAccept(island -> {
                if(island != null){
                    IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                    playersMap.getMembers().thenAccept(map->{
                        HashMap<UUID, MemberRank> filteredMap = new HashMap<>();
                        for (UUID uuid : map.keySet()) {
                            if(map.get(uuid).getPriority() >= MemberRank.MEMBER.getPriority()){
                                filteredMap.put(uuid, map.get(uuid));
                            }
                        }
                        MemberListGUI gui = new MemberListGUI(filteredMap, executor);
                        executor.sync(()->gui.openGUI(player));
                    });
                }
            });
        }

        @Subcommand("초대")
        public void invite(Player player, String name){
            CompletableFuture<UUID> receiverUuidFuture = playersModule.getUniqueID(name);
            CompletableFuture<String> receiverNameFuture = receiverUuidFuture.thenCompose(uuid->{
                if(uuid != null){
                    return playersModule.getUsername(uuid);
                }else
                    return null;
            });
            CompletableFuture<Boolean> isOnlineFuture = receiverUuidFuture.thenCompose(uuid->{
                if(uuid != null){
                    return playersModule.getProxied(uuid).isOnline();
                }else
                    return CompletableFuture.completedFuture(null);
            });
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            CompletableFuture<Boolean> hasSlotFuture = preconditions.getIsland().thenCompose(island->{
                if(island != null){
                    IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                    CompletableFuture<Integer> maxPlayersFuture = playersMap.getMaxPlayers();
                    CompletableFuture<Map<UUID, MemberRank>> membersFuture = playersMap.getMembers().thenApply(map->{
                        HashMap<UUID, MemberRank> newMap = new HashMap<>();
                        for (UUID uuid : map.keySet()) {
                            if(map.get(uuid).getPriority() > MemberRank.INTERN.getPriority()){
                                newMap.put(uuid, map.get(uuid));
                            }
                        }
                        return newMap;
                    });
                    return maxPlayersFuture.thenCombine(membersFuture, (maxPlayers, map)-> maxPlayers > map.size());
                }
                return CompletableFuture.completedFuture(null);
            });

            preconditions.getIsland().thenCombine(receiverUuidFuture, (island, receiverUuid) -> {
                if(island == null){
                    player.sendMessage("당신은 섬에 소속되어 있지 않습니다.");
                }else{
                    if(receiverUuid == null){
                        player.sendMessage("존재하지 않는 플레이어입니다.");
                    } else{
                        return preconditions.isOwner(player.getUniqueId()).thenApply(isOwner->{
                            if(isOwner){
                                return PlayerPreconditions.of(receiverUuid);
                            }else {
                                player.sendMessage("당신은 섬장이 아닙니다.");
                                return null;
                            }
                        });
                    }
                }
                return CompletableFuture.completedFuture((PlayerPreconditions)null);
            }).thenCompose(future->future).thenCombine(isOnlineFuture, (toTransferPreconditions, isOnline)->{
                CompletableFuture<Island> result = CompletableFuture.completedFuture(null);

                if(toTransferPreconditions != null){
                    result = toTransferPreconditions.hasIsland().thenCompose(hasIsland->{
                        if(hasIsland){
                            player.sendMessage("해당 플레이어는 섬이 존재합니다.");
                        }else{
                            if(isOnline != null){
                                if(isOnline){
                                    return preconditions.getIsland();
                                }else{
                                    player.sendMessage("해당 플레이어는 오프라인입니다.");
                                }
                            }
                        }
                        return CompletableFuture.completedFuture((Island)null);
                    });
                }

                return result;
            }).thenCombine(receiverUuidFuture, (island, receiverUuid) -> {
                CompletableFuture<Boolean> isMemberFuture, isInternFuture;
                isMemberFuture = preconditions.isMember(receiverUuid);
                isInternFuture = preconditions.isIntern(receiverUuid);
                isMemberFuture.thenCombine(isInternFuture, (isMember, isIntern)->{
                    if(isMember){
                        player.sendMessage("해당 플레이어는 이미 섬원입니다.");
                        return false;
                    }else if(isIntern){
                        player.sendMessage("해당 플레이어는 섬 알바입니다.");
                        return false;
                    }
                    return true;
                }).thenCombine(hasSlotFuture, (notMemberOf, hasSlot)->{
                    if(notMemberOf){
                        if(hasSlot){
                            memberInvitation.sender(player.getUniqueId()).canInvite(receiverUuid).thenAccept(canInvite->{
                                receiverNameFuture.thenAccept(username->{
                                    if(canInvite){
                                        memberInvitation.sender(player.getUniqueId()).invite(receiverUuid);
                                        player.sendMessage("플레이어 "+username+"님에게 섬 초대 메세지를 보냈습니다.");
                                    }else{
                                        player.sendMessage("이미 "+username+"님에게 초대를 보냈습니다.");
                                    }
                                });
                            });
                        }else{
                            player.sendMessage("섬이 꽉 차 초대할수 없습니다.");
                        }
                    }
                    return false;
                });
                return null;
            });
        }

        @Subcommand("초대 수락")
        public void accept(Player player, String name){
            executeMember(player, name, true);
        }

        @Subcommand("초대 거절")
        public void deny(Player player, String name){
            executeMember(player, name, false);
        }

        private void executeMember(Player player, String name, boolean accepted){
            CompletableFuture<UUID> senderFuture = playersModule.getUniqueID(name);
            senderFuture.thenAccept(sender -> {
                if(sender == null){
                    player.sendMessage("올바르지 않은 플레이어 이름 입니다.");
                    return;
                }

                PlayerPreconditions playerPreconditions, senderPreconditions;
                playerPreconditions = PlayerPreconditions.of(player.getUniqueId());
                senderPreconditions = PlayerPreconditions.of(sender);
                CompletableFuture<Boolean> hasSlotFuture = senderPreconditions.getIsland().thenCompose(island->{
                    if(island != null){
                        IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                        CompletableFuture<Integer> maxInternsFuture = playersMap.getMaxPlayers();
                        CompletableFuture<Map<UUID, MemberRank>> membersFuture = playersMap.getMembers().thenApply(map->{
                            HashMap<UUID, MemberRank> filteredMap = new HashMap<>();
                            for (UUID uuid : map.keySet()) {
                                if(map.get(uuid).getPriority() >= MemberRank.MEMBER.getPriority()){
                                    filteredMap.put(uuid, map.get(uuid));
                                }
                            }
                            return filteredMap;
                        });
                        return maxInternsFuture.thenCombine(membersFuture, (maxMembers, map)-> maxMembers > map.size());
                    }
                    return CompletableFuture.completedFuture(null);
                });

                CompletableFuture<Boolean> senderHasIslandFuture = senderPreconditions.hasIsland();
                playerPreconditions.hasIsland().thenCombine(senderHasIslandFuture, (hasIsland, senderHasIsland)->{
                    if(hasIsland){
                        player.sendMessage("당신은 이미 소속된 섬이 존재합니다.");
                        return false;
                    }else if(senderHasIsland){
                        player.sendMessage("해당 플레이어는 섬에 소속되어 있지 않습니다.");
                        return false;
                    }
                    return true;
                }).thenCombine(hasSlotFuture, (passed, hasSlot)->{
                    if(passed){
                        if(hasSlot){
                            return true;
                        }else{
                            player.sendMessage("이미 해당 섬은 최대 가입 가능한 섬원 수에 도달했습니다.");
                        }
                    }
                    return false;
                }).thenAccept(canExecute->{
                    if(canExecute){
                        if(accepted) {
                            memberInvitation.receiver(player.getUniqueId()).accept(sender);
                        }else {
                            memberInvitation.receiver(player.getUniqueId()).deny(sender);
                        }
                    }
                });
            });
        }


        @Subcommand("탈퇴")
        public void leave(Player player){
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            CompletableFuture<Boolean> hasIslandFuture = preconditions.hasIsland();
            CompletableFuture<Boolean> isOwnerFuture = preconditions.hasIsland().thenCompose(hasIsland->{
                if(hasIsland){
                    return preconditions.isOwner(player.getUniqueId());
                }else
                    return CompletableFuture.completedFuture(false);
            });

            hasIslandFuture.thenCombine(isOwnerFuture, (hasIsland, isOwner)->{
                if(hasIsland){
                    if(isOwner){
                        player.sendMessage("섬장은 탈퇴할수 없습니다.");
                    }else{
                        preconditions.getIsland().thenAccept(island ->{
                            IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                            playersMap.removeMember(playerRegistry.get(player.getUniqueId()))
                                    .thenRun(()->{
                                        player.sendMessage("섬을 탈퇴했습니다.");
                                    });
                        });
                    }
                }else{
                    player.sendMessage("당신은 섬에 소속되어 있지 않습니다.");
                }
                return null;
            });
        }

        @Subcommand("추방")
        public void kick(Player player, String name){
            PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
            CompletableFuture<UUID> targetUniqueIdFuture = playersModule.getUniqueID(name);
            CompletableFuture<String> targetNameFuture = targetUniqueIdFuture.thenCompose(uuid->{
                if(uuid != null){
                    return playersModule.getUsername(uuid);
                }else
                    return null;
            });
            CompletableFuture<Boolean> hasIslandFuture = preconditions.hasIsland();
            CompletableFuture<Boolean> isOwnerFuture = hasIslandFuture.thenCompose(hasIsland->{
                if(hasIsland){
                    return preconditions.isOwner(player.getUniqueId());
                }else
                    return CompletableFuture.completedFuture(false);
            });
            CompletableFuture<Boolean> isMemberOf = targetUniqueIdFuture.thenCompose(uuid->{
                if(uuid != null){
                    return preconditions.isMember(uuid);
                }else
                    return CompletableFuture.completedFuture(false);
            });

            preconditions.getIsland().thenAccept(island->{
                IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                IslandChat chat = island.getComponent(IslandChat.class);

                hasIslandFuture.thenCombine(isOwnerFuture, (hasIsland, isOwner)->{
                    if(hasIsland){
                        if(isOwner){
                            return true;
                        }else{
                            player.sendMessage("당신은 섬장이 아닙니다.");
                        }
                    }else{
                        player.sendMessage("당신은 섬에 소속되어 있지 않습니다.");
                    }
                    return false;
                }).thenCombine(isMemberOf, (canExecute, isMember)->{
                    if(canExecute){
                        if(player.getName().equalsIgnoreCase(name)){
                            player.sendMessage("자기 자신은 강퇴할 수 없습니다.");
                            return null;
                        }
                        if(isMember){
                            targetUniqueIdFuture.thenCombine(targetNameFuture, (uuid, username)->{
                                if(uuid != null){
                                    playersMap.removeMember(playerRegistry.get(uuid));
                                    if(username != null){
                                        chat.sendSystem(player.getName()+"님이 "+username+"님을 섬에서 추방했습니다.");
                                        player.sendMessage(username+"님을 추방했습니다.");
                                    }
                                }
                                return null;
                            });
                        }else{
                            player.sendMessage("해당 플레이어는 섬원이 아닙니다.");
                        }
                    }
                    return null;
                });
            });
        }

            @Subcommand("알바")
            public void list(Player player) {
                player.sendMessage("알바 목록 조회 중...");
                PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());

                preconditions.hasIsland().thenCompose(hasIsland->{
                    if(hasIsland){
                        return preconditions.getIsland();
                    }else{
                        player.sendMessage("당신은 섬에 소속되어 있지 않습니다.");
                        return CompletableFuture.completedFuture(null);
                    }
                }).thenAccept(island -> {
                    if(island != null){
                        IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                        playersMap.getMembers().thenAccept(map->{
                            List<UUID> list = new ArrayList<>();
                            UUID owner = null;
                            for (UUID uuid : map.keySet()) {
                                if(map.get(uuid).getPriority() == MemberRank.INTERN.getPriority()){
                                    list.add(uuid);
                                }
                                if(map.get(uuid) == MemberRank.OWNER){
                                    owner = uuid;
                                }
                            }
                            InternListGUI gui = new InternListGUI(owner, list, executor);
                            executor.sync(()->gui.openGUI(player));
                        });
                    }
                });
            }

            @Subcommand("알바 초대")
            public void inviteIntern(Player player, String name) {
                CompletableFuture<UUID> receiverUuidFuture = playersModule.getUniqueID(name);
                CompletableFuture<String> receiverNameFuture = receiverUuidFuture.thenCompose(uuid->{
                    if(uuid != null){
                        return playersModule.getUsername(uuid);
                    }else
                        return null;
                });
                CompletableFuture<Boolean> isOnlineFuture = receiverUuidFuture.thenCompose(uuid->{
                    if(uuid != null){
                        return playersModule.getProxied(uuid).isOnline();
                    }else
                        return CompletableFuture.completedFuture(null);
                });

                PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());

                CompletableFuture<Boolean> hasSlotFuture = preconditions.getIsland().thenCompose(island->{
                    if(island != null){
                        IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                        CompletableFuture<Integer> maxInternsFuture = playersMap.getMaxInterns();
                        CompletableFuture<Map<UUID, MemberRank>> internsFuture = playersMap.getMembers().thenApply(map->{
                            HashMap<UUID, MemberRank> newMap = new HashMap<>();
                            for (UUID uuid : map.keySet()) {
                                if(map.get(uuid).getPriority() == MemberRank.INTERN.getPriority()){
                                    newMap.put(uuid, map.get(uuid));
                                }
                            }
                            return newMap;
                        });
                        return maxInternsFuture.thenCombine(internsFuture, (maxInterns, map)-> maxInterns > map.size());
                    }
                    return CompletableFuture.completedFuture(null);
                });

                preconditions.getIsland().thenCombine(receiverUuidFuture, (island, receiverUuid) -> {
                    if(island == null){
                        player.sendMessage("당신은 섬에 소속되어 있지 않습니다.");
                    }else{
                        if(receiverUuid == null){
                            player.sendMessage("존재하지 않는 플레이어입니다.");
                        } else{
                            return preconditions.isOwner(player.getUniqueId()).thenApply(isOwner->{
                                if(isOwner){
                                    return PlayerPreconditions.of(receiverUuid);
                                }else {
                                    player.sendMessage("당신은 섬장이 아닙니다.");
                                    return null;
                                }
                            });
                        }
                    }
                    return CompletableFuture.completedFuture((PlayerPreconditions)null);
                }).thenCompose(future->future).thenCombine(isOnlineFuture, (toTransferPreconditions, isOnline)->{
                    CompletableFuture<Island> result = CompletableFuture.completedFuture(null);

                    if(toTransferPreconditions != null){
                        result = toTransferPreconditions.hasIsland().thenCompose(hasIsland->{
                            if(hasIsland){
                                player.sendMessage("해당 플레이어는 섬이 존재합니다.");
                            }else{
                                if(isOnline != null){
                                    if(isOnline){
                                        return preconditions.getIsland();
                                    }else{
                                        player.sendMessage("해당 플레이어는 오프라인입니다.");
                                    }
                                }
                            }
                            return CompletableFuture.completedFuture((Island)null);
                        });
                    }

                    return result;
                }).thenCombine(receiverUuidFuture, (island, receiverUuid) -> {
                    CompletableFuture<Boolean> isMemberFuture, isInternFuture;
                    isMemberFuture = preconditions.isMember(receiverUuid);
                    isInternFuture = preconditions.isIntern(receiverUuid);

                    IslandPlayer ip = playerRegistry.get(receiverUuid);
                    IslandInternship internship = ip.getInternship();
                    CompletableFuture<Boolean> exceededPersonalMaxInterns = internship.getMaxInternships()
                            .thenCombine(internship.getHiredIslands(), (max, list)-> max <= list.size());

                    isMemberFuture.thenCombine(isInternFuture, (isMember, isIntern)->{
                        if(isMember){
                            player.sendMessage("해당 플레이어는 섬원입니다.");
                            return false;
                        }else if(isIntern){
                            player.sendMessage("해당 플레이어는 이미 섬 알바입니다.");
                            return false;
                        }
                        return true;
                    }).thenCombine(exceededPersonalMaxInterns, (hasSlot, exceeded)->{
                        if(exceeded){
                            player.sendMessage("해당 플레이어는 이미 소속 가능한 최대 섬 알바 수에 도달했습니다.");
                            return false;
                        }
                        return hasSlot;
                    }).thenCombine(hasSlotFuture, (finallyExecutable, hasSlot)->{
                        if(finallyExecutable){
                            if(hasSlot){
                                internInvitation.sender(player.getUniqueId()).canInvite(receiverUuid).thenAccept(canInvite->{
                                    receiverNameFuture.thenAccept(username->{
                                        if(canInvite){
                                            internInvitation.sender(player.getUniqueId()).invite(receiverUuid);
                                            player.sendMessage("플레이어 "+username+"님에게 섬 초대 메세지를 보냈습니다.");
                                        }else{
                                            player.sendMessage("이미 "+username+"님에게 초대를 보냈습니다.");
                                        }
                                    });
                                });
                            }else{
                                player.sendMessage("섬이 꽉 차 초대할수 없습니다.");
                            }
                        }
                        return false;
                    });
                    return null;
                });
            }

            @Subcommand("알바 추방")
            public void removeIntern(Player player, String name){
                PlayerPreconditions preconditions = PlayerPreconditions.of(player.getUniqueId());
                CompletableFuture<UUID> targetUniqueIdFuture = playersModule.getUniqueID(name);
                CompletableFuture<String> targetNameFuture = targetUniqueIdFuture.thenCompose(uuid->{
                    if(uuid != null){
                        return playersModule.getUsername(uuid);
                    }else
                        return null;
                });
                CompletableFuture<Boolean> hasIslandFuture = preconditions.hasIsland();
                CompletableFuture<Boolean> isOwnerFuture = hasIslandFuture.thenCompose(hasIsland->{
                    if(hasIsland){
                        return preconditions.isOwner(player.getUniqueId());
                    }else
                        return CompletableFuture.completedFuture(false);
                });
                CompletableFuture<Boolean> isInternOf = targetUniqueIdFuture.thenCompose(uuid->{
                    if(uuid != null){
                        return preconditions.isIntern(uuid);
                    }else
                        return CompletableFuture.completedFuture(false);
                });

                preconditions.getIsland().thenAccept(island->{
                    IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                    IslandChat chat = island.getComponent(IslandChat.class);

                    hasIslandFuture.thenCombine(isOwnerFuture, (hasIsland, isOwner)->{
                        if(hasIsland){
                            if(isOwner){
                                return true;
                            }else{
                                player.sendMessage("당신은 섬장이 아닙니다.");
                            }
                        }else{
                            player.sendMessage("당신은 섬에 소속되어 있지 않습니다.");
                        }
                        return false;
                    }).thenCombine(isInternOf, (canExecute, isIntern)->{
                        if(canExecute){
                            if(player.getName().equalsIgnoreCase(name)){
                                player.sendMessage("자기 자신은 강퇴할 수 없습니다.");
                                return null;
                            }
                            if(isIntern){
                                targetUniqueIdFuture.thenCombine(targetNameFuture, (uuid, username)->{
                                    if(uuid != null){
                                        playersMap.removeIntern(uuid);
                                        if(username != null){
                                            chat.sendSystem(player.getName()+"님이 "+username+"님을 섬 알바에서 추방했습니다.");
                                            player.sendMessage(username+"님을 추방했습니다.");
                                        }
                                    }
                                    return null;
                                });
                            }else{
                                player.sendMessage("해당 플레이어는 섬 알바가 아닙니다.");
                            }
                        }
                        return null;
                    });
                });
            }

            @Subcommand("알바 수락")
            public void acceptIntern(Player player, String username) {
                internExecute(player, username, true);
            }

            @Subcommand("알바 거절")
            public void denyIntern(Player player, String username) {
                internExecute(player, username, false);
            }

            private void internExecute(Player player, String name, boolean accepted) {
                CompletableFuture<UUID> senderFuture = playersModule.getUniqueID(name);
                senderFuture.thenAccept(sender -> {
                    if(sender == null){
                        player.sendMessage("올바르지 않은 플레이어 이름 입니다.");
                        return;
                    }

                    PlayerPreconditions playerPreconditions, senderPreconditions;
                    playerPreconditions = PlayerPreconditions.of(player.getUniqueId());
                    senderPreconditions = PlayerPreconditions.of(sender);

                    CompletableFuture<Boolean> senderHasIslandFuture = senderPreconditions.hasIsland();
                    CompletableFuture<Boolean> memberRankFuture = playerPreconditions.hasRank(player.getUniqueId(), MemberRank.INTERN);
                    CompletableFuture<Boolean> hasSlotFuture = senderPreconditions.getIsland().thenCompose(island->{
                        if(island != null){
                            IslandPlayersMap playersMap = island.getComponent(IslandPlayersMap.class);
                            CompletableFuture<Integer> maxInternsFuture = playersMap.getMaxInterns();
                            CompletableFuture<Map<UUID, MemberRank>> internsFuture = playersMap.getMembers().thenApply(map->{
                                HashMap<UUID, MemberRank> newMap = new HashMap<>();
                                for (UUID uuid : map.keySet()) {
                                    if(map.get(uuid).getPriority() == MemberRank.INTERN.getPriority()){
                                        newMap.put(uuid, map.get(uuid));
                                    }
                                }
                                return newMap;
                            });
                            return maxInternsFuture.thenCombine(internsFuture, (maxInterns, map)-> maxInterns > map.size());
                        }
                        return CompletableFuture.completedFuture(null);
                    });

                    IslandPlayer ip = playerRegistry.get(player.getUniqueId());
                    IslandInternship internship = ip.getInternship();
                    CompletableFuture<Boolean> exceededPersonalMaxInterns = internship.getMaxInternships()
                            .thenCombine(internship.getHiredIslands(), (max, list)-> max <= list.size());

                    playerPreconditions.hasIsland().thenCombine(senderHasIslandFuture, (hasIsland, senderHasIsland)->{
                        if(hasIsland){
                            player.sendMessage("당신은 이미 소속된 섬이 존재합니다.");
                            return false;
                        }else if(senderHasIsland){
                            player.sendMessage("해당 플레이어는 섬에 소속되어 있지 않습니다.");
                            return false;
                        }
                        return true;
                    }).thenCombine(hasSlotFuture, (passed, hasSlot)->{
                        if(passed){
                            if(hasSlot){
                                return true;
                            }else{
                                player.sendMessage("이미 해당 섬은 최대 가입 가능한 알바원의 수에 도달했습니다.");
                            }
                        }
                        return false;
                    }).thenCombine(exceededPersonalMaxInterns, (hasSlot, exceeded)->{
                        if(exceeded){
                            player.sendMessage("해당 플레이어는 이미 소속 가능한 최대 섬 알바 수에 도달했습니다.");
                            return false;
                        }
                        return hasSlot;
                    }).thenCombine(memberRankFuture, (canExecute, hasRank)->{
                        if(canExecute){
                            if(hasRank){
                                player.sendMessage("당신은 이미 해당 섬에 소속되어 있습니다.");
                            }else {
                                if (accepted) {
                                    memberInvitation.receiver(player.getUniqueId()).accept(sender);
                                } else {
                                    memberInvitation.receiver(player.getUniqueId()).deny(sender);
                                }
                            }
                        }
                        return null;
                    });
                });
            }
    }
}
