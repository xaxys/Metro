package com.github.xaxys.metro;

import java.util.UUID;

public class Route {
	
	public MetroStation Orig;
	public MetroStation Dest;
	public Boolean Start;
	public UUID CartUUID;
	public double Speed;

	public Route(MetroStation orig, MetroStation dest, UUID uuid, double speed) {
		Orig = orig;
		Dest = dest;
		Start = false;
		CartUUID = uuid;
		Speed = speed;
	}
}
