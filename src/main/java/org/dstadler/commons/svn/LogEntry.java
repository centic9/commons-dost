package org.dstadler.commons.svn;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Pair;

/*
 * Data object for one SVN changelog
 *
 * @author dominik.stadler
 */
public class LogEntry {
	public final static String MORE = "...";

	public long revision = 0;
	public String author;
	public String date;
	public String msg;
	public SortedSet<Pair<String, String>> paths;

	@Override
	public String toString() {
		// without msg currently...
		return "LogEntry [revision=" + revision + ", author=" + author + ", date=" + date + "]";
	}

	public void addPath(String path, String action) {
		if(paths == null) {
			paths = new TreeSet<>(new Comparator<Pair<String,String>>() {

				@Override
				public int compare(Pair<String,String> o1, Pair<String,String> o2) {
					// sort "..." at the end, otherwise sort alphabetically
					String path1 = o1.getLeft();
					if(path1 != null && path1.equals(MORE)) {
						return 1;
					}
					String path2 = o2.getLeft();
					if(path2 != null && path2.equals(MORE)) {
						return -1;
					}

					return o1.compareTo(o2);
				}
			});
		}
		paths.add(Pair.of(path, action));
	}
}
