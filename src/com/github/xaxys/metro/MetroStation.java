package com.github.xaxys.metro;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;

import java.io.Serializable;

public class MetroStation implements Serializable {

	// v1.1.2
	private static final long serialVersionUID = -6390181463722350211L;
	
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
	public int DestIndex;
	
	public transient MetroLine Line;
	public transient MetroStation Dest;
	public transient Route LastRoute;
	
	public void complete(MetroLine line) {
		Line = line;
		Dest = Line.get(DestIndex);
	}
	
	public void updateDest(MetroLine line) {
		DestIndex = line.indexOf(Dest);
		if (DestIndex < 0) {
			setDestination(this);
			DestIndex = line.indexOf(this);
		}
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
	
	public boolean setDirection(String s) {
		if (s.equalsIgnoreCase("n") || s.equalsIgnoreCase("north")) {
			IncDire = Direction.NORTH;
			return true;
		} else if (s.equalsIgnoreCase("s") || s.equalsIgnoreCase("south")) {
			IncDire = Direction.SOUTH;
			return true;
		} else if (s.equalsIgnoreCase("e") || s.equalsIgnoreCase("east")) {
			IncDire = Direction.EAST;
			return true;
		} else if (s.equalsIgnoreCase("w") || s.equalsIgnoreCase("west")) {
			IncDire = Direction.WEST;
			return true;
		}
		return false;
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

	public int getIndex() {
		return Line.indexOf(this);
	}

	// spawn a cart near the station
	// return false if there is a unused cart spawned by this station nearby
	public boolean callCart() {
		Location loc = Pos.toLocation();
		if (LastRoute != null && !LastRoute.Start) {
			Entity cart = Bukkit.getEntity(LastRoute.CartUUID);
			if (cart != null) {
				// clean the unused cart spawned by this station faraway
				if (cart.getLocation().distance(loc) > 5) cart.remove();
				else return false;
			}
		}
		Entity cart = loc.getWorld().spawnEntity(loc.add(0.5, -1, 0.5), EntityType.MINECART);
		LastRoute = new Route(this, Dest, cart.getUniqueId(), Line.Speed);
		cart.setCustomName(LastRoute.Orig.Name+"-"+LastRoute.Dest.Name);
		cart.setCustomNameVisible(true);
		((Minecart) cart).setMaxSpeed(Line.Speed);
		Conf.dbg("CallCart UUID:"+cart.getUniqueId());
		DataBase.DB.addRoute(cart.getUniqueId(), LastRoute);
		return true;
	}
}
