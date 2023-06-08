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

import java.util.Map;

public class EventListener implements Listener {

	private static final BlockFace[] ADJACENT_FACES = new BlockFace[] {
			BlockFace.SELF, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	
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
				}
				if (e.getLine(1).contains(" ")) {
					Conf.msg(e.getPlayer(), "MetroLine can't contain space on row 1!");
					return;
				}
				s.LineName = e.getLine(1);

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
				}
				s.Name = e.getLine(3);

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
				if (!station.callCart()) {
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
		// Check disabled world
		if (Conf.DISABLE_WORLDS.contains(e.getBlock().getWorld().getName())) return;

		for (BlockFace face : ADJACENT_FACES) { // Must check every block adjacent to this block myself after 1.20
			Block b = e.getBlock().getRelative(face);
			if (b.getBlockData() instanceof WallSign) {
				WallSign sign = (WallSign) b.getBlockData();
				MetroStation station = DataBase.DB.getStation(b.getLocation());
				if (station == null) return;

				Block backBlock = b.getRelative(sign.getFacing().getOppositeFace());
				if (!backBlock.getType().isSolid()) {
					backBlock.setType(Material.STONE, false); // not apply physics to avoid calling event again
				}
				e.setCancelled(true);
				Conf.dbg("CancelSignPhysics");
				break;
			}
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
				Player player = (Player) e.getEntered();
				if (!player.hasPermission(Main.PERM_USE)) {
					e.setCancelled(true);
					Conf.msg(player, Conf.MSG_NOPERM);
					return;
				}
			}

			route.Start = true;
			Vector vec = new Vector(0, 0, 0);
			int N = route.Orig.Line.isLoop ? 1 : MetroLine.compareStation(route.Dest, route.Orig);
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
			Bukkit.getScheduler().runTask(Main.plugin, v::remove);
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
		Minecart cart = (Minecart) e.getVehicle();
		
		// Check disabled world
		if (Conf.DISABLE_WORLDS.contains(e.getVehicle().getWorld().getName())) return;

		// Check metro cart
		Route route = DataBase.DB.getRoute(cart.getUniqueId());
		if (route == null) return;

		// Check if the cart is started
		if (!route.Start) {
			cart.setVelocity(new Vector(0, 0, 0));
			return;
		}

		// Detect station
		if (detectStation(cart, route)) return;

		// Detect railways
		Block railBlock = cart.getLocation().getBlock();
		if (!(railBlock.getBlockData() instanceof Rail)) return;
		Rail.Shape railShape = ((Rail) railBlock.getBlockData()).getShape();

		// Get Velocity Direction
		// When ascending or descending, the velocity of vehicle in Y-axis is still 0
		// So we need to the Rail.Shape to do second check
		Direction direction;
		Direction velocityDirection = Direction.parse(cart.getVelocity());
		Direction shapeDirection = Direction.parse(railShape);
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
		if (direction.isOblique()) {
			Block nextRail = direction.relativeBlock(cart.getLocation().getBlock());
			Direction facing = getFacingDirection(railShape, direction);
			detectRouterN(nextRail, facing, direction, route.Dest.getIndex(), true, 2);
			Direction near = intersectOne(Direction.parse(railShape).separate(), direction.separate());
			Block nearRail = near.relativeBlock(railBlock);
			detectRouterN(nearRail, near.opposite(), direction, route.Dest.getIndex(), true, 2);
		} else {
			Block nextRail = direction.relativeBlock(cart.getLocation().getBlock());
			detectRouter(nextRail, direction.opposite(), route.Dest.getIndex(), true);
		}

		// Handle Speed

		// Slow down or Normal
		// Speed is already limited by the setMaxSpeed() method
		if (route.Speed <= Conf.NORMAL_SPEED) {
			// Conf.dbg("No acceleration");
			return;
		}

		// Speed up
		Acceleration:
		{
			// Not Oblique and not Powered Rail
			if (!direction.isOblique() && railBlock.getType() != Material.POWERED_RAIL) break Acceleration;

			// Get Flat Length and Detect Router
			int flatLength = getFlatLength(railBlock, direction, Conf.ADJUST_LENGTH + Conf.BUFFER_LENGTH, route.Dest.getIndex(), true);

			if (flatLength < Conf.BUFFER_LENGTH) break Acceleration;
			flatLength -= Conf.BUFFER_LENGTH;
			double coefficient = (double) flatLength / Conf.ADJUST_LENGTH;
			double speed = Conf.NORMAL_SPEED + (route.Speed - Conf.NORMAL_SPEED) * coefficient;

			// ascending max speed
			if (direction.isAscending() && speed > Conf.ASCENDING_MAX_SPEED) {
				speed = Conf.ASCENDING_MAX_SPEED;
				// Conf.dbg("Speed up: Ascending max speed");
			}

			// descending max speed
			if (direction.isDescending() && speed > Conf.DESCENDING_MAX_SPEED) {
				speed = Conf.DESCENDING_MAX_SPEED;
				// Conf.dbg("Speed up: Descending max speed");
			}

			cart.setMaxSpeed(speed);
			setCartSpeed(cart, speed);

			// Conf.dbg(String.format("Speed up: %f, Flat Length: %d, Direction: %s", speed, flatLength, direction));
			return;
		}
		cart.setMaxSpeed(Conf.NORMAL_SPEED);
		// Conf.dbg("Speed up: Normal Speed");
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
					Player player = (Player) p;
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

	private static Direction detectRouter(Block railBlock, Direction facingDirection, int destIndex, boolean changeRouter) {
		if (railBlock.getType() != Material.RAIL) return null;
		facingDirection = facingDirection.flat();
		Block routerSign = railBlock.getRelative(0, -2, 0);
		routerSign = facingDirection.relativeBlock(routerSign);

		Router router = checkRouter(routerSign, facingDirection.toBlockFace());
		if (router == null) return null;

		Direction routerDirection = router.getDirection(destIndex);
		if (routerDirection == null || routerDirection == Direction.NONE) return null;

		if (changeRouter) {
			changeDirection(railBlock, routerDirection);
			Conf.dbg(String.format("Router Applied: %s -> %s ", destIndex, routerDirection));
		}
		return routerDirection;
	}

	//
	private static Direction getFacingDirection(Rail.Shape prevRailShape, Direction direction) {
		Direction facingDirection = direction.flat();
		if (direction.isOblique()) {
			Map.Entry<Direction, Direction> velocityDirections = direction.separate();
			Map.Entry<Direction, Direction> shapeDirections = Direction.parse(prevRailShape).separate();
			facingDirection = minusOne(velocityDirections, shapeDirections);
		}
		facingDirection = facingDirection.opposite();
		return facingDirection;
	}

	private static Direction detectRouterN(Block railBlock, Direction facingDirection, Direction direction, int destIndex, boolean changeRouter, int searchRange) {
		Block b = railBlock;
		for (int i = 0; i < searchRange; i++) {
			Direction d = detectRouter(b, facingDirection, destIndex, changeRouter);
			if (d != null) return d;
			b = direction.relativeBlock(b);
		}
		return null;
	}

	private static void changeDirection(Block block, Direction direction) {
		if (block.getType() != Material.RAIL) return;
		Rail rail = (Rail) block.getBlockData();
		rail.setShape(direction.toRailShape());
		block.setBlockData(rail, false);
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

		// Conf.dbg(String.format("Router Detected: %s", router));
		return router;
	}

	private int getFlatLength(Block block, Direction direction, int peekLength, int destIndex, boolean changeRouter) {
		int flatLength = 0;
		Rail.Shape shape = ((Rail) block.getBlockData()).getShape();
		Block relative = direction.relativeBlock(block);
		Direction facingDirection = getFacingDirection(shape, direction);

		while (flatLength < peekLength) {
			// if not powered rail, and the rail is not router, stop
			if (relative.getType() != block.getType() && relative.getType() != Material.RAIL) break;

			Rail.Shape nextShape = ((Rail) relative.getBlockData()).getShape();

			// if is rail, check router
			if (relative.getType() == Material.RAIL && !(direction.isAscending() || direction.isDescending())) {
				Direction routerDirection = detectRouter(relative, facingDirection, destIndex, changeRouter);
				if (routerDirection != null) nextShape = routerDirection.toRailShape();
			}

			if (nextShape != shape) break;
			relative = direction.relativeBlock(relative);
			flatLength++;
		}
		return flatLength;
	}

	private static Direction intersectOne(Map.Entry<Direction, Direction> a, Map.Entry<Direction, Direction> b) {
		if (a.getKey() == b.getKey()) return a.getKey();
		if (a.getKey() == b.getValue()) return a.getKey();
		if (a.getValue() == b.getKey()) return a.getValue();
		if (a.getValue() == b.getValue()) return a.getValue();
		return null;
	}

	private static Direction minusOne(Map.Entry<Direction, Direction> a, Map.Entry<Direction, Direction> b) {
		if (b == null) return a.getKey();
		if (a.getKey() == b.getKey()) return a.getValue();
		if (a.getKey() == b.getValue()) return a.getValue();
		return a.getKey();
	}
}
