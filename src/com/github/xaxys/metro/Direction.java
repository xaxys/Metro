package com.github.xaxys.metro;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rail;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public enum Direction {
    NONE,
    ASCENDING_EAST,
    ASCENDING_SOUTH,
    ASCENDING_WEST,
    ASCENDING_NORTH,
    DESCENDING_EAST,
    DESCENDING_SOUTH,
    DESCENDING_WEST,
    DESCENDING_NORTH,
    EAST,
    SOUTH,
    WEST,
    NORTH,
    SOUTH_EAST,
    SOUTH_WEST,
    NORTH_WEST,
    NORTH_EAST;

    Direction() {
    }

    static Map<String, Direction> enumMap = new HashMap<String, Direction>() {{
        put("NONE", NONE);
        put("NULL", NONE);
        put("NIL", NONE);

        put("AE", ASCENDING_EAST);
        put("AS", ASCENDING_SOUTH);
        put("AW", ASCENDING_WEST);
        put("AN", ASCENDING_NORTH);
        put("DE", DESCENDING_EAST);
        put("DS", DESCENDING_SOUTH);
        put("DW", DESCENDING_WEST);
        put("DN", DESCENDING_NORTH);

        put("ASCENDINGEAST", ASCENDING_EAST);
        put("ASCENDINGSOUTH", ASCENDING_SOUTH);
        put("ASCENDINGWEST", ASCENDING_WEST);
        put("ASCENDINGNORTH", ASCENDING_NORTH);
        put("DESCENDINGEAST", DESCENDING_EAST);
        put("DESCENDINGSOUTH", DESCENDING_SOUTH);
        put("DESCENDINGWEST", DESCENDING_WEST);
        put("DESCENDINGNORTH", DESCENDING_NORTH);

        put("ASCEAST", ASCENDING_EAST);
        put("ASCESOUTH", ASCENDING_SOUTH);
        put("ASCWEST", ASCENDING_WEST);
        put("ASCNORTH", ASCENDING_NORTH);
        put("DESCEAST", DESCENDING_EAST);
        put("DESCSOUTH", DESCENDING_SOUTH);
        put("DESCWEST", DESCENDING_WEST);
        put("DESCNORTH", DESCENDING_NORTH);

        put("ASCENDING_EAST", ASCENDING_EAST);
        put("ASCENDING_SOUTH", ASCENDING_SOUTH);
        put("ASCENDING_WEST", ASCENDING_WEST);
        put("ASCENDING_NORTH", ASCENDING_NORTH);
        put("DESCENDING_EAST", DESCENDING_EAST);
        put("DESCENDING_SOUTH", DESCENDING_SOUTH);
        put("DESCENDING_WEST", DESCENDING_WEST);
        put("DESCENDING_NORTH", DESCENDING_NORTH);

        put("E", EAST);
        put("S", SOUTH);
        put("W", WEST);
        put("N", NORTH);

        put("EAST", EAST);
        put("SOUTH", SOUTH);
        put("WEST", WEST);
        put("NORTH", NORTH);

        put("SE", SOUTH_EAST);
        put("SW", SOUTH_WEST);
        put("NW", NORTH_WEST);
        put("NE", NORTH_EAST);

        put("SOUTHEAST", SOUTH_EAST);
        put("SOUTHWEST", SOUTH_WEST);
        put("NORTHWEST", NORTH_WEST);
        put("NORTHEAST", NORTH_EAST);

        put("SOUTH_EAST", SOUTH_EAST);
        put("SOUTH_WEST", SOUTH_WEST);
        put("NORTH_WEST", NORTH_WEST);
        put("NORTH_EAST", NORTH_EAST);
    }};

    public boolean isAscending() {
        return this == ASCENDING_EAST || this == ASCENDING_SOUTH || this == ASCENDING_WEST || this == ASCENDING_NORTH;
    }

    public boolean isDescending() {
        return this == DESCENDING_EAST || this == DESCENDING_SOUTH || this == DESCENDING_WEST || this == DESCENDING_NORTH;
    }

    public boolean isOblique() {
        return this == SOUTH_EAST || this == SOUTH_WEST || this == NORTH_WEST || this == NORTH_EAST;
    }

    public boolean isStraight() {
        return this == EAST || this == SOUTH || this == WEST || this == NORTH;
    }

    public boolean isEast() {
        return this == EAST || this == ASCENDING_EAST || this == DESCENDING_EAST;
    }

    public boolean isSouth() {
        return this == SOUTH || this == ASCENDING_SOUTH || this == DESCENDING_SOUTH;
    }

    public boolean isWest() {
        return this == WEST || this == ASCENDING_WEST || this == DESCENDING_WEST;
    }

    public boolean isNorth() {
        return this == NORTH || this == ASCENDING_NORTH || this == DESCENDING_NORTH;
    }

    public static Direction parse(String name) {
        return enumMap.get(name.toUpperCase());
    }

    static Map<Direction, Direction> oppositeMap = new HashMap<Direction, Direction>() {{
        put(NONE, NONE);
        put(ASCENDING_EAST, DESCENDING_WEST);
        put(ASCENDING_SOUTH, DESCENDING_NORTH);
        put(ASCENDING_WEST, DESCENDING_EAST);
        put(ASCENDING_NORTH, DESCENDING_SOUTH);
        put(DESCENDING_EAST, ASCENDING_WEST);
        put(DESCENDING_SOUTH, ASCENDING_NORTH);
        put(DESCENDING_WEST, ASCENDING_EAST);
        put(DESCENDING_NORTH, ASCENDING_SOUTH);
        put(EAST, WEST);
        put(SOUTH, NORTH);
        put(WEST, EAST);
        put(NORTH, SOUTH);
        put(SOUTH_EAST, NORTH_WEST);
        put(SOUTH_WEST, NORTH_EAST);
        put(NORTH_WEST, SOUTH_EAST);
        put(NORTH_EAST, SOUTH_WEST);
    }};

    public Direction opposite() {
        return oppositeMap.get(this);
    }

    static Map<Direction, String> shortNameMap = new HashMap<Direction, String>() {{
        put(NONE, "NONE");
        put(ASCENDING_EAST, "AE");
        put(ASCENDING_SOUTH, "AS");
        put(ASCENDING_WEST, "AW");
        put(ASCENDING_NORTH, "AN");
        put(DESCENDING_EAST, "DE");
        put(DESCENDING_SOUTH, "DS");
        put(DESCENDING_WEST, "DW");
        put(DESCENDING_NORTH, "DN");
        put(EAST, "E");
        put(SOUTH, "S");
        put(WEST, "W");
        put(NORTH, "N");
        put(SOUTH_EAST, "SE");
        put(SOUTH_WEST, "SW");
        put(NORTH_WEST, "NW");
        put(NORTH_EAST, "NE");
    }};

    public String toShortString() {
        return shortNameMap.get(this);
    }

    // RailShape
    private static Map<Direction, Rail.Shape> toRailShapeMap = new HashMap<Direction, Rail.Shape>() {{
        put(Direction.NONE, null);
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

    public Rail.Shape toRailShape() {
        return toRailShapeMap.get(this);
    }

    private static Map<Rail.Shape, Direction> parseRailShapeMap = new HashMap<Rail.Shape, Direction>() {{
        put(Rail.Shape.NORTH_SOUTH, Direction.SOUTH);
        put(Rail.Shape.EAST_WEST, Direction.EAST);
        put(Rail.Shape.ASCENDING_EAST, Direction.ASCENDING_EAST);
        put(Rail.Shape.ASCENDING_WEST, Direction.ASCENDING_WEST);
        put(Rail.Shape.ASCENDING_NORTH, Direction.ASCENDING_NORTH);
        put(Rail.Shape.ASCENDING_SOUTH, Direction.ASCENDING_SOUTH);
        put(Rail.Shape.SOUTH_EAST, Direction.SOUTH_EAST);
        put(Rail.Shape.SOUTH_WEST, Direction.SOUTH_WEST);
        put(Rail.Shape.NORTH_WEST, Direction.NORTH_WEST);
        put(Rail.Shape.NORTH_EAST, Direction.NORTH_EAST);
    }};

    public static Direction parse(Rail.Shape shape) {
        return parseRailShapeMap.get(shape);
    }

    // BlockFace
    private static Map<Direction, BlockFace> toBlockFaceMap = new HashMap<Direction, BlockFace>() {{
        put(Direction.NORTH, BlockFace.NORTH);
        put(Direction.SOUTH, BlockFace.SOUTH);
        put(Direction.WEST, BlockFace.WEST);
        put(Direction.EAST, BlockFace.EAST);
        put(Direction.NORTH_WEST, BlockFace.NORTH_WEST);
        put(Direction.NORTH_EAST, BlockFace.NORTH_EAST);
        put(Direction.SOUTH_WEST, BlockFace.SOUTH_WEST);
        put(Direction.SOUTH_EAST, BlockFace.SOUTH_EAST);
    }};

    public BlockFace toBlockFace() {
        return toBlockFaceMap.get(this);
    }

    private static Map<BlockFace, Direction> parseBlockFaceMap = new HashMap<BlockFace, Direction>() {{
        put(BlockFace.NORTH, Direction.NORTH);
        put(BlockFace.SOUTH, Direction.SOUTH);
        put(BlockFace.WEST, Direction.WEST);
        put(BlockFace.EAST, Direction.EAST);
        put(BlockFace.NORTH_WEST, Direction.NORTH_WEST);
        put(BlockFace.NORTH_EAST, Direction.NORTH_EAST);
        put(BlockFace.SOUTH_WEST, Direction.SOUTH_WEST);
        put(BlockFace.SOUTH_EAST, Direction.SOUTH_EAST);
    }};

    public static Direction parse(BlockFace face) {
        return parseBlockFaceMap.get(face);
    }

    // Bukkit Vector
    private static Map<Direction, Vector> toVectorMap = new HashMap<Direction, Vector>() {{
        put(Direction.NONE, new Vector(0, 0, 0));
        put(Direction.NORTH, new Vector(0, 0, -1));
        put(Direction.SOUTH, new Vector(0, 0, 1));
        put(Direction.WEST, new Vector(-1, 0, 0));
        put(Direction.EAST, new Vector(1, 0, 0));
        put(Direction.NORTH_WEST, new Vector(-1, 0, -1));
        put(Direction.NORTH_EAST, new Vector(1, 0, -1));
        put(Direction.SOUTH_WEST, new Vector(-1, 0, 1));
        put(Direction.SOUTH_EAST, new Vector(1, 0, 1));
        put(Direction.ASCENDING_EAST, new Vector(1, 1, 0));
        put(Direction.ASCENDING_WEST, new Vector(-1, 1, 0));
        put(Direction.ASCENDING_NORTH, new Vector(0, 1, -1));
        put(Direction.ASCENDING_SOUTH, new Vector(0, 1, 1));
        put(Direction.DESCENDING_EAST, new Vector(1, -1, 0));
        put(Direction.DESCENDING_WEST, new Vector(-1, -1, 0));
        put(Direction.DESCENDING_NORTH, new Vector(0, -1, -1));
        put(Direction.DESCENDING_SOUTH, new Vector(0, -1, 1));
    }};

    public Vector toVector() {
        return toVectorMap.get(this);
    }

    private static double DoubleEqualityThreshold = 0.0001;

    public static boolean isZero(double d) {
        return Math.abs(d) < DoubleEqualityThreshold;
    }

    private static Map<Vector, Direction> parseVectorMap = new HashMap<Vector, Direction>() {{
        put(new Vector(0, 0, 0), Direction.NONE);
        put(new Vector(0, 0, -1), Direction.NORTH);
        put(new Vector(0, 0, 1), Direction.SOUTH);
        put(new Vector(-1, 0, 0), Direction.WEST);
        put(new Vector(1, 0, 0), Direction.EAST);
        put(new Vector(-1, 0, -1), Direction.NORTH_WEST);
        put(new Vector(1, 0, -1), Direction.NORTH_EAST);
        put(new Vector(-1, 0, 1), Direction.SOUTH_WEST);
        put(new Vector(1, 0, 1), Direction.SOUTH_EAST);
        put(new Vector(1, 1, 0), Direction.ASCENDING_EAST);
        put(new Vector(-1, 1, 0), Direction.ASCENDING_WEST);
        put(new Vector(0, 1, -1), Direction.ASCENDING_NORTH);
        put(new Vector(0, 1, 1), Direction.ASCENDING_SOUTH);
        put(new Vector(1, -1, 0), Direction.DESCENDING_EAST);
        put(new Vector(-1, -1, 0), Direction.DESCENDING_WEST);
        put(new Vector(0, -1, -1), Direction.DESCENDING_NORTH);
        put(new Vector(0, -1, 1), Direction.DESCENDING_SOUTH);
    }};

    public static Direction parse(Vector vector) {
        int x = isZero(vector.getX()) ? 0 : (vector.getX() > 0 ? 1 : -1);
        int y = isZero(vector.getY()) ? 0 : (vector.getY() > 0 ? 1 : -1);
        int z = isZero(vector.getZ()) ? 0 : (vector.getZ() > 0 ? 1 : -1);
        Vector normalized = new Vector(x, y, z);
        return parseVectorMap.get(normalized);
    }
}
