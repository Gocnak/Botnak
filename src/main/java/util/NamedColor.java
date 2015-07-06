package util;

import java.awt.*;

/**
 * Created by Gocnak on 1/3/14.
 * <p>
 * Credit to tduva for the colors + idea.
 */
public class NamedColor {

    private String name;
    private int red, green, blue;

    public NamedColor(String name, int r, int g, int b) {
        this.name = name;
        red = r;
        green = g;
        blue = b;
    }

    public String getName() {
        return name;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public Color getColor() {
        return new Color(red, green, blue);
    }

}