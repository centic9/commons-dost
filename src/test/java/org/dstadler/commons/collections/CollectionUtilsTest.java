package org.dstadler.commons.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.junit.jupiter.api.Test;

public class CollectionUtilsTest {

    @Test
    public void testGetCombinedText() {
        assertEquals("", CollectionUtils.getCombinedText(Collections.emptyList()));
        assertEquals("1", CollectionUtils.getCombinedText(Collections.singleton(1)));

        List<Integer> ints = new ArrayList<>();
        ints.add(2);
        ints.add(3);
        assertEquals("2,3", CollectionUtils.getCombinedText(ints),
				"Two items are not combined yet");

        ints.add(4);
        assertEquals("2-4", CollectionUtils.getCombinedText(ints),
				"Now they are combined");

        ints.add(3);
        assertEquals("2-4,3", CollectionUtils.getCombinedText(ints));

        ints.add(99);
        assertEquals("2-4,3,99", CollectionUtils.getCombinedText(ints));

        List<Long> longs = new ArrayList<>();
        for(long l = 1023L;l < 3228L;l++) {
            longs.add(l);
        }
        assertEquals("1023-3227", CollectionUtils.getCombinedText(longs));

        longs.addFirst(832L);
        assertEquals("832,1023-3227", CollectionUtils.getCombinedText(longs));

        longs.clear();
        longs.add(0L);
        longs.add(1L);
        longs.add(2L);
        assertEquals("0-2", CollectionUtils.getCombinedText(longs));

        // initial MIN_VALUE is not supported due to implementation
        longs.clear();
        longs.add(Long.MIN_VALUE);
        assertEquals("", CollectionUtils.getCombinedText(longs));
    }

    // helper method to get coverage of the unused constructor
    @Test
    public void testPrivateConstructor() throws Exception {
        PrivateConstructorCoverage.executePrivateConstructor(CollectionUtils.class);
    }
}
