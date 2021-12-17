package kr.cosmoisland.cosmoislands.core;

import kr.cosmoisland.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.core.api.ChatPlayer;
import kr.cosmoislands.cosmochat.privatechat.PrivateChat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public abstract class AbstractIslandChat implements IslandChat {

    @Getter
    final int islandID;
    final CosmoChat core;
    final PrivateChat chat;

    @Override
    public CompletableFuture<UUID> getOwner() {
        return chat.getOwner();
    }

    @Override
    public CompletableFuture<Void> add(UUID uuid) {
        return chat.add(core.getChatPlayerRegistry().getPlayer(uuid));
    }

    @Override
    public CompletableFuture<Void> remove(UUID uuid) {
        return chat.remove(core.getChatPlayerRegistry().getPlayer(uuid));
    }

    @Override
    public void sendSystem(String message) {
        chat.getChannel().context(ChatPlayer.SYSTEM, "SYSTEM").send(message);
    }

    @Override
    public void sendPlayer(UUID uuid, String username, String message) {
        chat.getChannel().context(uuid, username).send(message);
    }

    @Override
    public CompletableFuture<Void> setOwner(UUID uuid) {
        return chat.setOwner(uuid);
    }

    @Override
    public CompletableFuture<Void> switchChannel(UUID uuid) {
        ChatPlayer chatPlayer = core.getChatPlayerRegistry().getPlayer(uuid);
        return chatPlayer.getSpeaking().thenAccept(speaking->{
            if(speaking.getFullName().equals(chat.getChannel().getFullName())){
                chatPlayer.setSpeaking(core.getDefaultChannel());
            }else{
                core.getChatPlayerRegistry().getPlayer(uuid).setSpeaking(chat.getChannel());
            }
        });
    }

    @Override
    public CompletableFuture<Void> disband() {
        return chat.disband();
    }

}
