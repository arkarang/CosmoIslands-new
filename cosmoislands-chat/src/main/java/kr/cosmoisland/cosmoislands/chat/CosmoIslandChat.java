package kr.cosmoisland.cosmoislands.chat;

import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmochat.core.CosmoChat;
import kr.cosmoislands.cosmochat.core.api.ChatPlayer;
import kr.cosmoislands.cosmochat.privatechat.PrivateChat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class CosmoIslandChat implements IslandChat {

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
    public CompletableFuture<Boolean> switchChannel(UUID uuid) {
        ChatPlayer chatPlayer = core.getChatPlayerRegistry().getPlayer(uuid);
        return chatPlayer.getSpeaking().thenApply(speaking->{
            if(speaking.getFullName().equals(chat.getChannel().getFullName())){
                core.getDefaultChannel().thenAccept(chatPlayer::setSpeaking);
                return false;
            }else{
                core.getChatPlayerRegistry().getPlayer(uuid).setSpeaking(chat.getChannel());
                return true;
            }
        });
    }

    @Override
    public CompletableFuture<Void> disband() {
        return chat.disband();
    }

    @Override
    public <T extends IslandComponent> CompletableFuture<T> sync() {
        return CompletableFuture.completedFuture((T)this);
    }

    @Override
    public CompletableFuture<Void> invalidate() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean validate() {
        return false;
    }
}
