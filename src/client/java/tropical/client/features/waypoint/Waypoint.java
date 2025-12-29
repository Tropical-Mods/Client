package tropical.client.features.waypoint;

import net.minecraft.world.phys.Vec3;

public class Waypoint {
    public float x, y, z;
    public String name, dimension, owner;
    public int id;
    public boolean enabled;
    public long timestamp;
    public Waypoint(String name, float x, float y, float z, String dimension, int id, String owner) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.dimension = dimension;
        this.owner = owner;
        this.id = id;
        this.enabled = true;

        this.timestamp = System.currentTimeMillis();
    }

    public Waypoint(String name, float x, float y, float z, String dimension, int id, String owner,
                    boolean enabled, long timestamp)
    {
        this(name, x, y, z, dimension, id, owner);
        this.enabled = enabled;
        this.timestamp = timestamp;
    }

    public Waypoint(Waypoint wp) {
        this(wp.name, wp.x, wp.y, wp.z, wp.dimension, wp.id, wp.owner, wp.enabled, wp.timestamp);
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
