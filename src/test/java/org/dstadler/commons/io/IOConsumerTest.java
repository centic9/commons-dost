package org.dstadler.commons.io;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class IOConsumerTest {
    @Test
    public void test() {
        AtomicBoolean called = new AtomicBoolean();

        IOConsumer<String> consumer = new IOConsumer<String>() {
            @Override
            public void acceptWithException(String entity) {
                called.set(true);
            }
        };

        consumer.accept("test");
    }

    @Test(expected = IllegalStateException.class)
    public void testWithException() {
        IOConsumer<String> consumer = new IOConsumer<String>() {
            @Override
            public void acceptWithException(String entity) throws IOException {
                throw new IOException("test");
            }
        };

        consumer.accept("test");
    }
}