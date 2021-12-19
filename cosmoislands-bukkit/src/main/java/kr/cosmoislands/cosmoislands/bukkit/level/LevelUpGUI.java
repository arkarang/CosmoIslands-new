package kr.cosmoislands.cosmoislands.bukkit.level;

import com.minepalm.arkarangutils.bukkit.ArkarangGUI;
import kr.cosmoisland.cosmoislands.api.level.IslandLevel;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelUpGUI extends ArkarangGUI {

    IslandLevel level;
    Pattern pattern;

    public LevelUpGUI(IslandLevel level, Pattern pattern) {
        super(6, "섬 레벨업");
        this.level = level;
        this.pattern = pattern;
        for(int i = 0; i < 54; i++){
            cancelled.put(i, false);
        }
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        List<ItemStack> toReturn = new ArrayList<>();
        int totalToLevelUp = 0;
        for(int i = 0; i < 54; i++){
            ItemStack item = inv.getItem(i);
            if(item != null && item.getType() != Material.AIR){
                ItemMeta meta = item.getItemMeta();
                boolean isValidItem = false;
                for (String lore : meta.getLore()) {
                    Matcher matcher = pattern.matcher(lore);
                    isValidItem = regexCheck(matcher);
                    if(isValidItem){
                        int level = getProvidingLevel(matcher);
                        totalToLevelUp += level * item.getAmount();
                        item.setAmount(0);
                        item.setType(Material.AIR);
                        break;
                    }
                }
                if(!isValidItem){
                    toReturn.add(item);
                }
            }
        }
        if(totalToLevelUp > 0){
            final int total = totalToLevelUp;
            level.addLevel(total).thenRun(()->{
                event.getPlayer().sendMessage("섬 레벨이 "+total+"만큼 올랐습니다!");
            });
        }
        if(!toReturn.isEmpty()){
            event.getPlayer().getInventory().addItem(toReturn.toArray(new ItemStack[0]));
        }
    }

    private boolean regexCheck(Matcher matcher){
        return matcher.find();
    }

    private int getProvidingLevel(Matcher matcher){
        String value = matcher.group(0);
        try {
            return Integer.parseInt(value);
        }catch (IllegalArgumentException e){
            return 0;
        }
    }
}
