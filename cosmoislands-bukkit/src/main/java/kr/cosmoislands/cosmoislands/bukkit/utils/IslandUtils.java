package kr.cosmoislands.cosmoislands.bukkit.utils;

import kr.cosmoislands.cosmoislands.api.AbstractLocation;
import kr.cosmoislands.cosmoislands.api.Island;
import kr.cosmoislands.cosmoislands.api.warp.IslandLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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

}
