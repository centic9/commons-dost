package org.dstadler.commons.io;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class AllFilesDirectoryWalkerTest {

    @Test
    public void test() throws IOException {
        ArrayList<File> files = new ArrayList<>();
        AllFilesDirectoryWalker walker = new AllFilesDirectoryWalker(files);
        walker.walk(new File("."));

        assertFalse(files.isEmpty());
    }
}
