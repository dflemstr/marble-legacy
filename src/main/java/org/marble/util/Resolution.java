package org.marble.util;

import java.awt.DisplayMode;

import com.google.common.collect.ComparisonChain;

public class Resolution implements Comparable<Resolution> {
    private final int depth;
    private final int freq;
    private final int height;
    private final int width;

    public Resolution(final int width, final int height, final int depth,
            final int freq) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.freq = freq;
    }

    @Override
    public int compareTo(final Resolution that) {
        return ComparisonChain.start().compare(width, that.width)
                .compare(height, that.height).compare(depth, that.depth)
                .compare(freq, that.freq).result();
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

    public int getDepth() {
        return depth;
    }

    public int getFrequency() {
        return freq;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public String toString() {
        final boolean incDepth = depth != DisplayMode.BIT_DEPTH_MULTI;
        final boolean incFreq = freq != DisplayMode.REFRESH_RATE_UNKNOWN;
        return width + "x" + height + (incDepth ? " @" + depth + "bpp" : "")
                + (incFreq ? " (" + freq + " Hz)" : "");
    }
}
