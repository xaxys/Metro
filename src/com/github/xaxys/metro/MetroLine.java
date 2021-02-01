package com.github.xaxys.metro;

import java.io.Serializable;
import java.util.ArrayList;

public class MetroLine extends ArrayList<MetroStation> implements Serializable {

	// v1.1.2
	private static final long serialVersionUID = -5903088337869471028L;

	public String Name;
	boolean isLoop = false;
	
	public MetroLine(String name) {
		super();
		Name = name;
	}
	
	@Override
	public boolean add(MetroStation s) {
		boolean f = super.add(s);
		// update index of stop sign
		if (f) s.setIndex(this.size()-1);
		return f;
	}
	
	@Override
	public void add(int index, MetroStation s) {
		super.add(index, s);
		// update the index of all stop signs after it on the line
		for (int i = index; i < this.size(); i++) {
			this.get(i).setIndex(i);
		}
	}
	
	@Override
	public boolean remove(Object o) {
		int index = this.indexOf(o);
		boolean f = super.remove(o);
		if (f) {
			// update the index of all stop signs after it on the line
			for (int i = index; i < this.size(); i++) {
				this.get(i).setIndex(i);
			}
		}
		return f;
	}

	// complete fields after deserialization.
	public void complete() {
		this.forEach((s) -> s.complete(this));
	}

	// return 1 if index of a in MetroLine > index of b in MetroLine
	// return -1 if index of a in MetroLine > index of b in MetroLine
	// return 0 if index of a in MetroLine = index of b in MetroLine
	public static Integer compareStation(MetroStation a, MetroStation b) {
		MetroLine aLine = a.Line;
		MetroLine bLine = b.Line;
		if (aLine != bLine) return 2;
		int aIdx = aLine.indexOf(a);
		int bIdx = bLine.indexOf(b);
		return Integer.compare(aIdx, bIdx);
	}
	
}
