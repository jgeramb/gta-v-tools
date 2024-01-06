package dev.justix.gtavtools.tools;

import dev.justix.gtavtools.util.SystemUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RelativeToolData {

    private final HashMap<String, Map<String, Object>> data = new HashMap<>();

    public void add(String resolution, String key, Object value) {
        this.data.putIfAbsent(resolution, new HashMap<>());
        this.data.get(resolution).put(key, value);
    }

    public void addRect(String resolution, String key, int x, int y, int width, int height) {
        this.add(resolution, key, new Rectangle(x, y, width, height));
    }

    public void addPoint(String resolution, String key, int x, int y) {
        this.add(resolution, key, new Point(x, y));
    }

    public Object get(String key) {
        return this.data.get(SystemUtil.RESOLUTION).get(key);
    }

    public int getNumber(String key) {
        return (int) get(key);
    }

    public double getDecimal(String key) {
        return (double) get(key);
    }

    public Rectangle getRect(String key) {
        return (Rectangle) get(key);
    }

    public Point getPoint(String key) {
        return (Point) get(key);
    }

}
