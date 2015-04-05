package org.dstadler.commons.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.logging.Logger;

/**
 * Various utilities related to sockets.
 *
 * @author dominik.stadler
 *
 */
public final class SocketUtils {
	private static final Logger log = Logger.getLogger(SocketUtils.class.getName());

	// private constructor to prevent instantiation
	private SocketUtils() {
	}

	/**
	 * Method that is used to find the next available port. It used the two constants PORT_RANGE_START and
	 * PORT_RANGE_END defined above to limit the range of ports that are tried.
	 *
	 * @param portRangeStart The first port that is tried
	 * @param portRangeEnd The last port that is tried
	 *
	 * @return A port number that can be used.
	 * @throws IOException
	 *             If no available port is found.
	 */
	public static int getNextFreePort(int portRangeStart, int portRangeEnd) throws IOException {
		for (int port = portRangeStart; port <= portRangeEnd; port++) {
			try (ServerSocket sock = new ServerSocket()) {
				sock.setReuseAddress(true);
				sock.bind(new InetSocketAddress(port));

				return port;
			} catch (IOException e) {
				// seems to be taken, try next one
				log.warning("Port " + port + " seems to be used already, trying next one...");
			}
		}

		throw new IOException("No free port found in the range of [" + portRangeStart + " - " + portRangeEnd + "]");
	}
}
