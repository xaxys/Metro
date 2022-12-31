package com.github.xaxys.metro;

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

    static Map<Direction, Direction> oppositeMap = new HashMap<Direction, Direction>() {{
        put(NONE, NONE);
        put(ASCENDING_EAST, DESCENDING_EAST);
        put(ASCENDING_SOUTH, DESCENDING_SOUTH);
        put(ASCENDING_WEST, DESCENDING_WEST);
        put(ASCENDING_NORTH, DESCENDING_NORTH);
        put(DESCENDING_EAST, ASCENDING_EAST);
        put(DESCENDING_SOUTH, ASCENDING_SOUTH);
        put(DESCENDING_WEST, ASCENDING_WEST);
        put(DESCENDING_NORTH, ASCENDING_NORTH);
        put(EAST, WEST);
        put(SOUTH, NORTH);
        put(WEST, EAST);
        put(NORTH, SOUTH);
        put(SOUTH_EAST, NORTH_WEST);
        put(SOUTH_WEST, NORTH_EAST);
        put(NORTH_WEST, SOUTH_EAST);
        put(NORTH_EAST, SOUTH_WEST);
    }};

    static Map<Direction, String> nameMap = new HashMap<Direction, String>() {{
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

    public static Direction parse(String name) {
        return enumMap.get(name.toUpperCase());
    }

    public static Direction opposite(Direction direction) {
        return oppositeMap.get(direction);
    }

    public String toShortString() {
        return nameMap.get(this);
    }
}
