package kr.cosmoislands.cosmoislands.bukkit.chat;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import kr.cosmoisland.cosmoislands.api.IslandService;
import kr.cosmoisland.cosmoislands.api.chat.IslandChat;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import org.bukkit.entity.Player;

public class ChatCommands {

    public static void init(IslandService service, PaperCommandManager manager){
        manager.registerCommand(new User());
        manager.registerCommand(new Admin());
    }

    @CommandAlias("섬")
    protected static class User extends BaseCommand {

        @Subcommand("채팅")
        public void chat(Player player){
            PlayerPreconditions
                    .of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island -> {
                        if(island == null){
                            player.sendMessage("당신은 섬이 없습니다.");
                            return;
                        }
                        IslandChat chat = island.getComponent(IslandChat.class);
                        chat.switchChannel(player.getUniqueId()).thenAccept(toggle->{
                            if(toggle) {
                                player.sendMessage("채팅 채널을 섬으로 바꾸었습니다.");
                            }else {
                                player.sendMessage("채팅 채널을 바꾸었습니다.");
                            }
                        });
                    });
        }
    }

    @CommandAlias("섬관리")
    protected static class Admin extends BaseCommand {

        public void chatSpy(Player player){
            //todo: implements this.
        }

    }
}
