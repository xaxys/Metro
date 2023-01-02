package com.github.xaxys.metro;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Switch;

public final class Util {
	
	public static void setPowered(Block b, boolean on) {
		if (b.getBlockData() instanceof Switch) {
			Switch s = (Switch) b.getBlockData();
			s.setPowered(on);
			b.setBlockData(s);
		}
	}

	public static void setSign(Block sign, String[] lines) {
		Sign s = ((Sign) sign.getState());
		s.setLine(0, lines[0] == null ? Conf.TITLE : lines[0]);
		s.setLine(1, lines[1] == null ? "" : lines[1]);
		s.setLine(2, lines[2] == null ? "" : lines[2]);
		s.setLine(3, lines[3] == null ? "" : lines[3]);
		s.update();
	}

	public static void setSign(Block sign, String title) {
		Sign s = ((Sign) sign.getState());
		s.setLine(0, title == null ? Conf.TITLE : title);
		s.setLine(1, "");
		s.setLine(2, "");
		s.setLine(3, "");
		s.update();
	}

	public static void setLine(Block sign, int l, String str) {
		Sign s = ((Sign) sign.getState());
		s.setLine(l, str == null ? "" : str);
		s.update();
	}

	public static String[] getLines(Block sign) {
		return ((Sign) sign.getState()).getLines();
	}
}
