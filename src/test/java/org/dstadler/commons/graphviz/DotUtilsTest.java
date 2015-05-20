package org.dstadler.commons.graphviz;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class DotUtilsTest {
	private static final String DOT_FILE = "digraph G {" +
		"dpi=300;\n" +
		"rankdir=LR;\n" +
		"node [shape=box];\n" +
		"\n" +
		"\"agent15\"->\"c.d.d.agent\";\n" +
		"\"com.dynatrace.adk\"->\"c.d.d.agent\";\n" +
		"\"c.d.d.adm\"->\"test.util.nonprod\";\n" +
		"\"c.d.d.adm\"->\"c.d.d.sdk\";\n" +
		"\"c.d.d.adm\"->\"c.d.d.portlets\";\n" +
		"\"c.d.d.adm\"->\"c.d.d.server.shared\";\n" +
		"\"c.d.d.adm\"->\"c.d.d.util\";\n" +
		"}\n";

	@Before
	public void setUp() throws IOException {
		boolean exists = new File(DotUtils.DOT_EXE).exists();

		assertEquals(exists, DotUtils.checkDot());
		Assume.assumeTrue("Did not find dot executable at " + DotUtils.DOT_EXE, exists);
	}

	@Test
	public void testRenderGraph() throws Exception {
		File file = File.createTempFile("DotUtilsTest", ".dot");
		try {
			FileUtils.write(file, DOT_FILE);

			File retfile = DotUtils.renderGraph(file);
			assertNotNull(retfile);
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testRenderGraphInvalidFile() throws Exception {
		File file = File.createTempFile("DotUtilsTest", ".dot");
		try {
			FileUtils.write(file, "{ digraph");

			try {
				DotUtils.renderGraph(file);
				
				// on linux dot does not set the exit code to 1 here...
				if(SystemUtils.IS_OS_WINDOWS) {
					fail("Should catch exception");
				}
			} catch (IOException e) {
				// expected here
			}
		} finally {
			assertTrue(file.delete());
		}
	}
}
