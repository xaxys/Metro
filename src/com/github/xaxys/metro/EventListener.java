package com.github.xaxys.metro;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent e) {
		synchronized (Conf.API_SYNC) {
			if (e.getBlock().getBlockData() instanceof WallSign) {
				if (e.getLine(0).equalsIgnoreCase("[metro]")) {
					e.setCancelled(true);
					// Line 0
					e.setLine(0, Conf.ERROR);
					
					MetroStation s = new MetroStation();
					Integer stationIdx;
					
					// Line 1
					if (e.getLine(1).isEmpty()) {
						Conf.msg(e.getPlayer(), "Line is empty on row 1!");
						return;
					} else {
						s.LineName = e.getLine(1);
					}
					
					// Line 2
					Pattern p = Pattern.compile("^(\\d+)([N|S|E|W])$");
					Matcher m = p.matcher(e.getLine(2));
					if (m.find()) {
						s.setDirection(m.group(2));
						stationIdx = Integer.valueOf(m.group(1)); 
					} else {
						Conf.msg(e.getPlayer(), "StationIndex or Direction is missing on row 2!");
						return;
					}
					
					// Line 3
					if (e.getLine(3).isEmpty()) {
						Conf.msg(e.getPlayer(), "StationName is empty on row 3!");
						return;
					} else {
						s.Name = e.getLine(3);
					}
					
					s.Pos = new Position(e.getBlock().getLocation());
					
					Util.setSign(e.getBlock(), new String[]{
						Conf.TITLE,
						s.LineName,
						"",
						s.Name,
					});
					
					DataBase.DB.addStation(s, stationIdx);
					Conf.msg(e.getPlayer(), "MetroStaion Created");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e) {
		synchronized (Conf.API_SYNC) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getBlockData() instanceof WallSign) {
				MetroStation station = DataBase.DB.getStation(e.getClickedBlock().getLocation());
				if (station != null) {
					if (e.getItem() == null || e.getPlayer().isSneaking()) {
						station.callCart();
					} else {
						MetroLine line = station.Line;
						int idx = line.indexOf(station.Dest);
						idx = (idx + 1) % line.size();
						station.setDestination(line.get(idx));
					}
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent e) {
		synchronized (Conf.API_SYNC) {
			if (e.getBlock().getBlockData() instanceof WallSign &&
					DataBase.DB.getStation(e.getBlock().getLocation()) != null) {
				e.setCancelled(true);
				Conf.dbg("CancelSignPhysics");
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		synchronized (Conf.API_SYNC) {
			if (e.getBlock().getBlockData() instanceof WallSign) {
				MetroStation station = DataBase.DB.getStation(e.getBlock().getLocation()); 
				if (station != null) {
					DataBase.DB.delStation(station);
					Conf.dbg("onStationSignBreak");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleEnter(VehicleEnterEvent e) {
		Vehicle v = e.getVehicle();
		if (v instanceof Minecart) {
			Route route = DataBase.DB.getRoute(v.getUniqueId());
			if (route != null) {
				Vector vec = new Vector(0, 0, 0);
				Integer N = MetroLine.compareStation(route.Dest, route.Orig);
				switch (route.Orig.IncDire) {
				case NORTH: vec.setZ(-N); break;
				case SOUTH: vec.setZ(N); break;
				case WEST: vec.setX(-N); break;
				case EAST: vec.setX(N); break;
				}
				v.setVelocity(vec);
				Conf.dbg("onVehicleEnter");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleExit(VehicleExitEvent e) {
		Vehicle v = e.getVehicle();
		if (v instanceof Minecart) {
			Route route = DataBase.DB.getRoute(v.getUniqueId());
			if (route != null) {
				DataBase.DB.delRoute(v.getUniqueId());
				v.remove();
				Conf.dbg("onVehicleExit");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleDestroy(VehicleDestroyEvent e) {
		Vehicle v = e.getVehicle();
		if (v instanceof Minecart) {
			Route route = DataBase.DB.getRoute(v.getUniqueId());
			if (route != null) {
				DataBase.DB.delRoute(v.getUniqueId());
				v.remove();
				e.setCancelled(true);
				Conf.dbg("onVehicleDestroy");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleMove(VehicleMoveEvent e) {
		Vehicle v = e.getVehicle();
		if (v instanceof Minecart) {
			Route route = DataBase.DB.getRoute(v.getUniqueId());
			if (route != null) {
				Location loc = v.getLocation();
				loc.set(loc.getBlockX(), loc.getBlockY()+1, loc.getBlockZ());
				MetroStation station = DataBase.DB.getStation(loc);
				if (station == route.Dest) {
					v.setVelocity(new Vector(0, 0, 0));
				}
				if (station != null && v.isEmpty() == false) {
					v.getPassengers().forEach((p) -> {
						if (p instanceof Player) {
							((Player) p).sendTitle(station.Name, station.LineName, 10, 70, 20);
						}
					});
				}
			}
		}
	}
}
