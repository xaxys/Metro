package com.github.xaxys.metro;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent e) {
		
		if (e.getBlock().getBlockData() instanceof WallSign) {
			if (e.getLine(0).equalsIgnoreCase("[metro]")) {
				// Check permission
				if (!e.getPlayer().hasPermission(Main.PERM_CREATE)) {
					Conf.msg(e.getPlayer(), Conf.MSG_NOPERM);
					return;
				}

				e.setCancelled(true);
				
				// Line 0
				e.setLine(0, Conf.ERROR);

				MetroStation s = new MetroStation();
				int stationIdx;

				// Line 1
				if (e.getLine(1).isEmpty()) {
					Conf.msg(e.getPlayer(), "MetroLine is empty on row 1!");
					return;
				} else {
					s.LineName = e.getLine(1);
				}

				// Line 2
				Pattern p = Pattern.compile("^(\\d+)([NSEW])$");
				Matcher m = p.matcher(e.getLine(2));
				if (m.find()) {
					s.setDirection(m.group(2));
					stationIdx = Integer.parseInt(m.group(1));
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
				Conf.msg(e.getPlayer(), "MetroStation Created");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getBlockData() instanceof WallSign) {
			MetroStation station = DataBase.DB.getStation(e.getClickedBlock().getLocation());
			if (station == null) return;
			e.setCancelled(true);

			// Check permission
			if (!e.getPlayer().hasPermission(Main.PERM_USE)) {
				Conf.msg(e.getPlayer(), Conf.MSG_NOPERM);
				return;
			}
			if (e.getItem() == null || e.getPlayer().isSneaking()) {
				boolean f = station.callCart();
				if (f == false) {
					Conf.msg(e.getPlayer(), "Please clean the last minecart first.");
				}
			} else {
				MetroLine line = station.Line;
				int idx = line.indexOf(station.Dest);
				idx = (idx + 1) % line.size();
				station.setDestination(line.get(idx));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent e) {
		Block b = e.getBlock();
		if (b.getBlockData() instanceof WallSign) {
			WallSign sign = (WallSign) b.getBlockData();
			MetroStation station = DataBase.DB.getStation(b.getLocation());
			if (station == null) return;

			Block backBlock = b.getRelative(sign.getFacing().getOppositeFace());
			if (backBlock.getType() == Material.AIR) {
				backBlock.setType(Material.STONE);
			}
			e.setCancelled(true);
			Conf.dbg("CancelSignPhysics");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getBlock().getBlockData() instanceof WallSign) {
			MetroStation station = DataBase.DB.getStation(e.getBlock().getLocation());
			if (station == null) return;

			// Check permission
			if (!e.getPlayer().hasPermission(Main.PERM_CREATE)) {
				e.setCancelled(true);
				Conf.msg(e.getPlayer(), Conf.MSG_NOPERM);
				return;
			}
			DataBase.DB.delStation(station);
			Conf.dbg("onStationSignBreak");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleEnter(VehicleEnterEvent e) {
		Vehicle v = e.getVehicle();
		if (v instanceof Minecart) {
			Route route = DataBase.DB.getRoute(v.getUniqueId());
			if (route == null) return;

			// Check permission
			if (e.getEntered() instanceof Player) {
				Player player = ((Player) e.getEntered());
				if (!player.hasPermission(Main.PERM_USE)) {
					e.setCancelled(true);
					Conf.msg(player, Conf.MSG_NOPERM);
					return;
				}
			}

			route.Start = true;
			Vector vec = new Vector(0, 0, 0);
			Integer N = route.Orig.Line.isLoop ? 1 : MetroLine.compareStation(route.Dest, route.Orig);
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
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleExit(VehicleExitEvent e) {
		Vehicle v = e.getVehicle();
		if (v instanceof Minecart) {
			Route route = DataBase.DB.getRoute(v.getUniqueId());
			if (route == null) return;

			DataBase.DB.delRoute(v.getUniqueId());
			v.remove();
			Conf.dbg("onVehicleExit");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleDestroy(VehicleDestroyEvent e) {
		Vehicle v = e.getVehicle();
		if (v instanceof Minecart) {
			Route route = DataBase.DB.getRoute(v.getUniqueId());
			if (route == null) return;

			DataBase.DB.delRoute(v.getUniqueId());
			v.remove();
			e.setCancelled(true);
			Conf.dbg("onVehicleDestroy");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleMove(VehicleMoveEvent e) {
		if (!(e.getVehicle() instanceof Minecart)) return;
		Minecart cart = ((Minecart) e.getVehicle());

		Route route = DataBase.DB.getRoute(cart.getUniqueId());
		if (route == null) return;
		if (route.Start == false) {
			cart.setVelocity(new Vector(0, 0, 0));
			return;
		}
		Location loc = cart.getLocation();
		loc.setX(loc.getBlockX());
		loc.setY(loc.getBlockY() + 1);
		loc.setZ(loc.getBlockZ());
		MetroStation station = DataBase.DB.getStation(loc);
		if (station == route.Dest) {
			cart.setVelocity(new Vector(0, 0, 0));
		}
		if (station != null) {
			cart.getPassengers().forEach((p) -> {
				if (p instanceof Player) {
					Player player = ((Player) p);
					player.sendTitle(station.Name, station.LineName, 10, 70, 20);
					player.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 1);
				}
			});
		}

		// Acceleration

		Acceleration:
		{
			Block b = cart.getLocation().getBlock();
			if (!(b.getBlockData() instanceof Rail)) break Acceleration;

			Rail.Shape shape = ((Rail) b.getBlockData()).getShape();
			Vector v = e.getVehicle().getVelocity();
			if (v.getY() != 0) break Acceleration;

			BlockFace face;
			switch (shape) {
				case EAST_WEST:
					face = v.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
					break;
				case NORTH_SOUTH:
					face = v.getY() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
					break;
				case NORTH_EAST:
				case SOUTH_WEST:
					face = v.getX() > 0 ? BlockFace.SOUTH_EAST : BlockFace.NORTH_WEST;
					break;
				case NORTH_WEST:
				case SOUTH_EAST:
					face = v.getX() > 0 ? BlockFace.NORTH_EAST : BlockFace.SOUTH_WEST;
					break;
				default:
					break Acceleration;
			}

			int flatLength = 0;
			Block rb = b.getRelative(face);
			while (flatLength < Conf.ADJUST_LENGTH + Conf.BUFFER_LENGTH) {
				if (rb.getType() != b.getType()) break;
				BlockData data = rb.getBlockData();
				if (((Rail) data).getShape() != shape) break;
				rb = rb.getRelative(face);
				flatLength++;
			}

			if (flatLength < Conf.BUFFER_LENGTH) break Acceleration;
			flatLength -= Conf.BUFFER_LENGTH;
			double n = (double) flatLength / Conf.ADJUST_LENGTH;
			double speed = Conf.NORMAL_SPEED + (route.Speed - Conf.NORMAL_SPEED) * n;
			cart.setMaxSpeed(speed);
			return;
		}
		NoAcceleration:
		{
			cart.setMaxSpeed(Conf.NORMAL_SPEED);
			return;
		}
	}
}
