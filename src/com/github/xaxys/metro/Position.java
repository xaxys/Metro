package com.github.xaxys.metro;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Position implements Serializable {

	// v1.1.2
	private static final long serialVersionUID = 455651403437390838L;

	Integer x;
	Integer y;
	Integer z;
	UUID world;

	public Position(Location loc) {
		x = loc.getBlockX();
		y = loc.getBlockY();
		z = loc.getBlockZ();
		world = loc.getWorld().getUID();
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object b) {
		if (b instanceof Position) {
			Position p = (Position)b;
			return x.equals(p.x) && y.equals(p.y) && z.equals(p.z) && world.equals(p.world);
		} else return false;
	}

	@Override
	public String toString() {
		return String.format("Position{x=%d,y=%d,z=%d,world=%s}", x, y, z, world);
	}

	public Location toLocation() {
		World w = Bukkit.getWorld(world);
		return w == null ? null : new Location(w, x.doubleValue(), y.doubleValue(), z.doubleValue());
	}

}
