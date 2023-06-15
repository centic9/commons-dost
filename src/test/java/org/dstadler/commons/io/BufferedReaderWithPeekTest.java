package org.dstadler.commons.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class BufferedReaderWithPeekTest {
	@Test
	public void testReader() throws IOException {
		try (BufferedReaderWithPeek reader = new BufferedReaderWithPeek("src/test/resources/logging.properties")) {
			assertEquals("handlers=java.util.logging.ConsoleHandler", reader.peekLine());
			assertEquals("handlers=java.util.logging.ConsoleHandler", reader.peekLine());

			assertEquals("handlers=java.util.logging.ConsoleHandler", reader.readLine());
			assertEquals("java.util.logging.ConsoleHandler.level=ALL", reader.readLine());

			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
			}

			assertNull(reader.peekLine());
			assertNull(reader.readLine());
		}
	}

	@Test
	public void testReaderConstructor() throws IOException {
		try (BufferedReaderWithPeek reader = new BufferedReaderWithPeek(new BufferedReader(new StringReader("test\ntest2")))) {
			assertEquals("test", reader.peekLine());
			assertEquals("test", reader.peekLine());

			assertEquals("test", reader.readLine());
			assertEquals("test2", reader.readLine());

			assertNull(reader.peekLine());
			assertNull(reader.readLine());
		}
	}

	@Test
	public void testClose() throws IOException {
		AtomicBoolean closed = new AtomicBoolean();

		final BufferedReader delegate = new BufferedReader(new StringReader("test\ntest2")) {

			@Override
			public void close() throws IOException {
				super.close();

				closed.set(true);
			}
		};

		try (BufferedReaderWithPeek reader = new BufferedReaderWithPeek(delegate)) {
			assertEquals("test", reader.peekLine());
			assertEquals("test", reader.peekLine());

			assertEquals("test", reader.readLine());
			assertEquals("test2", reader.readLine());

			assertNull(reader.peekLine());
			assertNull(reader.readLine());
		}

		assertTrue(closed.get());
	}

	@Test
	public void testReaderInvalidFile() {
		//noinspection resource
		assertThrows(FileNotFoundException.class, () -> new BufferedReaderWithPeek("invalid-file"));
	}
}