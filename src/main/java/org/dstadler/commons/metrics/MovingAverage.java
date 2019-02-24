package org.dstadler.commons.metrics;


/**
 * Class to sum up values over a sliding window and allow to retrieve the current average.
 *
 * During construction, the size of the sliding window is defined as number of slots.
 *
 * Can also be used to determine the value at the start-time of the window, e.g. if the stored
 * value is the timestamp, the method getStart() will return the time it took for the number
 * of slots to be taken.
 *
 * Taken from https://codereview.stackexchange.com/questions/127542/calculating-running-average-in-java
 *
 * This class is not thread-safe!
 */
public class MovingAverage {
    private final long[] window;
    private long sum = 0;
    private int fill;
    private int position;


    public MovingAverage(int size) {
        checkArgument(size > 0,
                "Must have a valid window size, but had %s", size);

        this.window = new long[size];
    }

    public void add(long number) {
        if (fill == window.length) {
            sum -= window[position];
        } else {
            fill++;
        }

        sum += number;
        window[position++] = number;

        if (position == window.length) {
            position = 0;
        }

    }

    public double getAverage() {
        return ((double)sum) / fill;
    }

    public long getFirst() {
        // already filled up?
        if(fill == 0) {
            return 0;
        } else if(fill == window.length) {
            return window[position];
        } else {
            return window[0];
        }
    }

    public long getSum() {
        return sum;
    }

    public int getFill() {
        return fill;
    }

    public long getLast() {
        // return zero if there is no element stored yet
        if(fill == 0) {
            return 0;
        }

        // if position is at the beginning, return last element
        if((position) == 0) {
            return window[fill-1];
        }

        return window[position-1];
    }

    public long[] getWindow() {
        if(fill == 0) {
            return new long[0];
        }

        long[] ret = new long[fill];
        if(fill != window.length || position == 0) {
            // not yet filled up or we did just complete a roundtrip in the
            // ring-buffer: we can simply copy the array
            System.arraycopy(window, 0, ret, 0, fill);
            return ret;
        }

        // we need to first copy the part up to the end and then from 0 up to
        // before the current position
        System.arraycopy(window, position, ret, 0, fill - position);
        System.arraycopy(window, 0, ret, fill - position, position);

        return ret;
    }

    // copy of Guava to avoid including Guava in this core library
    private void checkArgument(boolean argument, String msg, Object... args) {
        if (!argument) {
            throw new IllegalArgumentException(String.format(msg, args));
        }
    }
}
