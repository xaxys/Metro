package com.github.xaxys.metro;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router {

    interface Value {
        boolean isMatch(int value);
        boolean isMatch(Value value);
    }

    static class Exact implements Value {
        private final int value;

        public Exact(int value) {
            this.value = value;
        }

        @Override
        public boolean isMatch(int value) {
            return this.value == value;
        }

        @Override
        public boolean isMatch(Value value) {
            return value instanceof Exact && this.value == ((Exact)value).value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    static class Range implements Value {
        private final int min;
        private final int max;

        public Range(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean isMatch(int value) {
            return value >= min && value <= max;
        }

        @Override
        public boolean isMatch(Value value) {
            if (value instanceof Range) {
                Range range = (Range) value;
                return range.min >= min && range.max <= max;
            } else if (value instanceof Exact) {
                Exact exact = (Exact) value;
                return exact.value >= min && exact.value <= max;
            } else return false;
        }

        @Override
        public String toString() {
            return String.format("%d-%d", min, max);
        }
    }

    static class Rule {
        private final Value[] values;
        private final Direction direction;

        public Rule(Direction direction, Value... values) {
            this.direction = direction;
            this.values = values;
        }

        public static Rule parse(String rule) {
            Pattern rule_pattern = Pattern.compile("^((-?\\d+|-?\\d+--?\\d+)(,(-?\\d+|-?\\d+--?\\d+))*)?(([ADad]?[ESWNeswn])|SE|se|SW|sw|NW|nw|NE|ne)$");
            Matcher m = rule_pattern.matcher(rule);
            if (m.find()) {
                if (m.group(1) != null) {
                    String[] values = m.group(1).split(",");
                    Value[] value_list = new Value[values.length];
                    for (int i = 0; i < values.length; i++) {
                        Pattern range_pattern = Pattern.compile("^(-?\\d+)-(-?\\d+)$");
                        Matcher m2 = range_pattern.matcher(values[i]);
                        if (m2.find()) {
                            String min = m2.group(1);
                            String max = m2.group(2);
                            value_list[i] = new Range(Integer.parseInt(min), Integer.parseInt(max));
                        } else {
                            value_list[i] = new Exact(Integer.parseInt(values[i]));
                        }
                    }
                    Direction direction = Direction.parse(m.group(5));
                    return new Rule(direction, value_list);
                } else {
                    Direction direction = Direction.parse(m.group(5));
                    return new Rule(direction);
                }
            }
            return null;
        }

        public Direction getDirection() {
            return direction;
        }

        public Value[] getValues() {
            return values;
        }

        public Integer getExact() {
            if (values.length == 1 && values[0] instanceof Exact) {
                return ((Exact) values[0]).value;
            } else return null;
        }

        public boolean isMatch(int value) {
            if (values == null || values.length == 0) return true;
            for (Value v : values) {
                if (v.isMatch(value)) return true;
            }
            return false;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (values != null) {
                for (Value v : values) {
                    sb.append(v.toString()).append(",");
                }
                if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(direction.toShortString());
            return sb.toString();
        }
    }

    private final List<Rule> rules;

    public Router() {
        rules = new ArrayList<>();
    }

    public boolean addRule(String rule) {
        Rule r = Rule.parse(rule);
        if (r != null) {
            rules.add(r);
            return true;
        } else return false;
    }

    public Direction getDirection(int value) {
        for (Rule rule : rules) {
            if (rule.isMatch(value)) return rule.getDirection();
        }
        return Direction.NONE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Rule rule : rules) {
            sb.append(rule.toString()).append(";");
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
