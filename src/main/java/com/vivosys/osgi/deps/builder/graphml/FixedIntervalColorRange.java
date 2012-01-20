package com.vivosys.osgi.deps.builder.graphml;

import java.awt.*;

public class FixedIntervalColorRange implements ColorRange {

    private int min;
    private int max;

    private Color[] colors;

    public FixedIntervalColorRange(int min, int max, Color... colors) {
        this.min = min;
        this.max = max;
        this.colors = colors;
    }

    public Color getColor(long value) {
        if (value >= max) {
            value = max -1;
        }
        if (value < min) {
            value = min;
        }
        double v = value - min;
        double bucketSize = (((max - min) * 1.) / colors.length);
        v = v/bucketSize;
        return colors[((int) Math.floor(v))];
    }
}
