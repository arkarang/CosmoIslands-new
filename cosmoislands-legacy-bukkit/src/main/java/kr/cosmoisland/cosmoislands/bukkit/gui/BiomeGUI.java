package kr.cosmoisland.cosmoislands.bukkit.gui;

import com.minepalm.arkarangutils.bukkit.SimpleGUI;
import kr.cosmoisland.cosmoislands.bukkit.island.IslandLocal;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@Deprecated
public class BiomeGUI extends SimpleGUI {

    private static final ItemStack glass;
    private static final HashMap<Biome, ItemStack> icons = new HashMap<>();
    private static final HashMap<Integer, Biome> slots = new HashMap<>();

    static{
        glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
    }

    public BiomeGUI(IslandLocal island) {
        super(3, "바이옴 GUI");
        for(int i = 0 ; i < 27 ; i++) {
            inv.setItem(i, glass);
            if(slots.containsKey(i)){
                Biome biome = slots.get(i);
                inv.setItem(i, icons.get(biome));
                funcs.put(i, event->{
                    setBiome(island, biome);
                    return true;
                });
            }
        }
    }

    private void setBiome(IslandLocal island, Biome biome){

    }

}
