package org.dstadler.commons.graphviz;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class DotUtilsTest {
	private static final String DOT_FILE = """
		digraph G {\
		dpi=300;
		rankdir=LR;
		node [shape=box];
		
		"agent15"->"c.d.d.agent";
		"com.dynatrace.adk"->"c.d.d.agent";
		"c.d.d.adm"->"test.util.nonprod";
		"c.d.d.adm"->"c.d.d.sdk";
		"c.d.d.adm"->"c.d.d.portlets";
		"c.d.d.adm"->"c.d.d.server.shared";
		"c.d.d.adm"->"c.d.d.util";
		}
		""";

	@BeforeEach
	public void setUp() {
		boolean exists = new File(DotUtils.DOT_EXE).exists();

		try {
			assertEquals(exists, DotUtils.checkDot());
			Assumptions.assumeTrue(exists, "Did not find dot executable at " + DotUtils.DOT_EXE);
		} catch (IOException e) {
			Assumptions.assumeTrue(exists, "Did not find dot executable at " + DotUtils.DOT_EXE + ": " + e);
		}

	}

	@Test
	public void testRenderGraph() throws Exception {
		File file = File.createTempFile("DotUtilsTest", ".dot");
		try {
			FileUtils.write(file, DOT_FILE, "UTF-8");

			File out = File.createTempFile("DotUtilsTest", ".png");
			try {
				assertTrue(out.delete());

				DotUtils.renderGraph(file, out);

				assertNotNull(out);
				assertTrue(out.exists());
				assertTrue(out.length() > 0);
			} finally {
				assertTrue(out.delete());
			}
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testRenderGraphInvalidFile() throws Exception {
		File file = File.createTempFile("DotUtilsTest", ".dot");
		try {
			FileUtils.write(file, "{ digraph", "UTF-8");

			try {
				File out = File.createTempFile("DotUtilsTest", ".png");
				try {
					assertTrue(out.delete());

					DotUtils.renderGraph(file, out);

					// on linux dot does not set the exit code to 1 here...
					if(SystemUtils.IS_OS_WINDOWS) {
						fail("Should catch exception");
					}
					assertNotNull(out);
					assertTrue(out.exists());
				} finally {
					assertTrue(!out.exists() || out.delete());
				}
			} catch (@SuppressWarnings("unused") IOException e) {
				// expected here
			}
		} finally {
			assertTrue(file.delete());
		}
	}

	@Test
	public void testWriteHeaderFooter() throws IOException {
		try (StringWriter writer = new StringWriter()) {
			DotUtils.writeHeader(writer, 350, "here", "version", Collections.singletonList("attribline"));

			DotUtils.writeFooter(writer);

			TestHelpers.assertContains(writer.toString(), "350", "here", "version", "attribline");
		}
	}

	@Test
	public void testWriteHeaderFooterEmptyLine() throws IOException {
		try (StringWriter writer = new StringWriter()) {
			DotUtils.writeHeader(writer, 350, null, "version", null);

			DotUtils.writeFooter(writer);

			TestHelpers.assertContains(writer.toString(), "350", "version");
		}
	}


    @Test
    public void testWriteHeaderContent() throws Exception {
        File temp = File.createTempFile("dot_header_test", ".txt");

        try {
	        // Basic header
	        try (FileWriter fileWriter = new FileWriter(temp, false)) {
	            DotUtils.writeHeader(fileWriter, 0, null, "G", null);
	            fileWriter.flush();
	            assertEquals(FileUtils.readFileToString(temp, "UTF-8"),
							"""
                             digraph G {
                             rankdir=LR;
                             node [shape=box];
	                            
                             """);
	        }

	        // DPI set
	        try (FileWriter fileWriter = new FileWriter(temp, false)) {
	            DotUtils.writeHeader(fileWriter, 200, null, "G", null);
	            fileWriter.flush();
	            assertEquals(FileUtils.readFileToString(temp, "UTF-8"),
							"""
                             digraph G {
                             dpi=200;
                             rankdir=LR;
                             node [shape=box];
	                            
                             """);
	        }

	        // Another rankdir
	        try (FileWriter fileWriter = new FileWriter(temp, false)) {
	            DotUtils.writeHeader(fileWriter, 0, "AB", "G", null);
	            fileWriter.flush();
	            assertEquals(FileUtils.readFileToString(temp, "UTF-8"),
							"""
                             digraph G {
                             rankdir=AB;
                             node [shape=box];
	                            
                             """);
	        }

	        // Extra lines
	        try (FileWriter fileWriter = new FileWriter(temp, false)) {
	            List<String> lines = new ArrayList<>();
	            lines.add("someline");
	            lines.add("someotherline;");

	            DotUtils.writeHeader(fileWriter, 0, null, "G", lines);
	            fileWriter.flush();
	            assertEquals(FileUtils.readFileToString(temp, "UTF-8"),
							"""
                             digraph G {
                             rankdir=LR;
                             someline;
                             someotherline;
                             node [shape=box];
	                            
                             """);
	        }

	        // different title
	        try (FileWriter fileWriter = new FileWriter(temp, false)) {
	            DotUtils.writeHeader(fileWriter, 0, null, "mygraph", null);
	            fileWriter.flush();
	            assertEquals(FileUtils.readFileToString(temp, "UTF-8"),
							"""
                             digraph mygraph {
                             rankdir=LR;
                             node [shape=box];
	                            
                             """);
	        }
        } finally {
        	assertTrue(temp.delete());
        }
    }

	// helper method to get coverage of the unused constructor
	@Test
	public void testPrivateConstructor() throws Exception {
		org.dstadler.commons.testing.PrivateConstructorCoverage.executePrivateConstructor(DotUtils.class);
	}

	@Test
	public void testInvalidDotExe() throws IOException {
		String previousExe = DotUtils.DOT_EXE;
		try {
			DotUtils.setDotExe("SomeInvalidBinaryfile");

			File out = File.createTempFile("DotUtilsTest", ".png");
			assertTrue(out.delete());

			try {
				DotUtils.renderGraph(new File("Some nonexistingfile"), out);
			} catch (IOException e) {
				TestHelpers.assertContains(e, "SomeInvalidBinaryfile");
			}

			assertFalse(out.exists());
		} finally {
			DotUtils.setDotExe(previousExe);
		}
	}

	@Test
	public void testInvalidInput() throws IOException {
		File out = File.createTempFile("DotUtilsTest", ".png");
		assertTrue(out.delete());

		try {
			DotUtils.renderGraph(new File("SomeNonexistingfile"), out);
		} catch (IOException e) {
			// expected here, error is operating system specific
		}

		assertFalse(out.exists());
	}
}
