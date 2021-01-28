package com.github.xaxys.metro;

import java.io.Serializable;

public class Route implements Serializable {
	
	public MetroStation Orig;
	public MetroStation Dest;
	
	public Route(MetroStation orig, MetroStation dest) {
		Orig = orig;
		Dest = dest;
	}
}
