package com.github.xaxys.metro;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Position implements Serializable {
	
	Integer x;
	Integer y;
	Integer z;
	UUID world;
	
	public Position(int _x, int _y, int _z, UUID _world) {
		x = _x;
		y = _y;
		z = _z;
		world = _world;
	}
	
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
	
	public Location toLocation() {
		World w = Bukkit.getWorld(world);
		if (w == null) return null;
		return new Location(w, x.doubleValue(), y.doubleValue(), z.doubleValue());
	}
	
	@Override
	public String toString() {
		return String.format("Position{x=%d,y=%d,z=%d,world=%s}", x, y, z, world);
	}
	
}
