package org.dstadler.commons.io;

import org.apache.commons.io.DirectoryWalker;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Very simple directory walker which just returns a list of all found files in
 * all sub-directories.
 */
public class AllFilesDirectoryWalker extends DirectoryWalker<File> {
    private final Collection<File> files;

    public AllFilesDirectoryWalker(Collection<File> files) {
        this.files = files;
    }

    public void walk(File startDirectory) throws IOException {
        walk(startDirectory, files);
    }

    protected void handleFile(File file, int depth, Collection<File> results) {
        files.add(file);
    }
}
