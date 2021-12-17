package kr.cosmoisland.cosmoislands.bukkit.config;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class ReinforceSetting {

    final String name;
    @Getter
    final int maxLevel;
    private HashMap<Integer, Component> map = new HashMap<>();

    public ReinforceSetting(String name, int maxLevel, List<Component> list){
        this.name = name;
        this.maxLevel = maxLevel;
        list.forEach(comp-> map.put(comp.getLevel(), comp));
    }

    public void initIcons(HeadDatabaseAPI api){
        for (Component value : map.values()) {
            if(!value.head.equals("null")) {
                ItemStack item = api.getItemHead(value.head);
                if(item != null)
                    value.setItem(item);
            }
        }
    }

    public Component getComponent(int i){
        return map.get(i);
    }

    @Data
    @RequiredArgsConstructor
    public static class Component{
        final String head;
        final int level;
        final int value;
        final double cost;
        ItemStack item;
    }
}
