package com.github.xaxys.metro;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
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
import org.bukkit.event.vehicle.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class EventListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent e) {
		if (e.getBlock().getBlockData() instanceof WallSign) {
			// Check disabled world
			if (Conf.DISABLE_WORLDS.contains(e.getBlock().getWorld().getName())) return;
			if (e.getLine(0).equalsIgnoreCase("[metro]")) {
				// Check permission
				if (!e.getPlayer().hasPermission(Main.PERM_CREATE)) {
					Conf.msg(e.getPlayer(), Conf.MSG_NOPERM);
					return;
				}

				e.setCancelled(true);
				
				// Line 0
				Util.setSign(e.getBlock(), Conf.ERROR);

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
				Router.Rule rule = Router.Rule.parse(e.getLine(2));
				if (rule == null) {
					Conf.msg(e.getPlayer(), "Invalid Format on row 2!");
					return;
				}
				Integer parsedStationIdx = rule.getExact();
				if (parsedStationIdx == null) {
					Conf.msg(e.getPlayer(), "Invalid StationIndex on row 2!");
					return;
				}
				stationIdx = parsedStationIdx;
				Direction dir = rule.getDirection();
				if (!s.setDirection(dir.toShortString())) {
					Conf.msg(e.getPlayer(), "Invalid Direction on row 2!");
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

				Util.setSign(e.getBlock(), new String[] {
						Conf.TITLE,
						s.LineName,
						"",
						s.Name,
				});
				s.setDestination(s);

				DataBase.DB.addStation(s, stationIdx);
				Conf.msg(e.getPlayer(), "MetroStation Created");

			} else if (e.getLine(0).equalsIgnoreCase("[metro:router]")) {
				// Check permission
				if (!e.getPlayer().hasPermission(Main.PERM_CREATE)) {
					Conf.msg(e.getPlayer(), Conf.MSG_NOPERM);
					return;
				}
				e.setCancelled(true);

				// Line 0
				Util.setSign(e.getBlock(), Conf.ERROR);

				// Line 1-3
				for (int i = 1; i < 4; i++) {
					if (!e.getLine(i).isEmpty() && Router.Rule.parse(e.getLine(i)) == null) {
						Conf.msg(e.getPlayer(), "Invalid rule on row " + i);
						return;
					}
				}

				Util.setSign(e.getBlock(), new String[] {
						Conf.TITLE,
						e.getLine(1),
						e.getLine(2),
						e.getLine(3),
				});
				Conf.msg(e.getPlayer(), "Router Created");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND) return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getBlockData() instanceof WallSign) {
			// Check disabled world
			if (Conf.DISABLE_WORLDS.contains(e.getPlayer().getWorld().getName())) return;
			
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
			// Check disabled world
			if (Conf.DISABLE_WORLDS.contains(b.getWorld().getName())) return;
			
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
			// Check disabled world
			if (Conf.DISABLE_WORLDS.contains(e.getBlock().getWorld().getName())) return;
			
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
			// Check disabled world
			if (Conf.DISABLE_WORLDS.contains(v.getWorld().getName())) return;
			
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
			// Check disabled world
			if (Conf.DISABLE_WORLDS.contains(v.getWorld().getName())) return;
			
			Route route = DataBase.DB.getRoute(v.getUniqueId());
			if (route == null) return;
			
			DataBase.DB.delRoute(v.getUniqueId());
			// Remove minecart on the next tick to avoid player falling
			Bukkit.getScheduler().runTask(Main.plugin, () -> {
				v.remove();
			});
			Conf.dbg("onVehicleExit");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleDestroy(VehicleDestroyEvent e) {
		Vehicle v = e.getVehicle();
		if (v instanceof Minecart) {
			// Check disabled world
			if (Conf.DISABLE_WORLDS.contains(v.getWorld().getName())) return;
			
			Route route = DataBase.DB.getRoute(v.getUniqueId());
			if (route == null) return;

			DataBase.DB.delRoute(v.getUniqueId());
			v.remove();
			e.setCancelled(true);
			Conf.dbg("onVehicleDestroy");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleCollision(VehicleEntityCollisionEvent e) {
		Vehicle v = e.getVehicle();
		if (!(v instanceof Minecart)) return;

		// Except: disabled world
		if (Conf.DISABLE_WORLDS.contains(v.getWorld().getName())) return;

		// Except: non-metro cart
		Route route = DataBase.DB.getRoute(v.getUniqueId());
		if (route == null) return;

		// Except: empty cart should be able to be collided
		if (v.getPassengers().isEmpty()) return;

		e.setCancelled(true);
		Conf.dbg("onVehicleCollision");
	}

	private static Map<Direction, Rail.Shape> railShape = new HashMap<Direction, Rail.Shape>() {{
		put(Direction.NORTH, Rail.Shape.NORTH_SOUTH);
		put(Direction.SOUTH, Rail.Shape.NORTH_SOUTH);
		put(Direction.WEST, Rail.Shape.EAST_WEST);
		put(Direction.EAST, Rail.Shape.EAST_WEST);
		put(Direction.NORTH_WEST, Rail.Shape.NORTH_WEST);
		put(Direction.NORTH_EAST, Rail.Shape.NORTH_EAST);
		put(Direction.SOUTH_WEST, Rail.Shape.SOUTH_WEST);
		put(Direction.SOUTH_EAST, Rail.Shape.SOUTH_EAST);
		put(Direction.ASCENDING_EAST, Rail.Shape.ASCENDING_EAST);
		put(Direction.ASCENDING_WEST, Rail.Shape.ASCENDING_WEST);
		put(Direction.ASCENDING_NORTH, Rail.Shape.ASCENDING_NORTH);
		put(Direction.ASCENDING_SOUTH, Rail.Shape.ASCENDING_SOUTH);
		put(Direction.DESCENDING_EAST, Rail.Shape.ASCENDING_WEST);
		put(Direction.DESCENDING_WEST, Rail.Shape.ASCENDING_EAST);
		put(Direction.DESCENDING_NORTH, Rail.Shape.ASCENDING_SOUTH);
		put(Direction.DESCENDING_SOUTH, Rail.Shape.ASCENDING_NORTH);
	}};

	private static Map<Rail.Shape, Vector> railVec = new HashMap<Rail.Shape, Vector>() {{
		put(Rail.Shape.NORTH_SOUTH, new Vector(0, 0, 1));
		put(Rail.Shape.EAST_WEST, new Vector(1, 0, 0));
		put(Rail.Shape.ASCENDING_EAST, new Vector(1, 1, 0));
		put(Rail.Shape.ASCENDING_WEST, new Vector(-1, 1, 0));
		put(Rail.Shape.ASCENDING_NORTH, new Vector(0, 1, -1));
		put(Rail.Shape.ASCENDING_SOUTH, new Vector(0, 1, 1));
		put(Rail.Shape.SOUTH_EAST, new Vector(-1, 0, 1));
		put(Rail.Shape.NORTH_WEST, new Vector(-1, 0, 1));
		put(Rail.Shape.SOUTH_WEST, new Vector(1, 0, 1));
		put(Rail.Shape.NORTH_EAST, new Vector(1, 0, 1));
	}};
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleMove(VehicleMoveEvent e) {
		if (!(e.getVehicle() instanceof Minecart)) return;
		Minecart cart = ((Minecart) e.getVehicle());
		
		// Check disabled world
		if (Conf.DISABLE_WORLDS.contains(e.getVehicle().getWorld().getName())) return;

		Route route = DataBase.DB.getRoute(cart.getUniqueId());
		if (route == null) return;

		if (route.Start == false) {
			cart.setVelocity(new Vector(0, 0, 0));
			return;
		}

		// Detect station
		Location loc = cart.getLocation();
		loc.setX(loc.getBlockX());
		loc.setY(loc.getBlockY() + 1);
		loc.setZ(loc.getBlockZ());
		MetroStation station = DataBase.DB.getStation(loc);
		if (station == route.Dest) {
			cart.setVelocity(new Vector(0, 0, 0));
			return;
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

		// Detect switcher
		Switcher:
		{
			Location loc2 = cart.getLocation();
			loc2.setX(loc2.getBlockX());
			loc2.setY(loc2.getBlockY() - 2);
			loc2.setZ(loc2.getBlockZ());

			if (!(loc2.getBlock().getBlockData() instanceof WallSign)) break Switcher;

			Sign sign = (Sign) loc2.getBlock().getState();
			if (!sign.getLine(0).equals(Conf.TITLE)) break Switcher;

			Router router = new Router();
			router.addRule(sign.getLine(1));
			router.addRule(sign.getLine(2));
			router.addRule(sign.getLine(3));

			Direction direction = router.getDirection(route.Dest.getIndex());
			if (direction == null || direction == Direction.NONE) break Switcher;

			// Change direction
			WallSign wallSign = (WallSign) loc2.getBlock().getBlockData();
			BlockFace face = wallSign.getFacing().getOppositeFace();
			Block block = loc2.getBlock().getRelative(face).getRelative(0, 2, 0);
			if (block.getType() != Material.RAIL) break Switcher;

			Rail rail = (Rail) block.getBlockData();
			rail.setShape(railShape.get(direction));
			block.setBlockData(rail);
			Conf.dbg(String.format("Switcher: %s -> %s", route.Dest.Name, direction));
		}

		double speed = Conf.NORMAL_SPEED;

		// Slow down
		if (route.Speed <= Conf.NORMAL_SPEED) {
			speed = route.Speed;
		}

		// Speed up
		Acceleration:
		{
			Vector v = e.getVehicle().getVelocity();
			Block b = cart.getLocation().getBlock();
			if (!(b.getBlockData() instanceof Rail)) break Acceleration;

			Rail.Shape shape = ((Rail) b.getBlockData()).getShape();
			Vector face = railVec.get(shape);

			// Not Slant and not Powered Rail
			if (!(face.getX() != 0 && face.getZ() != 0) && b.getType() != Material.POWERED_RAIL) break Acceleration;

			int frontFlatLength = getFlatLength(b, shape, face);
			int backFlatLength = getFlatLength(b, shape, face.multiply(-1));
			int flatLength = Math.min(frontFlatLength, backFlatLength);

			// back to original
			face.multiply(-1);

			if (flatLength < Conf.BUFFER_LENGTH) break Acceleration;
			flatLength -= Conf.BUFFER_LENGTH;
			double coefficient = (double) flatLength / Conf.ADJUST_LENGTH;
			speed = Conf.NORMAL_SPEED + (route.Speed - Conf.NORMAL_SPEED) * coefficient;

			// ascending max speed
			if (face.getY() != 0 && (v.getX() * face.getX() > 0.01 || v.getZ() * face.getZ() > 0.01) && speed > Conf.ASCENDING_MAX_SPEED) {
				speed = Conf.ASCENDING_MAX_SPEED;
				Conf.dbg("Speed up: Ascending max speed");
			}

			// descending max speed
			if (face.getY() != 0 && (v.getX() * face.getX() < -0.01 || v.getZ() * face.getZ() < -0.01) && speed > Conf.DESCENDING_MAX_SPEED) {
				speed = Conf.DESCENDING_MAX_SPEED;
				Conf.dbg("Speed up: Descending max speed");
			}
		}

		Vector vel = cart.getVelocity().normalize().multiply(speed);
		try {
			vel.checkFinite();
		} catch (IllegalArgumentException ex) {
			return;
		}
		cart.setVelocity(vel);
	}

	private boolean isAscending(Vector v) {
		return v.getY() > 0.01;
	}

	private int getFlatLength(Block b, Rail.Shape shape, Vector face) {
		int flatLength = 0;
		Block rb = b.getRelative(face.getBlockX(), face.getBlockY(), face.getBlockZ());

		while (flatLength < Conf.ADJUST_LENGTH + Conf.BUFFER_LENGTH) {
			if (rb.getType() != b.getType()) break;
			if (((Rail) rb.getBlockData()).getShape() != shape) break;
			rb = rb.getRelative(face.getBlockX(), face.getBlockY(), face.getBlockZ());
			flatLength++;
		}
		return flatLength;
	}
}
