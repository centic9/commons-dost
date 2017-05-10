package org.dstadler.commons.os;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * Small helper class that can be used to set the console title
 * on Windows.
 *
 * @author dominik.stadler
 *
 */
public class WindowTitle {
	/**
	 *  http://en.wikipedia.org/wiki/Java_Native_Access
	 *  https://jna.dev.java.net/javadoc/overview-summary.html
	 *
	 */
    public interface Kernel32 extends Library {
        /* http://msdn.microsoft.com/en-us/library/ms686050%28VS.85%29.aspx
         *
         *
        BOOL WINAPI SetConsoleTitle(
        		  __in  LPCTSTR lpConsoleTitle
        		);*/
    	boolean SetConsoleTitleA(String title);

    }

    public static void setConsoleTitle(String title) {
    	if(!Platform.isWindows()) {
    		return;
    	}

     	Kernel32 lib = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
     	lib.SetConsoleTitleA(title);
    }
}
