package org.dstadler.commons.logging.jdk;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.Test;

public class LoggerFactoryTest {

	/**
	 * Test method for {@link org.dstadler.commons.logging.jdk.LoggerFactory#make()}.
	 */
	@Test
	public void testMake() {
		assertNotNull(LoggerFactory.make());
	}

	@Test
	public void testInitLogging() throws IOException {
		Thread.currentThread().setContextClassLoader(new ClassLoader() {
			@Override
			public URL getResource(String name) {
				return null;
			}
		});

		try {
			LoggerFactory.initLogging();
			fail("Should throw Exception, seems we found a resource: " + Thread.currentThread().getContextClassLoader().getResource("logging.properties"));
		} catch (IOException e) {
			// expected if we do not have a logging.properties here
			assertNotNull(e);
		}

		try (URLClassLoader cl = new URLClassLoader(new URL[] {new File("src/test/resources").toURI().toURL()},
				Thread.currentThread().getContextClassLoader())) {
			Thread.currentThread().setContextClassLoader(cl);

			// now it should work
			LoggerFactory.initLogging();
		}
	}

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		PrivateConstructorCoverage.executePrivateConstructor(LoggerFactory.class);
	}

	@Test
	public void testRolloverLogfile() throws SecurityException, IOException {
	    LoggerFactory.rolloverLogfile();

        for(File file : findLogFiles()) {
            assertTrue("Could not remove logfile: " + file, file.delete());
        }

        assertEquals(0, findLogFiles().length);

        Logger log = Logger.getLogger("");    // NOSONAR - local logger used on purpose here
        FileHandler handler = new FileHandler("build/LoggerFactoryTest.%g.%u.log", 100*1024*1024, 10);
        try {
            log.addHandler(handler);

            Logger logger = LoggerFactory.make();
            logger.info("testlog1");

            LoggerFactory.rolloverLogfile();

            logger.info("testlog2");

            LoggerFactory.rolloverLogfile();

            logger.info("testlog2");

            File[] logFiles = findLogFiles();
            assertEquals(3, logFiles.length);
        } finally {
        	handler.close();
            log.removeHandler(handler);
        }
	}

    @Test
    public void testRolloverLogfileException() throws SecurityException, IOException {
        LoggerFactory.rolloverLogfile();

        for(File file : findLogFiles()) {
            assertTrue("Could not remove logfile: " + file, file.delete());
        }

        assertEquals(0, findLogFiles().length);

        final AtomicBoolean shouldFail = new AtomicBoolean(false);

        Logger log = Logger.getLogger("");    // NOSONAR - local logger used on purpose here
        FileHandler handler = new FileHandler("build/LoggerFactoryTest.%g.%u.log", 100*1024*1024, 10) {
            @Override
            public synchronized void setLevel(Level newLevel) throws SecurityException {
                if(shouldFail.get()) {
                    throw new IllegalArgumentException("testexception");
                }

                super.setLevel(newLevel);
            }
        };

        try {
            log.addHandler(handler);

            Logger logger = LoggerFactory.make();
            logger.info("testlog1");

            LoggerFactory.rolloverLogfile();

            logger.info("testlog2");

            shouldFail.set(true);

            try {
                LoggerFactory.rolloverLogfile();
                fail("Should catch exception");
            } catch (IllegalStateException e) {
                TestHelpers.assertContains(ExceptionUtils.getStackTrace(e), "testexception");
            }
        } finally {
        	handler.close();
            log.removeHandler(handler);
        }
    }

	@Test
    public void testRolloverLogFileClosed() throws SecurityException, IOException {
        LoggerFactory.rolloverLogfile();

        for(File file : findLogFiles()) {
            assertTrue("Could not remove logfile: " + file, file.delete());
        }

        assertEquals(0, findLogFiles().length);

        Logger log = Logger.getLogger("");    // NOSONAR - local logger used on purpose here
        FileHandler handler = new FileHandler("build/LoggerFactoryTest.%g.%u.log", 100*1024*1024, 10);
        try {
            log.addHandler(handler);

            handler.setLevel(Level.OFF);

            Logger logger = LoggerFactory.make();
            logger.info("testlog1");

            LoggerFactory.rolloverLogfile();

            logger.info("testlog2");

            LoggerFactory.rolloverLogfile();

            logger.info("testlog2");

            File[] logFiles = findLogFiles();
            assertEquals(1, logFiles.length);
        } finally {
        	handler.close();
            log.removeHandler(handler);
        }
    }

    private File[] findLogFiles() {
        File dir = new File("build");
        return dir.listFiles((FileFilter)new AndFileFilter(
                new PrefixFileFilter("LoggerFactoryTest"),
                new SuffixFileFilter(".log")));
    }
}
