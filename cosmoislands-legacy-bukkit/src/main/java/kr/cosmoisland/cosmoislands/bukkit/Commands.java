package kr.cosmoisland.cosmoislands.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.bukkit.Pair;
import com.minepalm.arkarangutils.invitation.InvitationService;
import com.minepalm.helloplayer.core.HelloPlayer;
import com.minepalm.helloplayer.core.HelloPlayers;
import com.minepalm.helloteleport.LocationData;
import com.minepalm.helloteleport.Teleporter;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.IslandComponent;
import kr.cosmoisland.cosmoislands.api.bank.IslandBank;
import kr.cosmoisland.cosmoislands.api.internship.IslandIntern;
import kr.cosmoisland.cosmoislands.api.internship.IslandInternsMap;
import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.warp.IslandLocation;
import kr.cosmoisland.cosmoislands.api.warp.IslandUserWarp;
import kr.cosmoisland.cosmoislands.bukkit.database.IslandDataLoader;
import kr.cosmoisland.cosmoislands.bukkit.database.PlayersMapLoader;
import kr.cosmoisland.cosmoislands.bukkit.events.IslandCreateEvent;
import kr.cosmoisland.cosmoislands.bukkit.events.IslandLoadedEvent;
import kr.cosmoisland.cosmoislands.bukkit.gui.*;
import kr.cosmoisland.cosmoislands.bukkit.gui.components.ConfirmCompounds;
import kr.cosmoisland.cosmoislands.bukkit.gui.components.RankingComponents;
import kr.cosmoisland.cosmoislands.bukkit.island.IslandLocal;
import kr.cosmoisland.cosmoislands.bukkit.island.IslandVoter;
import kr.cosmoisland.cosmoislands.bukkit.utils.IslandUtils;
import kr.cosmoisland.cosmoislands.core.*;
import kr.cosmoisland.cosmoislands.core.packet.IslandCreatePacket;
import kr.cosmoisland.cosmoislands.core.packet.IslandStatusChangePacket;
import kr.cosmoislands.cosmochat.core.api.ChatChannel;
import kr.cosmoislands.cosmochat.core.api.ChatPlayer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@CommandAlias("섬")
@RequiredArgsConstructor
public class Commands extends BaseCommand {

    private final BukkitExecutor executor;
    private final HelloPlayers players;
    private final InvitationService memberInvitation, internInvitation;
    private final CosmoTeleport teleporter;


}