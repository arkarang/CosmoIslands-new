package kr.cosmoislands.cosmoislands.bukkit.bank;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import kr.cosmoislands.cosmoislands.api.IslandService;
import kr.cosmoislands.cosmoislands.api.bank.IslandInventory;
import kr.cosmoislands.cosmoislands.api.bank.IslandVault;
import kr.cosmoislands.cosmoislands.bukkit.PlayerPreconditions;
import kr.cosmoislands.cosmoislands.core.DebugLogger;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class BankCommands {

    public static void init(IslandService service, PaperCommandManager manager){
        Economy economy = service.getExternalRepository().getRegisteredService(Economy.class);
        manager.registerCommand(new Admin());
        manager.registerCommand(new User(economy));
    }

    @CommandAlias("섬")
    @RequiredArgsConstructor
    protected static class User extends BaseCommand{

        private final Economy economy;
        private final DecimalFormat format = new DecimalFormat("#");

        @Subcommand("창고")
        public void openInventory(Player player){
            PlayerPreconditions
                    .of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island->{
                        if(island == null){
                            player.sendMessage("섬이 존재하지 않습니다.");
                            return;
                        }
                        IslandInventory bank = island.getComponent(IslandInventory.class);
                        bank.openInventory(player.getUniqueId());
                    });
        }

        @Subcommand("금고")
        public void info(Player player){
            PlayerPreconditions
                    .of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island -> {
                        if(island == null){
                            player.sendMessage("섬이 존재하지 않습니다.");
                            return;
                        }
                        IslandVault vault = island.getComponent(IslandVault.class);
                        vault.getMoney().thenAccept(money->{
                            player.sendMessage("섬 금고: "+format.format(money)+"G");
                        });
                    });
        }

        @Subcommand("금고 넣기")
        public void deposit(Player player, int amount){
            if(amount <= 0){
                player.sendMessage("올바른 숫자를 입력해주세요.");
                return;
            }
            PlayerPreconditions
                    .of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island -> {
                        if(island == null){
                            player.sendMessage("섬이 존재하지 않습니다.");
                            return;
                        }
                        if( economy.getBalance(player) >= amount){
                            IslandVault vault = island.getComponent(IslandVault.class);
                            val execution = vault.addMoney(amount).thenRun(()->{
                                player.sendMessage("섬에 돈 "+amount+"G 를 입금했습니다.");
                                economy.withdrawPlayer(player, amount);
                            });
                            DebugLogger.handle("addMoney", execution);
                        }else{
                            player.sendMessage("잔액이 부족합니다!");
                        }
                    });
        }

        @Subcommand("금고 빼기")
        public void withdraw(Player player, int amount){
            if(amount <= 0){
                player.sendMessage("올바른 숫자를 입력해주세요.");
                return;
            }
            PlayerPreconditions
                    .of(player.getUniqueId())
                    .getIsland()
                    .thenAccept(island -> {
                        if(island == null){
                            player.sendMessage("섬이 존재하지 않습니다.");
                            return;
                        }
                        IslandVault vault = island.getComponent(IslandVault.class);
                        vault.getMoney().thenAccept(money -> {
                            if( amount <= money ){
                                vault.takeMoney(amount).thenRun(()-> {
                                    player.sendMessage("섬에 돈 "+amount+"G 를 출금했습니다.");
                                    economy.depositPlayer(player, amount);
                                });
                            }else{
                                player.sendMessage("잔액이 부족합니다!");
                            }
                        });
                    });
        }

    }

    @CommandAlias("섬관리")
    @CommandPermission("cosmoislands.admin")
    protected static class Admin extends BaseCommand{

        @Subcommand("입금")
        public void deposit(){
            //todo: implements this.
        }

        @Subcommand("출금")
        public void withdraw(){
            //todo: implements this.
        }

    }

}
