package kr.cosmoislands.cosmoislands.bukkit.settings;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import com.minepalm.arkarangutils.bukkit.BukkitExecutor;
import com.minepalm.arkarangutils.bukkit.ItemStackBuilder;
import com.minepalm.arkarangutils.bukkit.Pair;
import kr.cosmoisland.cosmoislands.api.settings.IslandSetting;
import kr.cosmoisland.cosmoislands.api.settings.IslandSettingsMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

//todo: setting 값에 맞는 WorldHandler 구현하기
public class SettingGUI extends ArkarangGUI {

    private static final ItemStack[] glass = new ItemStack[3];
    private static final ItemStack setting, weather, time, biome;

    static{
        glass[0] = new ItemStackBuilder(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        glass[1] = new ItemStackBuilder(new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        glass[2] = new ItemStackBuilder(new ItemStack(Material.CYAN_STAINED_GLASS_PANE, 1)).setName(" ").getHandle();
        setting = new ItemStackBuilder(new ItemStack(Material.WRITABLE_BOOK)).setName("§f§l[§7§l 섬 설정 §f§l]").getHandle();
        weather = new ItemStackBuilder(new ItemStack(Material.CAULDRON)).setName("§f§l[§3§l 섬 날씨 §f§l]").getHandle();
        time = new ItemStackBuilder(new ItemStack(Material.CLOCK)).setName("§f§l[§e§l 섬 시간 §f§l]").getHandle();
        biome = new ItemStackBuilder(new ItemStack(Material.GRASS)).setName("§f§l[§2§l 섬 바이옴 §f§l]").getHandle();
    }

    public SettingGUI(World world, IslandSettingsMap settings, Map<IslandSetting, String> view, BukkitExecutor executor) {
        super(3, "§f§l[§7§l 섬 관리 §f§l]");
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
            executor.async(() -> {
                ArkarangGUI gui = new Misc(world, settings, view, executor);
                executor.sync(()->gui.openGUI(player));
            });
        });
        funcs.put(12, event->{
            Player player = (Player) event.getWhoClicked();
            executor.async(() -> {
                ArkarangGUI gui = new Weather(world, settings, view, executor);
                executor.sync(()->gui.openGUI(player));
            });
        });
        funcs.put(14, event->{
            Player player = (Player) event.getWhoClicked();
            executor.async(() -> {
                ArkarangGUI gui = new Time(world, settings, view, executor);
                executor.sync(()->gui.openGUI(player));
            });
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

        final World world;
        final IslandSettingsMap settings;
        final Map<IslandSetting, String> view;
        final BukkitExecutor executor;

        public Misc( World world, 
                     IslandSettingsMap settings, 
                     Map<IslandSetting, String> view, 
                     BukkitExecutor executor ) {
            super(3, "§f§l[§7§l섬 설정 §f§l]");
            for(int i = 0 ; i < 27 ; i ++){
                inv.setItem(i, glass);
            }
            this.world = world;
            this.settings = settings;
            this.view = view;
            this.executor = executor;
            ItemStack pvpIcon, physicsIcon;
            boolean isPVP = Boolean.parseBoolean(view.get(IslandSetting.ALLOW_PVP));
            boolean isPhysics = Boolean.parseBoolean(view.get(IslandSetting.BUILDERS_UTILITY));
            ItemStackBuilder builder1, builder2;
            builder1 = new ItemStackBuilder(pvp.clone());
            builder2 = new ItemStackBuilder(physics.clone());
            pvpIcon = get(isPVP, builder1, "§c§lPVP 가 활성화 되어 있습니다.", "§a§lPVP 가 비활성화 되어 있습니다");
            physicsIcon = get(isPhysics, builder2, "§c§l건축 모드가 활성화 되어 있습니다.", "§a§l건축 모드가 비활성화 되어 있습니다");
            inv.setItem(11, pvpIcon);
            inv.setItem(15, physicsIcon);
            funcs.put(11, buildFunction(inv, settings, IslandSetting.ALLOW_PVP, new ItemStackBuilder(pvp.clone()), 11, new Pair<>("§c§lPVP 가 활성화 되어 있습니다.", "§a§lPVP 가 비활성화 되어 있습니다")));
            funcs.put(15, buildFunction(inv, settings, IslandSetting.BUILDERS_UTILITY, new ItemStackBuilder(physics.clone()), 15, new Pair<>("§c§l건축 모드가 활성화 되어 있습니다.", "§a§l건축 모드가 비활성화 되어 있습니다")));
        }

        Consumer<InventoryClickEvent> buildFunction(Inventory inv, IslandSettingsMap map, IslandSetting setting, ItemStackBuilder builder, int slot, Pair<String, String> text){
            return event -> {
                Player player = ((Player)event.getWhoClicked());
                boolean b = Boolean.parseBoolean(view.get(setting));
                b = !b;
                map.setSetting(setting, Boolean.toString(b));
                view.put(setting, Boolean.toString(b));
                inv.setItem(slot, get(b, builder, text.getKey(), text.getValue()));
                executor.sync(player::updateInventory);
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

        World world;
        IslandSettingsMap settings;
        Map<IslandSetting, String> view;
        BukkitExecutor executor;

        public Weather( World world,
                        IslandSettingsMap settings,
                        Map<IslandSetting, String> view,
                        BukkitExecutor executor ) {
            super(3, "§f§l[§3§l섬 날씨 §f§l]");

            this.world = world;
            this.settings = settings;
            this.view = view;
            this.executor = executor;

            for(int i = 0 ; i < 27 ; i ++){
                inv.setItem(i, glass);
            }

            boolean isWeather = Boolean.parseBoolean(view.get(IslandSetting.SUNNY));
            updateIcon(isWeather);

            funcs.put(11, event->{
                Player player = ((Player)event.getWhoClicked());
                boolean b = Boolean.parseBoolean(view.get(IslandSetting.SUNNY));
                if(!b) {
                    b = true;
                    settings.setSetting(IslandSetting.SUNNY, Boolean.toString(b));
                    updateIcon(true);
                    world.setStorm(false);
                    player.updateInventory();
                }
            });
            funcs.put(15, event->{
                Player player = ((Player)event.getWhoClicked());
                boolean b = Boolean.parseBoolean(view.get(IslandSetting.SUNNY));
                if(b) {
                    b = false;
                    settings.setSetting(IslandSetting.SUNNY, Boolean.toString(b));
                    updateIcon(false);
                    world.setStorm(true);
                    player.updateInventory();
                }

            });
        }

        private void updateIcon(boolean b){
            inv.setItem(11, get(b, new ItemStackBuilder(weather.clone()), "§a§l선택되었습니다.", "§e§l클릭 시 날씨가 맑아집니다."));
            inv.setItem(15, get(!b, new ItemStackBuilder(rainy.clone()), "§a§l선택되었습니다.", "§9§l클릭 시 비가 내립니다."));
        }

        @Override
        protected void onClose(InventoryCloseEvent event) {
            world = null;
            settings = null;
            view = null;
            executor = null;
        }

    }

    public static class Time extends ArkarangGUI{
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

        World world;
        IslandSettingsMap settings;
        Map<IslandSetting, String> view;
        BukkitExecutor executor;
        AtomicBoolean lock = new AtomicBoolean(false);

        public Time( World world,
                     IslandSettingsMap settings,
                     Map<IslandSetting, String> view,
                     BukkitExecutor executor ) {
            super(3, "§f§l[§7§l섬 설정 §f§l]");

            this.world = world;
            this.settings = settings;
            this.view = view;
            this.executor = executor;

            for(int i = 0; i < 27; i++){
                inv.setItem(i, glass);
            }

            updateIcons();

            funcs.put(9, event->{
                setTime((Player)event.getWhoClicked(), "DAWN", DAWN_TIME);
            });
            funcs.put(11, event->{
                setTime((Player)event.getWhoClicked(), "BREAKFAST", BREAKFAST_TIME);
            });
            funcs.put(13, event->{
                setTime((Player)event.getWhoClicked(), "NOON", NOON_TIME);
            });
            funcs.put(15, event->{
                setTime((Player)event.getWhoClicked(), "EVENING", EVENING_TIME);
            });
            funcs.put(17, event->{
                setTime((Player)event.getWhoClicked(), "NIGHT", NIGHT_TIME);
            });
        }

        private void updateIcons() {
            String val = view.get(IslandSetting.TIME);
            inv.setItem(9, get(val.equals("DAWN"), dawn, "§a§l선택되었습니다", "§e§l클릭 시 새벽이 됩니다"));
            inv.setItem(11, get(val.equals("BREAKFAST"), breakfast, "§a§l선택되었습니다", "§e§l클릭 시 아침이 됩니다"));
            inv.setItem(13, get(val.equals("NOON"), noon, "§a§l선택되었습니다", "§e§l클릭 시 낮이 됩니다"));
            inv.setItem(15, get(val.equals("EVENING"), evening, "§a§l선택되었습니다", "§e§l클릭 시 저녁이 됩니다"));
            inv.setItem(17, get(val.equals("NIGHT"), night, "§a§l선택되었습니다", "§e§l클릭 시 밤이 됩니다"));
        }

        private void setTime(Player player, String str, int time){
            if(!lock.get()) {
                executor.async(() -> {
                    lock.set(true);
                    settings.setSetting(IslandSetting.TIME, str);
                    view.put(IslandSetting.TIME, str);
                    updateIcons();
                    player.updateInventory();
                    executor.sync(() -> world.setTime(time));
                    lock.set(false);
                });
            }
        }

        @Override
        protected void onClose(InventoryCloseEvent event) {
            world = null;
            settings = null;
            view = null;
            executor = null;
        }
    }

    public static class Biome extends ArkarangGUI{

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

    static boolean switchSetting(IslandSettingsMap map, IslandSetting setting) throws ExecutionException, InterruptedException {
        boolean b = Boolean.parseBoolean(map.getSettingAsync(setting).get());
        b = !b;
        map.setSetting(setting, Boolean.toString(b)).get();
        return b;
    }
}
