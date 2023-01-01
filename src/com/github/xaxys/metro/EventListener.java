package com.github.xaxys.metro;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
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
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onVehicleMove(VehicleMoveEvent e) {
		// Check cart
		if (!(e.getVehicle() instanceof Minecart)) return;
		Minecart cart = ((Minecart) e.getVehicle());
		
		// Check disabled world
		if (Conf.DISABLE_WORLDS.contains(e.getVehicle().getWorld().getName())) return;

		// Check metro cart
		Route route = DataBase.DB.getRoute(cart.getUniqueId());
		if (route == null) return;

		// Check if the cart is started
		if (route.Start == false) {
			cart.setVelocity(new Vector(0, 0, 0));
			return;
		}

		// Detect station
		if (detectStation(cart, route)) return;

		// Detect railways
		Block railBlock = cart.getLocation().getBlock();
		if (!(railBlock.getBlockData() instanceof Rail)) return;
		Rail railData = (Rail) railBlock.getBlockData();

		// Get Velocity Direction
		// When ascending or descending, the velocity of vehicle in Y-axis is still 0
		// So we need to the Rail.Shape to do second check
		Direction direction;
		Direction velocityDirection = Direction.parse(cart.getVelocity());
		Direction shapeDirection = Direction.parse(railData.getShape());
		if (velocityDirection == null) return;
		if (shapeDirection == null) return;

		if (shapeDirection.isAscending()) {
			direction = shapeDirection;
			// if rail is ascending, the velocity direction is opposite to the shape direction
			// then the velocity in Y-axis should be negative (i.e. descending)
			if ((shapeDirection.isEast() || shapeDirection.isWest()) && velocityDirection.isEast() != shapeDirection.isEast()) {
				direction = direction.opposite();
			} else if ((shapeDirection.isNorth() || shapeDirection.isSouth()) && velocityDirection.isNorth() != shapeDirection.isNorth()) {
				direction = direction.opposite();
			}
		} else {
			direction = velocityDirection;
		}

		// Detect Router
		detectRouter(cart.getLocation(), route, direction);

		// Handle Speed

		// Slow down or Normal
		// Speed is already limited by the setMaxSpeed() method
		if (route.Speed <= Conf.NORMAL_SPEED) {
			Conf.dbg("No acceleration");
			return;
		}

		// Speed up
		Acceleration:
		{
			// Not Oblique and not Powered Rail
			if (!direction.isOblique() && railBlock.getType() != Material.POWERED_RAIL) break Acceleration;

			// Get Flat Length and Detect Router
			int frontFlatLength = getFlatLength(railBlock, direction, route.Dest.getIndex(), true);
			int backFlatLength = getFlatLength(railBlock, direction, route.Dest.getIndex(), false);
			int flatLength = Math.min(frontFlatLength, backFlatLength);

			if (flatLength < Conf.BUFFER_LENGTH) break Acceleration;
			flatLength -= Conf.BUFFER_LENGTH;
			double coefficient = (double) flatLength / Conf.ADJUST_LENGTH;
			double speed = Conf.NORMAL_SPEED + (route.Speed - Conf.NORMAL_SPEED) * coefficient;

			// ascending max speed
			if (direction.isAscending() && speed > Conf.ASCENDING_MAX_SPEED) {
				speed = Conf.ASCENDING_MAX_SPEED;
				Conf.dbg("Speed up: Ascending max speed");
			}

			// descending max speed
			if (direction.isDescending() && speed > Conf.DESCENDING_MAX_SPEED) {
				speed = Conf.DESCENDING_MAX_SPEED;
				Conf.dbg("Speed up: Descending max speed");
			}

			cart.setMaxSpeed(speed);
			setCartSpeed(cart, speed);

			Conf.dbg(String.format("Speed up: %f, Flat Length: %d, Direction: %s", speed, flatLength, direction));
			return;
		}
		cart.setMaxSpeed(Conf.NORMAL_SPEED);
		Conf.dbg("Speed up: Normal Speed");
	}

	private static void setCartSpeed(Minecart cart, double speed) {
		Vector vel = cart.getVelocity().normalize().multiply(speed);
		try {
			vel.checkFinite();
		} catch (IllegalArgumentException ex) {
			return;
		}
		cart.setVelocity(vel);
	}

	private static boolean detectStation(Minecart cart, Route route) {
		Location loc = cart.getLocation();
		loc.setX(loc.getBlockX());
		loc.setY(loc.getBlockY() + 1);
		loc.setZ(loc.getBlockZ());
		MetroStation station = DataBase.DB.getStation(loc);
		if (station != null) {
			cart.getPassengers().forEach((p) -> {
				if (p instanceof Player) {
					Player player = ((Player) p);
					player.sendTitle(station.Name, station.LineName, 10, 70, 20);
					player.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2, 1);
				}
			});
			if (station == route.Dest) {
				cart.setVelocity(new Vector(0, 0, 0));
				return true;
			}
		}
		return false;
	}

	// Location is the center of the rail
	private static void detectRouter(Location loc, Route route, Direction direction) {
		Location signLoc = loc.clone();
		signLoc.setX(loc.getBlockX());
		signLoc.setY(loc.getBlockY() - 2);
		signLoc.setZ(loc.getBlockZ());
		Block sign = signLoc.getBlock();

		Direction routerDirection = checkRoutingDestination(sign, direction, route.Dest.getIndex());
		if (routerDirection == null || routerDirection == Direction.NONE) return;

		Block rail = loc.getBlock().getRelative(direction.toBlockFace());
		changeDirection(rail, routerDirection);
		Conf.dbg(String.format("Router Applied: %s -> %s", route.Dest.Name, direction));
	}

	private static void changeDirection(Block block, Direction direction) {
		if (block.getType() != Material.RAIL) return;
		Rail rail = (Rail) block.getBlockData();
		rail.setShape(direction.toRailShape());
		block.setBlockData(rail, false);
	}

	private static Direction checkRoutingDestination(Block block, Direction cartDirection, int destIndex) {
		// BlockFace of WallSign should be opposite to the cart direction
		Router router = checkRouter(block, cartDirection.opposite().toBlockFace());
		if (router == null) return null;

		return router.getDirection(destIndex);
	}

	private static Router checkRouter(Block block, BlockFace face) {
		if (!(block.getBlockData() instanceof WallSign)) return null;
		WallSign wallSign = (WallSign) block.getBlockData();

		if (wallSign.getFacing() != face) return null;

		Sign sign = (Sign) block.getState();
		if (!sign.getLine(0).equals(Conf.TITLE)) return null;

		Router router = new Router();
		router.addRule(sign.getLine(1));
		router.addRule(sign.getLine(2));
		router.addRule(sign.getLine(3));

		Conf.dbg(String.format("Router Detected: %s", router));
		return router;
	}

	private int getFlatLength(Block b, Direction direction, int destIndex, boolean changeRouter) {
		int flatLength = 0;
		Rail.Shape shape = ((Rail) b.getBlockData()).getShape();
		Vector v = direction.toVector();
		Block rb = b.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ());

		while (flatLength < Conf.ADJUST_LENGTH + Conf.BUFFER_LENGTH) {
			// if not powered rail, and the rail is not router, stop
			if (rb.getType() != b.getType()) {
				if (rb.getType() != Material.RAIL) break;
				Block routerSign = rb.getRelative(0, -2, 0).getRelative(direction.opposite().toBlockFace());
				Direction routerDirection = checkRoutingDestination(routerSign, direction, destIndex);
				if (routerDirection == null) break;
				// accelerate will not be stopped only when the routing direction is straight
				if (!routerDirection.isStraight()) break;
				if (changeRouter) changeDirection(rb, routerDirection);
			}
			if (((Rail) rb.getBlockData()).getShape() != shape) break;
			rb = rb.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ());
			flatLength++;
		}
		return flatLength;
	}
}
