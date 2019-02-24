package org.dstadler.commons.metrics;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MovingAverageTest {

    @Test
    public void test() {
        MovingAverage avg = new MovingAverage(1);
        assertEquals(Double.NaN, avg.getAverage(), 0.01);
        assertEquals(0, avg.getFirst());
        assertEquals(0, avg.getSum());
        assertEquals(0, avg.getFill());

        avg.add(1);
        assertEquals(1, avg.getAverage(), 0.01);
        assertEquals(1, avg.getFirst());
        assertEquals(1, avg.getSum());
        assertEquals(1, avg.getFill());

        avg.add(2);
        assertEquals(2, avg.getAverage(), 0.01);
        assertEquals(2, avg.getFirst());
        assertEquals(2, avg.getSum());
        assertEquals(1, avg.getFill());

        avg = new MovingAverage(2);
        avg.add(2);
        assertEquals(2, avg.getFirst());
        assertEquals(2, avg.getAverage(), 0.01);
        avg.add(3);
        assertEquals(2, avg.getFirst());
        assertEquals(2.5, avg.getAverage(), 0.01);
        avg.add(4);
        assertEquals(3, avg.getFirst());
        assertEquals(3.5, avg.getAverage(), 0.01);
        avg.add(4);
        assertEquals(4, avg.getFirst());
        assertEquals(4, avg.getAverage(), 0.01);
        assertEquals(8, avg.getSum());
        assertEquals(2, avg.getFill());

        avg = new MovingAverage(5);
        avg.add(2);
        assertEquals(2, avg.getFirst());
        assertEquals(2, avg.getAverage(), 0.01);
        avg.add(3);
        assertEquals(2, avg.getFirst());
        assertEquals(2.5, avg.getAverage(), 0.01);
        avg.add(4);
        assertEquals(2, avg.getFirst());
        assertEquals(3, avg.getAverage(), 0.01);
        avg.add(5);
        assertEquals(2, avg.getFirst());
        assertEquals(3.5, avg.getAverage(), 0.01);
        avg.add(6);
        assertEquals(2, avg.getFirst());
        assertEquals(4, avg.getAverage(), 0.01);
        avg.add(7);
        assertEquals(3, avg.getFirst());
        assertEquals(5, avg.getAverage(), 0.01);
        assertEquals(25, avg.getSum());
        assertEquals(5, avg.getFill());
    }

    @Test
    public void testGetLastAndWindow() {
        MovingAverage avg = new MovingAverage(3);
        assertEquals(0, avg.getLast());
        assertArrayEquals(new long[0], avg.getWindow());

        avg.add(1);
        assertArrayEquals(new long[] {1}, avg.getWindow());
        assertEquals(1, avg.getLast());
        avg.add(2);
        assertArrayEquals(new long[] {1, 2}, avg.getWindow());
        assertEquals(2, avg.getLast());
        avg.add(3);
        assertArrayEquals(new long[] {1, 2, 3}, avg.getWindow());
        assertEquals(3, avg.getLast());
        avg.add(4);
        assertArrayEquals(new long[] {2, 3, 4}, avg.getWindow());
        assertEquals(4, avg.getLast());
        avg.add(5);
        assertArrayEquals(new long[] {3, 4, 5}, avg.getWindow());
        assertEquals(5, avg.getLast());
        avg.add(6);
        assertArrayEquals(new long[] {4, 5, 6}, avg.getWindow());
        assertEquals(6, avg.getLast());
        avg.add(7);
        assertArrayEquals(new long[] {5, 6, 7}, avg.getWindow());
        assertEquals(7, avg.getLast());
        avg.add(8);
        assertArrayEquals(new long[] {6, 7, 8}, avg.getWindow());
        assertEquals(8, avg.getLast());
    }
}
