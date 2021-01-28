package com.github.xaxys.metro;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;

public class MetroStation implements Serializable {
	
	public enum Direction {
		NORTH,
		SOUTH,
		EAST,
		WEST,
	}
	
	public String LineName;
	public String Name;
	public Direction IncDire;
	public Position Pos;
	
	public transient MetroLine Line;
	public transient MetroStation Dest;
	
	public void complete(MetroLine line) {
		Line = line;
		Dest = this;
	}
	
	@Override
	public boolean equals(Object b) {
		if (b instanceof MetroStation) {
			MetroStation s = (MetroStation)b;
			return LineName.equals(s.LineName) && Name.equals(s.Name)
					&& IncDire.equals(s.IncDire) && Pos.equals(s.Pos);
		} else return false;
	}
	
	@Override
	public String toString() {
		return String.format("MetroStation{LineName=%s, Name=%s, Dire=%s, Dest=%s}",
				LineName, Name, IncDire, Dest.equals(this)? "this" : Dest);
	}
	
	public void setDirection(String s) {
		if (s.equalsIgnoreCase("n") || s.equalsIgnoreCase("north")) IncDire = Direction.NORTH;
		if (s.equalsIgnoreCase("s") || s.equalsIgnoreCase("south")) IncDire = Direction.SOUTH;
		if (s.equalsIgnoreCase("e") || s.equalsIgnoreCase("east")) IncDire = Direction.EAST;
		if (s.equalsIgnoreCase("w") || s.equalsIgnoreCase("west")) IncDire = Direction.WEST;
	}
	
	public void setDestination(MetroStation s) {
		Conf.dbg("SetDestination:"+s.Name);
		Dest = s;
		String destName = Conf.L_ST+s.Name+Conf.L_END;
		Block b = Pos.toLocation().getBlock();
		if (b.getBlockData() instanceof WallSign) {
			Util.setLine(b, 2, destName);
		}
	}
	
	public void setIndex(int n) {
		Block b = Pos.toLocation().getBlock();
		if (b.getBlockData() instanceof WallSign) {
			Util.setLine(b, 1, LineName+" §7["+n+"]");
		}
	}
	
	public void callCart() {
		Location loc = Pos.toLocation();
		Minecart cart = (Minecart)loc.getWorld().spawnEntity(loc.add(0, -1, 0), EntityType.MINECART);
		Route route = new Route(this, Dest);
		Conf.dbg("CallCart UUID:"+cart.getUniqueId());
		DataBase.DB.addRoute(cart.getUniqueId(), route);
	}
}
