package org.marble.util;

import java.awt.DisplayMode;

import com.google.common.collect.ComparisonChain;

public class Resolution implements Comparable<Resolution> {
    private final int width;
    private final int height;
    private final int depth;
    private final int freq;

    public Resolution(final int width, final int height, final int depth,
            final int freq) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.freq = freq;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        final boolean incDepth = depth != DisplayMode.BIT_DEPTH_MULTI;
        final boolean incFreq = freq != DisplayMode.REFRESH_RATE_UNKNOWN;
        return width + "x" + height + (incDepth ? " @" + depth + "bpp" : "")
                + (incFreq ? " (" + freq + " Hz)" : "");
    }

    @Override
    public int compareTo(final Resolution that) {
        return ComparisonChain.start().compare(width, that.width)
                .compare(height, that.height).compare(depth, that.depth)
                .compare(freq, that.freq).result();
    }

    public int getDepth() {
        return depth;
    }

    public int getFrequency() {
        return freq;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Resolution) {
            final Resolution that = (Resolution) obj;
            return width == that.width && height == that.height
                    && depth == that.depth && freq == that.freq;
        } else
            return false;
    }
}
