package tropical.client.features.waypoint;

import java.time.DayOfWeek;

import net.minecraft.world.phys.Vec3;
import tropical.client.TropicalUtils;
import tropical.client.TropicalUtils.Dimension;

public class Waypoint {
    public float x, y, z;
    public String name, owner;
    public TropicalUtils.Dimension dimension;
    public int id;
    public boolean enabled;
    public long timestamp;
    public int color;
    public Waypoint(String name, float x, float y, float z, String dimension, int id, String owner, int color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.dimension = TropicalUtils.Dimension.getValue(dimension);
        this.owner = owner;
        this.id = id;
        this.enabled = true;
        this.color = color;

        this.timestamp = System.currentTimeMillis();
    }

    public Waypoint(String name, float x, float y, float z, String dimension, int id, String owner,
                    boolean enabled, long timestamp, int color)
    {

        this(name, x, y, z, dimension, id, owner, color);
        this.enabled = enabled;
        this.timestamp = timestamp;
    }

    public Waypoint(String name, float x, float y, float z, Dimension dimension, int id, String owner,
                    boolean enabled, long timestamp, int color)
    {
        this(name, x, y, z, dimension.toString(), id, owner, color);
        this.enabled = enabled;
        this.timestamp = timestamp;
    }

    public Waypoint(Waypoint wp) {
        this(wp.name, wp.x, wp.y, wp.z, wp.dimension, wp.id, wp.owner, wp.enabled, wp.timestamp, wp.color);
    }

    public Vec3 asVec3() {
        return new Vec3((double)this.x, (double)this.y, (double)this.z);
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public Waypoint copy() {
        return new Waypoint(this); 
    }
}
