package kr.cosmoislands.cosmoislands.bukkit.settings;

import com.minepalm.arkarangutils.bukkit.*;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettings;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import kr.cosmoisland.cosmoislands.bukkit.CosmoIslandsBukkitBootstrap;
import kr.cosmoisland.cosmoislands.bukkit.island.IslandLocal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class SettingGUI extends ArkarangGUI {

    private static final ItemStack[] glass = new ItemStack[3];
    private static final ItemStack setting, weather, time, biome;

    static{
        glass[0] = new ItemStackBuilder(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        glass[1] = new ItemStackBuilder(new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        glass[2] = new ItemStackBuilder(new ItemStack(Material.CYAN_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        setting = new ItemStackBuilder(new ItemStack(Material.WRITABLE_BOOK)).setName("§f§l[§7§l섬 설정 §f§l]").getHandle();
        weather = new ItemStackBuilder(new ItemStack(Material.CAULDRON)).setName("§f§l[§3§l섬 날씨 §f§l]").getHandle();
        time = new ItemStackBuilder(new ItemStack(Material.CLOCK)).setName("§f§l[§e§l섬 시간 §f§l]").getHandle();
        biome = new ItemStackBuilder(new ItemStack(Material.GRASS)).setName("§f§l[§2§l섬 바이옴 §f§l]").getHandle();
    }

    final Island island;
    final IslandSettingsMap settings;
    final BukkitExecutor executor;

    public SettingGUI(Island island, BukkitExecutor executor) {
        super(3, "§f§l[§7§l섬 관리 §f§l]");
        this.island = island;
        this.settings = island.getComponent(IslandSettingsMap.class);
        this.executor = executor;
        for(int i = 0; i < 3; i++){
            for(int j = 0 ; j < 9 ; j ++){
                inv.setItem(i*9+j, glass[i]);
            }
        }

        inv.setItem(10, setting);
        inv.setItem(12, weather);
        inv.setItem(14, time);

        funcs.put(10, event->{
            Player player = (Player) event.getWhoClicked();
            if(settings != null) {
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), () -> {
                    try {
                        ArkarangGUI gui = new Misc(settings);
                        Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), () -> gui.openGUI((Player) event.getWhoClicked()));
                    } catch (ExecutionException | InterruptedException e) {
                        player.closeInventory();
                    }
                });
            }else{
                player.closeInventory();
                player.sendMessage("데이터 로드 중 오류가 발생했습니다. 다시 시도 해주세요.");
            }
        });
        funcs.put(12, event->{
            Player player = (Player) event.getWhoClicked();
            if(settings != null) {
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                    try {
                        SimpleGUI gui = new Weather(island, settings);
                        Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()-> gui.openGUI((Player)event.getWhoClicked()));
                    } catch (ExecutionException | InterruptedException e) {
                        player.closeInventory();
                    }
                });
            }else{
                player.closeInventory();
                player.sendMessage("데이터 로드 중 오류가 발생했습니다. 다시 시도 해주세요.");
            }
        });
        funcs.put(14, event->{
            Player player = (Player) event.getWhoClicked();
            if(settings != null) {
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                    try {
                        SimpleGUI gui = new Time(island, settings);
                        Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()-> gui.openGUI((Player)event.getWhoClicked()));
                    } catch (ExecutionException | InterruptedException e) {
                        player.closeInventory();
                    }
                });
            }else{
                player.closeInventory();
                player.sendMessage("데이터 로드 중 오류가 발생했습니다. 다시 시도 해주세요.");
            }
        });
    }


    public static class Misc extends ArkarangGUI{

        private static final ItemStack pvp, physics, glass;

        static{
            pvp = new ItemStackBuilder(Material.DIAMOND_SWORD)
                    .setName("§c§lPVP 설정")
                    .getHandle();
            physics = new ItemStackBuilder(Material.WATER_BUCKET).getHandle();
            glass = new ItemStackBuilder(new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        }

        public Misc(IslandSettingsMap settings) throws ExecutionException, InterruptedException {
            super(3, "§f§l[§7§l섬 설정 §f§l]");
            for(int i = 0 ; i < 27 ; i ++){
                inv.setItem(i, glass);
            }
            ItemStack pvpIcon, physicsIcon;
            boolean isPVP = Boolean.parseBoolean(settings.getSettingAsync(IslandSettings.ALLOW_PVP).get());
            boolean isPhysics = Boolean.parseBoolean(settings.getSettingAsync(IslandSettings.BUILDERS_UTILITY).get());
            ItemStackBuilder builder1, builder2;
            builder1 = new ItemStackBuilder(pvp.clone());
            builder2 = new ItemStackBuilder(physics.clone());
            pvpIcon = get(isPVP, builder1, "§c§lPVP 가 활성화 되어 있습니다.", "§a§lPVP 가 비활성화 되어 있습니다");
            physicsIcon = get(isPhysics, builder2, "§c§l건축 모드가 활성화 되어 있습니다.", "§a§l건축 모드가 비활성화 되어 있습니다");
            inv.setItem(11, pvpIcon);
            inv.setItem(15, physicsIcon);
            funcs.put(11, buildFunction(inv, settings, IslandSettings.ALLOW_PVP, new ItemStackBuilder(pvp.clone()), 11, new Pair<>("§c§lPVP 가 활성화 되어 있습니다.", "§a§lPVP 가 비활성화 되어 있습니다")));
            funcs.put(15, buildFunction(inv, settings, IslandSettings.BUILDERS_UTILITY, new ItemStackBuilder(physics.clone()), 15, new Pair<>("§c§l건축 모드가 활성화 되어 있습니다.", "§a§l건축 모드가 비활성화 되어 있습니다")));
        }

        static Consumer<InventoryClickEvent> buildFunction(Inventory inv, IslandSettingsMap map, IslandSettings setting, ItemStackBuilder builder, int slot, Pair<String, String> text){
            return event -> {
                Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                    Player player = ((Player)event.getWhoClicked());
                    try {
                        boolean b = Boolean.parseBoolean(map.getSettingAsync(setting).get());
                        b = !b;
                        map.setSetting(setting, Boolean.toString(b)).get();
                        inv.setItem(slot, get(b, builder, text.getKey(), text.getValue()));
                        Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), player::updateInventory);
                    } catch (InterruptedException | ExecutionException e) {
                        player.closeInventory();
                        player.sendMessage("실행 중 오류가 발생했습니다. 다시 시도해주세요.");
                    }
                });
            };
        }
    }

    public static class Weather extends ArkarangGUI{

        private static final ItemStack glass, weather, rainy;

        static{
            glass = new ItemStackBuilder(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
            weather = new ItemStackBuilder(new ItemStack(Material.RED_WOOL, 1)).setName("§c§l맑음").getHandle();
            rainy = new ItemStackBuilder(new ItemStack(Material.BLUE_WOOL, 1)).setName("§7§l비").getHandle();
        }

        public Weather(IslandLocal island, IslandSettingsMap settings) throws ExecutionException, InterruptedException {
            super(3, "§f§l[§3§l섬 날씨 §f§l]");
            for(int i = 0 ; i < 27 ; i ++){
                inv.setItem(i, glass);
            }
            boolean isWeather = Boolean.parseBoolean(settings.getSettingAsync(IslandSettings.SUNNY).get());
            updateIcon(isWeather);
            funcs.put(11, event->{
                Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                    Player player = ((Player)event.getWhoClicked());
                    try {
                        boolean b = Boolean.parseBoolean(settings.getSettingAsync(IslandSettings.SUNNY).get());
                        if(!b) {
                            b = true;
                            settings.setSetting(IslandSettings.SUNNY, Boolean.toString(b)).get();
                            updateIcon(true);
                            island.getWorld().ifPresent(world->{
                                world.setStorm(false);
                            });
                            Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), player::updateInventory);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        player.closeInventory();
                        player.sendMessage("실행 중 오류가 발생했습니다. 다시 시도해주세요.");
                    }
                });
                return true;
            });
            funcs.put(15, event->{
                Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), ()->{
                    Player player = ((Player)event.getWhoClicked());
                    try {
                        boolean b = Boolean.parseBoolean(settings.getSettingAsync(IslandSettings.SUNNY).get());
                        if(b) {
                            b = false;
                            settings.setSetting(IslandSettings.SUNNY, Boolean.toString(b)).get();
                            updateIcon(false);
                            island.getWorld().ifPresent(world->{
                                world.setStorm(true);
                            });
                            Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), player::updateInventory);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        player.closeInventory();
                        player.sendMessage("실행 중 오류가 발생했습니다. 다시 시도해주세요.");
                    }
                });
                return true;
            });
        }

        private void updateIcon(boolean b){
            inv.setItem(11, get(b, new ItemStackBuilder(weather.clone()), "§a§l선택되었습니다.", "§e§l클릭 시 날씨가 맑아집니다."));
            inv.setItem(15, get(!b, new ItemStackBuilder(rainy.clone()), "§a§l선택되었습니다.", "§9§l클릭 시 비가 내립니다."));
        }

    }

    public static class Time extends SimpleGUI{
        private static final int DAWN_TIME = 0, BREAKFAST_TIME = 4000, NOON_TIME = 8000, EVENING_TIME = 12000, NIGHT_TIME = 20000;
        private static final ItemStackBuilder dawn, breakfast, noon, evening, night;
        private static final ItemStack glass;

        static{
            dawn = new ItemStackBuilder(Material.CLOCK).setName("§e§l새벽");
            breakfast = new ItemStackBuilder(Material.CLOCK).setName("§e§l아침");
            noon = new ItemStackBuilder(Material.CLOCK).setName("§e§l점심");
            evening = new ItemStackBuilder(Material.CLOCK).setName("§e§l저녁");
            night = new ItemStackBuilder(Material.CLOCK).setName("§e§l밤");
            glass = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1);
        }

        AtomicBoolean bool = new AtomicBoolean(false);

        public Time(IslandLocal island, IslandSettingsMap settings) throws ExecutionException, InterruptedException {
            super(3, "§f§l[§7§l섬 설정 §f§l]");
            for(int i = 0; i < 27; i++){
                inv.setItem(i, glass);
            }

            updateIcons(settings);
            funcs.put(9, event->{
                setTime((Player)event.getWhoClicked(), bool, island, settings, "DAWN", DAWN_TIME);
                return true;
            });
            funcs.put(11, event->{
                setTime((Player)event.getWhoClicked(), bool, island, settings, "BREAKFAST", BREAKFAST_TIME);
                return true;
            });
            funcs.put(13, event->{
                setTime((Player)event.getWhoClicked(), bool, island, settings, "NOON", NOON_TIME);
                return true;
            });
            funcs.put(15, event->{
                setTime((Player)event.getWhoClicked(), bool, island, settings, "EVENING", EVENING_TIME);
                return true;
            });
            funcs.put(17, event->{
                setTime((Player)event.getWhoClicked(), bool, island, settings, "NIGHT", NIGHT_TIME);
                return true;
            });
        }

        private void updateIcons(IslandSettingsMap settings) throws ExecutionException, InterruptedException {
            String val = settings.getSettingAsync(IslandSettings.TIME).get();
            inv.setItem(9, get(val.equals("DAWN"), dawn, "§a§l선택되었습니다", "§e§l클릭 시 새벽이 됩니다"));
            inv.setItem(11, get(val.equals("BREAKFAST"), breakfast, "§a§l선택되었습니다", "§e§l클릭 시 아침이 됩니다"));
            inv.setItem(13, get(val.equals("NOON"), noon, "§a§l선택되었습니다", "§e§l클릭 시 낮이 됩니다"));
            inv.setItem(15, get(val.equals("EVENING"), evening, "§a§l선택되었습니다", "§e§l클릭 시 저녁이 됩니다"));
            inv.setItem(17, get(val.equals("NIGHT"), night, "§a§l선택되었습니다", "§e§l클릭 시 밤이 됩니다"));
        }

        private void setTime(Player player, AtomicBoolean bool, IslandLocal island, IslandSettingsMap map, String str, int time){
            if(!bool.get()) {
                Bukkit.getScheduler().runTaskAsynchronously(CosmoIslandsBukkitBootstrap.getInst(), () -> {
                    bool.set(true);
                    map.setSetting(IslandSettings.TIME, str);
                    try {
                        updateIcons(map);
                        player.updateInventory();
                    } catch (ExecutionException | InterruptedException e) {

                    }
                    Bukkit.getScheduler().runTask(CosmoIslandsBukkitBootstrap.getInst(), () -> {
                        island.getWorld().ifPresent(world -> {
                            world.setTime(time);
                        });
                    });
                    bool.set(false);
                });
            }
        }
    }

    public static class Biome extends SimpleGUI{

        public Biome(IslandSettingsMap settings) {
            super(3, "§f§l[§7§l섬 설정 §f§l]");
        }
    }

    static ItemStack get(boolean b, ItemStackBuilder origin, String str1, String str2){
        ItemStack item = origin.getClone();
        ItemStackBuilder builder = new ItemStackBuilder(item);
        if(b){
            builder.addLine(str1);
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL,1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }else{
            builder.addLine(str2);
            item = builder.getHandle();
        }
        return item;
    }

    static boolean switchSetting(IslandSettingsMap map, IslandSettings setting) throws ExecutionException, InterruptedException {
        boolean b = Boolean.parseBoolean(map.getSettingAsync(setting).get());
        b = !b;
        map.setSetting(setting, Boolean.toString(b)).get();
        return b;
    }
}
