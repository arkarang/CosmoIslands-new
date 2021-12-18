package kr.cosmoisland.cosmoislands.bukkit.listeners;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.api.internship.IslandInternsMap;
import kr.cosmoisland.cosmoislands.api.IslandPlayer;
import kr.cosmoisland.cosmoislands.api.player.IslandPlayersMap;
import kr.cosmoisland.cosmoislands.api.player.MemberRank;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoisland.cosmoislands.api.protection.IslandPermissionsMap;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.bukkit.island.IslandLocal;
import kr.cosmoisland.cosmoislands.bukkit.utils.IslandUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Openable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

//참고
//https://github.com/tastybento/askyblock/blob/master/src/com/wasteofplastic/askyblock/listeners/IslandGuard.java
public class ProtectionListener implements org.bukkit.event.Listener{


}
