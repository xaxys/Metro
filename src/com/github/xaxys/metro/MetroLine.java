package com.github.xaxys.metro;

import java.io.Serializable;
import java.util.ArrayList;

public class MetroLine extends ArrayList<MetroStation> implements Serializable {
	
	public String Name;
	
	public MetroLine(String name) {
		super();
		Name = name;
	}
	
	@Override
	public boolean add(MetroStation s) {
		boolean f = super.add(s);
		if (f) s.setIndex(this.size()-1);
		return f;
	}
	
	@Override
	public void add(int index, MetroStation s) {
		super.add(index, s);
		for (int i = index; i < this.size(); i++) {
			this.get(i).setIndex(i);
		}
	}
	
	@Override
	public boolean remove(Object o) {
		int index = this.indexOf(o);
		boolean f = super.remove(o);
		if (f) {
			for (int i = index; i < this.size(); i++) {
				this.get(i).setIndex(i);
			}
		}
		return f;
	}
	
	public void complete() {
		this.forEach((s) -> s.complete(this));
	}
	
	public static Integer compareStation(MetroStation a, MetroStation b) {
		MetroLine aLine = a.Line;
		MetroLine bLine = b.Line;
		if (aLine != bLine) return 2;
		int aIdx = aLine.indexOf(a);
		int bIdx = bLine.indexOf(b);
		if (aIdx < bIdx) return -1;
		else if (aIdx > bIdx) return 1;
		else return 0;
	}
	
}
