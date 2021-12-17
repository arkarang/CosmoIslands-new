package kr.cosmoisland.cosmoislands.bukkit.listeners;

import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettings;
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

    ConcurrentHashMap<UUID, Boolean> cache = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Optional<IslandLocal> optional = Optional.ofNullable(getLocalIsland(event.getBlock().getWorld()));
        if(optional.isPresent()){
            Player player = event.getPlayer();
            IslandLocal local = optional.get();
            boolean isIslandPlayer = isLegalPlayer(player, local, IslandPermissions.BUILD);
            if(!isIslandPlayer) {
                player.sendMessage("당신은 섬원이 아닙니다!");
                event.setCancelled(true);
            }else{
                try {
                    if(isOutOfBorder(local, event.getBlock())){
                        player.sendMessage("섬 바깥입니다.");
                        event.setCancelled(true);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    player.sendMessage("문제가 발생해서 블럭을 부술수 없어요. 관리자에게 문의해주세요");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

            Optional<IslandLocal> optional = Optional.ofNullable(getLocalIsland(event.getBlock().getWorld()));
            if(optional.isPresent()){
                Player player = event.getPlayer();
                IslandLocal local = optional.get();
                boolean isIslandPlayer = isLegalPlayer(player, local, IslandPermissions.BUILD);
                if(!isIslandPlayer) {
                    player.sendMessage("당신은 섬원이 아닙니다!");
                    event.setCancelled(true);
                }else{
                    try {
                        if(isOutOfBorder(local, event.getBlock())){
                            player.sendMessage("섬 바깥입니다.");
                            event.setCancelled(true);
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        player.sendMessage("문제가 발생해서 블럭을 설치할 수 없어요. 관리자에게 문의해주세요");
                        event.setCancelled(true);
                    }
                }
            }

    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) throws ExecutionException, InterruptedException {

            Player player = event.getPlayer();
            int id = IslandUtils.getIslandID(event.getTo().getWorld().getName());
            if (id != Island.NIL_ID) {
                if(isOutOfBorder(event.getPlayer())){
                    if(!cache.containsKey(player.getUniqueId())) {
                        cache.put(player.getUniqueId(), true);
                        Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), () -> {
                            try {
                                final AbstractLocation al = getLocalIsland(player.getWorld()).getData().get().getSpawnLocation().get();
                                Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), () -> player.teleport(IslandUtils.convert(player.getWorld(), al)));
                            } catch (InterruptedException | ExecutionException ignored) {

                            }
                            cache.remove(player.getUniqueId());
                        });
                    }
                }
            }

    }


    private boolean isOutOfBorder(Player player){
        Location loc = player.getLocation();
        return loc.getY() < 0 || loc.getX() > 512 || loc.getBlockZ() > 512;
    }

    private boolean isOutOfBorder(IslandLocal local, Block block) throws ExecutionException, InterruptedException {
        IslandData data = local.getData().get();
        return block.getX() < data.getMinX().get() || block.getZ() < data.getMinZ().get() || block.getX() > data.getMinX().get() + data.getWeight().get() || block.getZ() > data.getMinZ().get() + data.getLength().get();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractAtEntityEvent event){
        Entity entity = event.getRightClicked();
        Optional.ofNullable(getLocalIsland(entity.getWorld())).ifPresent(local -> {
            if(entity instanceof ItemFrame){
                event.setCancelled(!isLegalPlayer(event.getPlayer(), local, IslandPermissions.BUILD));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event){
        Optional.ofNullable(getLocalIsland(event.getBlock().getWorld())).ifPresent(local -> {
            if(event.getPlayer() == null){
                event.setCancelled(true);
            }else
                event.setCancelled(!isLegalPlayer(event.getPlayer(), local, IslandPermissions.BUILD));
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if(event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)){
            Projectile proj = (Projectile) damager;
            if(proj.getShooter() instanceof Player){
                damager = (Player)proj.getShooter();
            }
        }
        if(entity instanceof ItemFrame && damager instanceof Player) {
            Player player = (Player)damager;
            Optional.ofNullable(getLocalIsland(entity.getWorld())).ifPresent(local -> {
                event.setCancelled(!isLegalPlayer(player, local, IslandPermissions.BUILD));
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingDestroy(HangingBreakByEntityEvent event){
        Optional.ofNullable(getLocalIsland(event.getRemover().getWorld())).ifPresent(local -> {
            Entity entity = event.getRemover();
            if (entity instanceof Projectile) {
                Projectile proj = (Projectile) entity;
                if (proj.getShooter() instanceof Player) {
                    entity = (Player) proj.getShooter();
                }
            }
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (!isLegalPlayer(player, local, IslandPermissions.BUILD)){
                    event.setCancelled(true);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingDestroy(HangingBreakEvent event){
        if(!(event instanceof HangingBreakByEntityEvent))
            event.setCancelled(buildersUtilities(event.getEntity().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event){
        Optional.ofNullable(getLocalIsland(event.getEntity().getWorld())).ifPresent(local -> {
            event.setCancelled(!isLegalPlayer(event.getPlayer(), local, IslandPermissions.BUILD));
        });
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
            Optional.ofNullable(getLocalIsland(event.getEntity().getWorld())).ifPresent(local -> {
                try {
                    String value = local.getSettings().get().getSetting(IslandSettings.ALLOW_PVP).get();
                    boolean bool = Boolean.parseBoolean(value);
                    event.setCancelled(!bool);
                } catch (ExecutionException | InterruptedException ignored) {

                }
            });
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
        Optional<IslandLocal> optional = Optional.ofNullable(getLocalIsland(loc.getWorld()));
        if(optional.isPresent()){
            try {
                IslandLocal local = optional.get();
                IslandSettingsMap map = local.getSettings().get();
                String val = map.getSettingAsync(IslandSettings.BUILDERS_UTILITY).get();
                return Boolean.parseBoolean(val);

            } catch (InterruptedException | ExecutionException ignored) {

            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event) {
        if(EquipmentSlot.OFF_HAND == event.getHand())
            return;

        Action act = event.getAction();
        if(!event.isCancelled()){
            Optional.ofNullable(getLocalIsland(event.getClickedBlock().getWorld())).ifPresent(local -> {
                if(act.equals(Action.PHYSICAL) || act.equals(Action.RIGHT_CLICK_BLOCK)) {
                    Block block = event.getClickedBlock();
                    if(block == null)
                        return;
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
                            event.setCancelled(!isLegalPlayer(event.getPlayer(), local, IslandPermissions.USE_CHEST));
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
                            event.setCancelled(!isLegalPlayer(event.getPlayer(), local, IslandPermissions.REDSTONE_INTERACTION));
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
                            event.setCancelled(!isLegalPlayer(event.getPlayer(), local, IslandPermissions.WOOD_DOOR));
                            break;
                        // IRON PERMISSIONS
                        case STONE_PRESSURE_PLATE:
                        case HEAVY_WEIGHTED_PRESSURE_PLATE:
                        case LIGHT_WEIGHTED_PRESSURE_PLATE:
                        case POLISHED_BLACKSTONE_PRESSURE_PLATE:
                            event.setCancelled(!isLegalPlayer(event.getPlayer(), local, IslandPermissions.IRON_DOOR));
                            break;
                        case IRON_TRAPDOOR:
                        case IRON_DOOR:
                            boolean permit = isLegalPlayer(event.getPlayer(), local, IslandPermissions.IRON_DOOR);
                            if(permit) {
                                if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
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
            });
        }
    }

    @EventHandler
    public void onFarmland(PlayerInteractEvent event){
        if(!event.isCancelled()) {
            Block block = event.getClickedBlock();
            if(block != null && getIslandID(block.getLocation()) != Island.NIL_ID) {
                if (block.getType().equals(Material.FARMLAND)) {
                    if (event.getAction().equals(Action.PHYSICAL)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPhysics(BlockPhysicsEvent event){
        if(!event.isCancelled()) {
            Optional.ofNullable(getLocalIsland(event.getSourceBlock().getWorld())).ifPresent(local -> {
                /*
                if (event.getBlock().getLocation().add(0, -1, 0).getBlock().getType().name().toLowerCase().contains("grass_block")){
                    if (event.getBlock().getLocation().getBlockY()>0) {
                        if (event.getSourceBlock().getType().equals(Material.AIR) || event.getChangedType().equals(Material.AIR)){
                            return;
                        }
                        if (event.getSourceBlock().getType().name().toLowerCase().contains("snow")){
                            return;
                        }
                    }
                }
                try {
                    if (event.getChangedType().name().toLowerCase().contains("chest") ||
                            event.getChangedType().name().toLowerCase().contains("stair") ||
                            event.getChangedType().name().toLowerCase().contains("fence") ||
                            event.getChangedType().name().toLowerCase().contains("pane") ||
                            event.getChangedType().name().toLowerCase().contains("wall") ||
                            event.getChangedType().name().toLowerCase().contains("bar")) {
                        return;
                    }
                } catch (Exception e) {
                    return;
                }
                 */
                try {
                    IslandSettingsMap map = local.getSettings().get();
                    String val = map.getSettingAsync(IslandSettings.BUILDERS_UTILITY).get();
                    boolean bool = Boolean.parseBoolean(val);
                    event.setCancelled(bool);
                } catch (InterruptedException | ExecutionException ignored) {

                }
            });
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFillEvent(PlayerBucketFillEvent event) {
        if(!event.isCancelled()){
            Optional.ofNullable(getLocalIsland(event.getBlockClicked().getWorld())).ifPresent(local -> {
                event.setCancelled(!isLegalPlayer(event.getPlayer(), local, IslandPermissions.BUILD));
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        if(!event.isCancelled()){
            Optional.ofNullable(getLocalIsland(event.getBlockClicked().getWorld())).ifPresent(local -> {
                event.setCancelled(!isLegalPlayer(event.getPlayer(), local, IslandPermissions.BUILD));
            });
        }
    }

    public boolean isPrevent(Player player, Location loc) {
        int id = getIslandID(loc);
        try {
            return getIslandIntern(player).getIslandId() != id && getIslandPlayer(player).getIslandId() != id;
        }catch (ExecutionException e) {
            return true;
        }
    }

    private int getIslandID(Location loc){
        return IslandUtils.getIslandID(loc.getWorld().getName());
    }

    private boolean isLegalPlayer(Player player, IslandLocal local, IslandPermissions perm){
        if(CosmoIslandsBukkitBootstrap.getInst().getAdmins().containsKey(player.getUniqueId()))
            return true;
        try {
            IslandPermissionsMap permsMap = local.getPermissionsMap().get();
            IslandPlayersMap playersMap = local.getPlayersMap().get();
            MemberRank rank = playersMap.getRank(getIslandPlayer(player)).get();
            if(permsMap.hasPermission(perm, rank).get()){
                return true;
            }else{
                IslandInternsMap map = local.getInternsMap().get();
                rank = map.isIntern(player.getUniqueId()).get() ? MemberRank.INTERN : MemberRank.NONE;
                return permsMap.hasPermission(perm, rank).get();
            }
        } catch (InterruptedException | ExecutionException e) {

        }
        return false;
    }

    private IslandLocal getLocalIsland(World world){
        return CosmoIslandsBukkitBootstrap.getInst().getCosmoIslands().getIslandLocal(IslandUtils.getIslandID(world.getName()));
    }

    private IslandPlayer getIslandPlayer(Player player) throws ExecutionException {
        return CosmoIslandsBukkitBootstrap.getInst().getIslandPlayer(player);
    }

    private IslandPlayer getIslandIntern(Player player) throws ExecutionException {
        return CosmoIslandsBukkitBootstrap.getInst().getIslandPlayer(player);
    }
}
