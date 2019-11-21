package org.dstadler.commons.io;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;

public class AllFilesDirectoryWalkerTest {

    @Test
    public void test() throws IOException {
        ArrayList<File> files = new ArrayList<>();
        AllFilesDirectoryWalker walker = new AllFilesDirectoryWalker(files);
        walker.walk(new File("."));

        assertFalse(files.isEmpty());
    }
}
