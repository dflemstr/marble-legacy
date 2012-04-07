package org.marble.util;

import java.util.concurrent.atomic.AtomicInteger;

public class DoubleRingBuffer {
    private final AtomicInteger cursor = new AtomicInteger();
    private final double[] buffer;

    public DoubleRingBuffer(final int size) {
        buffer = new double[size];
    }

    public void insert(final double value) {
        final int index = cursor.getAndIncrement() % buffer.length;
        buffer[index] = value;
    }

    public double getAverage() {
        double result = 0.0;

        for (final double element : buffer) {
            result += element;
        }

        return result / buffer.length;
    }
}
