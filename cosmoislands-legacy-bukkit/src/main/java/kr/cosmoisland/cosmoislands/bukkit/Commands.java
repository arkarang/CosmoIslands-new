package kr.cosmoisland.cosmoislands.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.invitation.InvitationService;
import com.minepalm.helloplayer.core.HelloPlayers;
import kr.cosmoisland.cosmoislands.api.internship.IslandIntern;
import kr.cosmoisland.cosmoislands.api.internship.IslandInternsMap;
import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.bukkit.database.IslandDataLoader;
import kr.cosmoisland.cosmoislands.bukkit.database.PlayersMapLoader;
import kr.cosmoisland.cosmoislands.bukkit.events.IslandCreateEvent;
import kr.cosmoisland.cosmoislands.bukkit.events.IslandLoadedEvent;
import kr.cosmoisland.cosmoislands.bukkit.island.IslandLocal;
import kr.cosmoisland.cosmoislands.bukkit.island.IslandVoter;
import kr.cosmoisland.cosmoislands.bukkit.utils.IslandUtils;
import lombok.RequiredArgsConstructor;

@CommandAlias("섬")
@RequiredArgsConstructor
public class Commands extends BaseCommand {

    private final BukkitExecutor executor;
    private final HelloPlayers players;
    private final InvitationService memberInvitation, internInvitation;
    private final CosmoTeleport teleporter;


}