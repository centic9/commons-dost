package org.dstadler.commons.collections;

/**
 * Some general helpers around collections of
 * objects.
 */
public class CollectionUtils {
    // private constructor to prevent instantiation of this class
    private CollectionUtils() {
    }

    /**
     * Summarizes the collection of numbers by replacing
     * consecutive values as ranges, e.g. a list of 1,2,3,4,6,9,10,11
     * would result in "1-4,6,9-11".
     *
     * An empty list leads to the empty string.
     *
     * Single occurrences and two consecutive numbers are not combined,
     * three or more consecutive numbers are combined.
     *
     * Note: The collection is expected to be sorted in the order in
     * which the values should be listed.
     *
     * For numbers with fractional parts, only the result of longValue()
     * is printed.
     *
     * @param numbers The list of numbers
     * @param <T> A type of integer numbers
     * @return A string representing the contents of the collection in a condensed way.
     */
    public static <T extends Number> String getCombinedText(Iterable<T> numbers) {
        StringBuilder text = new StringBuilder();
        long prev = -1;
        long start = -1;

        for(T nr : numbers) {
            // check if the streak continues
            if(prev != nr.longValue() - 1) {
                // do not detect a streak just on first iteration
                if(prev != -1) {
                    appendStreak(text, prev, start);
                }

                // new streak starts
                start = nr.longValue();
            }
            prev = nr.longValue();
        }

        // add the last range/nr
        if(prev != -1) {
            appendStreak(text, prev, start);
        }

        // cut away trailing comma
        if(text.length() > 0) {
            text.setLength(text.length()-1);
        }

        return text.toString();
    }

    private static void appendStreak(StringBuilder text, long prev, long start) {
        // print information about this streak
        if(start == prev) {
            // one only
            text.append(start).append(",");
        } else if(start == prev-1) {
            // two, still to less to print arrange
            text.append(start).append(",").append(prev).append(",");
        } else {
            // more than two should be displayed as range
            text.append(start).append("-").append(prev).append(",");
        }
    }

}
