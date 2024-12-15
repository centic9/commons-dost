package org.dstadler.commons.net;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.logging.Logger;

import org.apache.commons.lang3.SystemUtils;

import org.dstadler.commons.testing.PrivateConstructorCoverage;
import org.dstadler.commons.testing.TestHelpers;
import org.junit.jupiter.api.Test;

public class SocketUtilsTest {
	private static final Logger log = Logger.getLogger(SocketUtils.class.getName());

	private static final int NUMBER_OF_SOCKETS = 10;

	@Test
	public void testGetNextFreePort() throws Exception {
		// I can get a free port
		int port = SocketUtils.getNextFreePort(8000, 9000);
		assertTrue(port >= 8000 && port <= 9000, "Port is out of range");

		// I can do this many times as I do not really "reserve" it
		for(int i = 0;i < 3*NUMBER_OF_SOCKETS;i++) {
			port = SocketUtils.getNextFreePort(8000, 9000);
			assertTrue(port >= 8000 && port <= 9000, "Port is out of range");
		}

		ServerSocket[] sock = new ServerSocket[NUMBER_OF_SOCKETS];
		for(int i = 0;i < NUMBER_OF_SOCKETS;i++) {
			try {
				port = SocketUtils.getNextFreePort(29000, 29009);
			} catch (IOException e) {
				throw new IOException("While reserving port number " + i, e);
			}
			sock[i] = openSocket(port);
			log.info("Using port: " + port);
		}

		// now retrieving another port should fail
		try {
			SocketUtils.getNextFreePort(29000, 29009);
			fail("Should throw Exception here");
		} catch (IOException e) {
			TestHelpers.assertContains(e, "29000", "29009", "No free port");
		}

		// free up sockets again
		for(ServerSocket socket : sock) {
			socket.close();
		}
	}

	private ServerSocket openSocket(int port) throws IOException {
		ServerSocket sock = new ServerSocket();

		// for some strange reason this does not work on Windows!
		if(!SystemUtils.IS_OS_WINDOWS) {
			sock.setReuseAddress(true);
		}

		sock.bind(new InetSocketAddress((InetAddress)null, port));

		return sock;
	}

	 // helper method to get coverage of the unused constructor
	 @Test
	 public void testPrivateConstructor() throws Exception {
	 	PrivateConstructorCoverage.executePrivateConstructor(SocketUtils.class);
	 }
}
