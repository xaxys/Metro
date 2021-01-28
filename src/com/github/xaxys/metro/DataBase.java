package com.github.xaxys.metro;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;

public class DataBase implements Serializable {
	
	public transient static DataBase DB = null;
	
	public HashMap<String, MetroLine> MetroMap;
	public HashMap<Position, MetroStation> PositionMap;
	public transient HashMap<UUID, Route> RouteMap;
	
	public DataBase() {
		MetroMap = new HashMap<String, MetroLine>();
		PositionMap = new HashMap<Position, MetroStation>();
		RouteMap = new HashMap<UUID, Route>();
	}
	
	@Override
	public String toString() {
		StringBuilder mmsb = new StringBuilder();
		MetroMap.forEach((k, v) -> mmsb.append(String.format("{%s:%s},", k, v)));
		StringBuilder pmsb = new StringBuilder();
		PositionMap.forEach((k, v) -> pmsb.append(String.format("{%s:%s},", k, v)));
		return String.format("DataBase{MetroMap{%s},PositionMap{%s}}", mmsb, pmsb);
	}
	
	public void addStation(MetroStation s, int index) {
		if (MetroMap.containsKey(s.LineName) == false) {
			MetroMap.put(s.LineName, new MetroLine(s.LineName));
		}
		MetroLine line = MetroMap.get(s.LineName);
		s.Line = line;
		if (line.size() <= index) {
			line.add(s);
		} else {
			line.add(index, s);
		}
		
		PositionMap.put(s.Pos, s);
		Main.plugin.setTimeout(() -> this.saveDB(Conf.DB_PATH), 200);
	}
	
	public void addRoute(UUID uuid, Route route) {
		RouteMap.put(uuid, route);
	}
	
	public MetroStation getStation(Location loc) {
		return PositionMap.get(new Position(loc));
	}
	
	public Route getRoute(UUID uuid) {
		return uuid == null ? null : RouteMap.get(uuid);
	}
	
	public void delStation(MetroStation s) {
		PositionMap.remove(s.Pos);
		MetroLine line = s.Line;
		line.remove(s);
		if (line.isEmpty()) {
			MetroMap.remove(line.Name);
		}
		
		Main.plugin.setTimeout(() -> this.saveDB(Conf.DB_PATH), 200);
	}
	
	public void delRoute(UUID uuid) {
		RouteMap.remove(uuid);
	}
	
	public void complete() {
		MetroMap.forEach((k, v) -> {
			v.complete();
		});
		RouteMap = new HashMap<UUID, Route>();
	}
	
	public void saveDB(String s) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(s);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			fos.close();
			oos.close();
		} catch (IOException e) {
			Conf.err("saveDB", e.getMessage());
		}
	}
	
	public static DataBase readDB(String s) {
		FileInputStream fos = null;
		ObjectInputStream oos = null;
		DataBase db = null;
		try {
			fos = new FileInputStream(s);
			oos = new ObjectInputStream(fos);
			db = (DataBase) oos.readObject();
			db.complete();
			fos.close();
			oos.close();
		} catch (IOException | ClassNotFoundException e) {
			Conf.err("readDB", e.getMessage());
		}
		return db == null ? new DataBase() : db;
	}
}
