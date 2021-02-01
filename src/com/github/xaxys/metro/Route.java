package com.github.xaxys.metro;

import java.util.UUID;

public class Route {
	
	public MetroStation Orig;
	public MetroStation Dest;
	public Boolean Start;
	public UUID CartUUID;
	
	public Route(MetroStation orig, MetroStation dest, UUID uuid) {
		Orig = orig;
		Dest = dest;
		Start = false;
		CartUUID = uuid;
	}
}
