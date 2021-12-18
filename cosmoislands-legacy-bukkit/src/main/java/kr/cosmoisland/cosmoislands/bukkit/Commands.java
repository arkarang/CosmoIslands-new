package kr.cosmoisland.cosmoislands.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.invitation.InvitationService;
import com.minepalm.helloplayer.core.HelloPlayers;
import lombok.RequiredArgsConstructor;

@CommandAlias("섬")
@RequiredArgsConstructor
public class Commands extends BaseCommand {

    private final BukkitExecutor executor;
    private final HelloPlayers players;
    private final InvitationService memberInvitation, internInvitation;
    private final CosmoTeleport teleporter;


}