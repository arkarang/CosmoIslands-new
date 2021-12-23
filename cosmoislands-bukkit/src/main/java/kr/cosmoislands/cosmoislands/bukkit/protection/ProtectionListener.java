package kr.cosmoislands.cosmoislands.bukkit.protection;

import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.protection.IslandPermissions;
import kr.cosmoislands.cosmoislands.api.protection.IslandProtection;
import kr.cosmoislands.cosmoislands.api.settings.IslandSetting;
import kr.cosmoislands.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoislands.cosmoislands.api.warp.IslandWarpsMap;
import kr.cosmoislands.cosmoislands.api.world.IslandWorld;
import kr.cosmoislands.cosmoislands.bukkit.IslandPreconditions;
import kr.cosmoislands.cosmoislands.bukkit.utils.IslandUtils;
import lombok.RequiredArgsConstructor;
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
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Openable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//참고
//https://github.com/tastybento/askyblock/blob/master/src/com/wasteofplastic/askyblock/listeners/IslandGuard.java
@RequiredArgsConstructor
public class ProtectionListener implements Listener {

    private final BukkitExecutor executor;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final World world = event.getBlock().getWorld();

        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandProtection protection = island.getComponent(IslandProtection.class);
            IslandWorld islandWorld = island.getComponent(IslandWorld.class);
            boolean hasPermission = hasPermission(player, protection, IslandPermissions.BUILD);
            if(hasPermission) {
                if(isOutOfBorder(islandWorld, block)){
                    player.sendMessage("섬 바깥입니다.");
                    event.setCancelled(true);
                }
            }else {
                player.sendMessage("당신은 섬원이 아닙니다!");
                event.setCancelled(true);
            }
                
        }catch (IllegalArgumentException ignored){
            
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final World world = event.getBlock().getWorld();

        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandProtection protection = island.getComponent(IslandProtection.class);
            IslandWorld islandWorld = island.getComponent(IslandWorld.class);
            boolean hasPermission = hasPermission(player, protection, IslandPermissions.BUILD);

            if(hasPermission) {
                if(isOutOfBorder(islandWorld, block)){
                    player.sendMessage("섬 바깥입니다.");
                    event.setCancelled(true);
                }
            }else {
                player.sendMessage("당신은 섬원이 아닙니다!");
                event.setCancelled(true);
            }

        }catch (IllegalArgumentException ignored){

        }
    }

    ConcurrentHashMap<UUID, Boolean> teleportLock = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final World world = event.getPlayer().getWorld();

        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandWarpsMap warps = island.getComponent(IslandWarpsMap.class);

            if(isOutOfBorder(player)){
                teleportLock.put(player.getUniqueId(), true);
                    warps.getSpawnLocation().thenAccept(spawnLocation ->{
                        executor.sync(()->{
                            player.teleport(IslandUtils.convert(spawnLocation));
                            teleportLock.remove(player.getUniqueId());
                        });
                    });
            }
        }catch (IllegalArgumentException ignored){

        }
    }


    private boolean isOutOfBorder(Player player){
        Location loc = player.getLocation();
        return loc.getY() < 0 || loc.getX() > 512 || loc.getBlockZ() > 512;
    }

    private boolean isOutOfBorder(IslandWorld world, Block block) {
        return block.getX() < world.getMinX() ||
                block.getZ() < world.getMinZ() ||
                block.getX() > world.getMinX() + world.getWeight() ||
                block.getZ() > world.getMinZ() + world.getLength();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractAtEntityEvent event){
        final Player player = event.getPlayer();
        final Entity entity = event.getRightClicked();
        final World world = entity.getWorld();

        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandProtection protection = island.getComponent(IslandProtection.class);
            boolean hasPermission = hasPermission(player, protection, IslandPermissions.BUILD);
            if(!hasPermission) {
                event.setCancelled(true);
            }
        }catch (IllegalArgumentException ignored){

        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event){
        if(event.getPlayer() == null) {
            event.setCancelled(true);
            return;
        }

        final Player player = event.getPlayer();
        final World world = player.getWorld();

        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandProtection protection = island.getComponent(IslandProtection.class);
            boolean hasPermission = hasPermission(player, protection, IslandPermissions.BUILD);
            if(!hasPermission) {
                event.setCancelled(true);
            }
        }catch (IllegalArgumentException ignored){

        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFrameDamaged(EntityDamageByEntityEvent event){
        final Entity entity = event.getEntity();
        Entity damager = event.getDamager();

        if(event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)){
            Projectile proj = (Projectile) damager;
            if(proj.getShooter() instanceof Player){
                damager = (Player)proj.getShooter();
            }
        }

        if(!(damager instanceof Player)){
            return;
        }

        if(entity instanceof ItemFrame) {
            final Player player = (Player)damager;
            final World world = player.getWorld();
            try {
                IslandPreconditions preconditions = IslandPreconditions.of(world);
                Island island = preconditions.getIsland();
                IslandProtection protection = island.getComponent(IslandProtection.class);
                boolean hasPermission = hasPermission(player, protection, IslandPermissions.BUILD);
                if(!hasPermission) {
                    event.setCancelled(true);
                }
            }catch (IllegalArgumentException ignored){

            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingDestroy(HangingBreakByEntityEvent event){
        Entity entity = event.getRemover();
        final World world = entity.getWorld();

        if (entity instanceof Projectile) {
            Projectile proj = (Projectile) entity;
            if (proj.getShooter() instanceof Player) {
                entity = (Player) proj.getShooter();
            }
        }

        if(entity instanceof Player){
            final Player player = (Player) entity;

            try {
                IslandPreconditions preconditions = IslandPreconditions.of(world);
                Island island = preconditions.getIsland();
                IslandProtection protection = island.getComponent(IslandProtection.class);
                boolean hasPermission = hasPermission(player, protection, IslandPermissions.BUILD);
                if(!hasPermission) {
                    event.setCancelled(true);
                }
            }catch (IllegalArgumentException ignored){

            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingDestroy(HangingBreakEvent event){
        if(!(event instanceof HangingBreakByEntityEvent))
            event.setCancelled(buildersUtilities(event.getEntity().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event){
        Entity entity = event.getEntity();
        final World world = entity.getWorld();
        final Player player = (Player) entity;

        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandProtection protection = island.getComponent(IslandProtection.class);
            boolean hasPermission = hasPermission(player, protection, IslandPermissions.BUILD);
            if(!hasPermission) {
                event.setCancelled(true);
            }
        }catch (IllegalArgumentException ignored){

        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPVP(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if(event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)){
            Projectile proj = (Projectile) damager;
            if(proj.getShooter() instanceof Player){
                damager = (Player)proj.getShooter();
            }
        }
        if(entity instanceof Player && damager instanceof Player) {
            final World world = event.getDamager().getWorld();
            try {
                IslandPreconditions preconditions = IslandPreconditions.of(world);
                Island island = preconditions.getIsland();
                IslandSettingsMap settings = island.getComponent(IslandSettingsMap.class);
                String value = settings.getSetting(IslandSetting.ALLOW_PVP);
                boolean canPvp = Boolean.parseBoolean(value);
                if(!canPvp) {
                    event.setCancelled(true);
                }
            }catch (IllegalArgumentException ignored){

            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeafDecay(LeavesDecayEvent event) {
        if (getIslandID(event.getBlock().getLocation()) != Island.NIL_ID) {
            event.setCancelled(true);
        }
    }

    /**
     * 눈 생기고 옵시디언 생기고 얼음 생기고 옵시디언 코블스톤 생기고 콘크리트 생기는 이벤트
     * @param event 이벤트
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event){
        event.setCancelled(buildersUtilities(event.getBlock().getLocation()));
    }

    /**
     * 눈 사라지고 얼음 녹고, 불 꺼지는 이벤트
     * @param event 이벤트
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event){
        event.setCancelled(buildersUtilities(event.getBlock().getLocation()));
    }

    /**
     * 물, 용암 움직이는 이벤트
     * @param event 이벤트
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFade(BlockFromToEvent event){
        event.setCancelled(buildersUtilities(event.getBlock().getLocation()));
    }

    /**
     * 버섯, 불 번지는 이벤트
     * @param event 이벤트
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockSpreadEvent event){
        event.setCancelled(buildersUtilities(event.getBlock().getLocation()));
    }


    private boolean buildersUtilities(Location loc){
        final World world = loc.getWorld();
        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandSettingsMap settings = island.getComponent(IslandSettingsMap.class);
            String value = settings.getSetting(IslandSetting.BUILDERS_UTILITY);
            boolean enabled = Boolean.parseBoolean(value);
            if(enabled) {
                return true;
            }
        }catch (IllegalArgumentException ignored){

        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event) {
        if(EquipmentSlot.OFF_HAND == event.getHand())
            return;

        Action act = event.getAction();
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        if(block == null){
            return;
        }
        final World world = block.getWorld();
        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandProtection protection = island.getComponent(IslandProtection.class);
            if(act.equals(Action.PHYSICAL) || act.equals(Action.RIGHT_CLICK_BLOCK)) {
                switch (block.getType()) {
                    case CHEST:
                    case ENDER_CHEST:
                    case TRAPPED_CHEST:
                    case HOPPER:
                    case DISPENSER:
                    case DROPPER:
                    case FURNACE:
                    case BLAST_FURNACE:
                    case ANVIL:
                    case ENCHANTING_TABLE:
                    case JUKEBOX:
                        event.setCancelled(!hasPermission(player, protection, IslandPermissions.USE_CHEST));
                        // WOODEN PERMISSIONS
                    case LEVER:
                    case ACACIA_BUTTON:
                    case BIRCH_BUTTON:
                    case CRIMSON_BUTTON:
                    case DARK_OAK_BUTTON:
                    case JUNGLE_BUTTON:
                    case OAK_BUTTON:
                    case SPRUCE_BUTTON:
                    case WARPED_BUTTON:
                    case POLISHED_BLACKSTONE_BUTTON:
                    case STONE_BUTTON:
                        event.setCancelled(!hasPermission(player, protection, IslandPermissions.REDSTONE_INTERACTION));
                        break;
                    case ACACIA_PRESSURE_PLATE:
                    case BIRCH_PRESSURE_PLATE:
                    case CRIMSON_PRESSURE_PLATE:
                    case DARK_OAK_PRESSURE_PLATE:
                    case JUNGLE_PRESSURE_PLATE:
                    case OAK_PRESSURE_PLATE:
                    case SPRUCE_PRESSURE_PLATE:
                    case WARPED_PRESSURE_PLATE:
                    case DARK_OAK_DOOR:
                    case ACACIA_DOOR:
                    case BIRCH_DOOR:
                    case JUNGLE_DOOR:
                    case SPRUCE_DOOR:
                    case CRIMSON_DOOR:
                    case OAK_DOOR:
                    case WARPED_DOOR:
                    case ACACIA_TRAPDOOR:
                    case BIRCH_TRAPDOOR:
                    case CRIMSON_TRAPDOOR:
                    case DARK_OAK_TRAPDOOR:
                    case JUNGLE_TRAPDOOR:
                    case OAK_TRAPDOOR:
                    case SPRUCE_TRAPDOOR:
                    case WARPED_TRAPDOOR:
                        event.setCancelled(!hasPermission(player, protection, IslandPermissions.WOOD_DOOR));
                        break;
                    // IRON PERMISSIONS
                    case STONE_PRESSURE_PLATE:
                    case HEAVY_WEIGHTED_PRESSURE_PLATE:
                    case LIGHT_WEIGHTED_PRESSURE_PLATE:
                    case POLISHED_BLACKSTONE_PRESSURE_PLATE:
                        event.setCancelled(!hasPermission(player, protection, IslandPermissions.IRON_DOOR));
                        break;
                    case IRON_TRAPDOOR:
                    case IRON_DOOR:
                        boolean permit = hasPermission(player, protection, IslandPermissions.IRON_DOOR);
                        if(permit) {
                            if(event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                                BlockData data = block.getBlockData();
                                Openable openable = (Openable)data;
                                openable.setOpen(!openable.isOpen());
                                block.setBlockData(data);
                            }
                        }else{
                            event.setCancelled(true);
                        }
                        break;
                }
            }
        }catch (IllegalArgumentException ignored){

        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFarmland(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        if(block != null && getIslandID(block.getLocation()) != Island.NIL_ID) {
            if (block.getType().equals(Material.FARMLAND)) {
                if (event.getAction().equals(Action.PHYSICAL)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent event){
        boolean shouldCancel = buildersUtilities(event.getSourceBlock().getLocation());
        event.setCancelled(shouldCancel);
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFillEvent(PlayerBucketFillEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlockClicked();
        final World world = event.getBlock().getWorld();

        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandProtection protection = island.getComponent(IslandProtection.class);
            IslandWorld islandWorld = island.getComponent(IslandWorld.class);
            boolean hasPermission = hasPermission(player, protection, IslandPermissions.BUILD);

            if(hasPermission) {
                if(isOutOfBorder(islandWorld, block)){
                    player.sendMessage("섬 바깥입니다.");
                    event.setCancelled(true);
                }
            }else {
                event.setCancelled(true);
            }

        }catch (IllegalArgumentException ignored){

        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlockClicked();
        final World world = event.getBlock().getWorld();

        try {
            IslandPreconditions preconditions = IslandPreconditions.of(world);
            Island island = preconditions.getIsland();
            IslandProtection protection = island.getComponent(IslandProtection.class);
            IslandWorld islandWorld = island.getComponent(IslandWorld.class);
            boolean hasPermission = hasPermission(player, protection, IslandPermissions.BUILD);

            if(hasPermission) {
                if(isOutOfBorder(islandWorld, block)){
                    player.sendMessage("섬 바깥입니다.");
                    event.setCancelled(true);
                }
            }else {
                event.setCancelled(true);
            }

        }catch (IllegalArgumentException ignored){

        }
    }

    private int getIslandID(Location loc){
        return IslandUtils.getIslandID(loc.getWorld().getName());
    }

    private boolean hasPermission(Player player, IslandProtection protection, IslandPermissions perm){
        if(player.hasPermission("cosmoislands.admin"))
            return true;
        return protection.hasPermission(player.getUniqueId(), perm);
    }

}
