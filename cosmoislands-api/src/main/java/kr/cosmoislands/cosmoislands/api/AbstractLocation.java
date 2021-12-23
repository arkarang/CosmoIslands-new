package kr.cosmoislands.cosmoislands.api;


import lombok.Getter;
@Getter
public class AbstractLocation {

    double x, y, z;
    float pitch = 0, yaw = 0;

    public AbstractLocation(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public AbstractLocation(double x, double y, double z, float yaw, float pitch){
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public AbstractLocation(AbstractLocation loc){
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.pitch = loc.getPitch();
        this.yaw = loc.getYaw();
    }

    public int getBlockX(){
        return (int)x;
    }

    public int getBlockY(){
        return (int)y;
    }

    public int getBlockZ(){
        return (int)z;
    }
}
