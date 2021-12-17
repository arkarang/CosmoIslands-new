package kr.cosmoislands.cosmoislands.bukkit.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minepalm.arkarangutils.bukkit.InventorySerializer;
import com.minepalm.helloteleport.LocationData;
import kr.cosmoisland.cosmoislands.api.AbstractLocation;
import kr.cosmoisland.cosmoislands.api.Island;
import kr.cosmoisland.cosmoislands.api.warp.IslandLocation;
import kr.cosmoisland.cosmoislands.core.utils.jackson.JacksonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IslandUtils {

    private IslandUtils(){}

    public static int getIslandID(String str){
        try {
            return Integer.parseInt(str.substring(7));
        }catch (NumberFormatException | IndexOutOfBoundsException e){
            return Island.NIL_ID;
        }
    }

    public static Location convert(IslandLocation location) throws IllegalArgumentException{
        World world = Bukkit.getWorld("island_"+location.getIslandID());
        if(world != null){
            return new Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        }else
            throw new IllegalArgumentException("cannot find island id: "+location.getIslandID());
    }

    public static Location convert(World world, AbstractLocation loc) throws IllegalArgumentException{
        return new Location(world, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public static LocationData convertTeleport(IslandLocation location){
        return new LocationData("island_"+location.getIslandID(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }


    public static IslandLocation convert(Location location) throws IllegalArgumentException{
        int i = getIslandID(location);
        if(i != Island.NIL_ID)
            return new IslandLocation(i, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        else
            throw new IllegalArgumentException("Island not exist");
    }

    public static int getIslandID(Location loc){
        return getIslandID(loc.getWorld().getName());
    }

    public static AbstractLocation convertBukkit(Location loc){
        return new AbstractLocation(loc.getBlockX()+0.5, loc.getY(), loc.getBlockZ()+0.5, loc.getYaw(), loc.getPitch());
    }

    public static LocationData convertLocation(String world, AbstractLocation loc){
        return new LocationData(world, loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
    }
}
