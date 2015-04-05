package org.dstadler.commons.testing;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.logging.Logger;

import org.dstadler.commons.http.NanoHTTPD;


/**
 * Simple REST Webserver that can be used to mock REST responses to client-code tests.
 *
 * Use it as follows
 *
 * <code>
 try (MockRESTServer server = new MockRESTServer()) {

     // get the actually used port to use in the client code
     server.getPort();

     // set the response that you want the server to send back
     server.setResponse("whatever you want to return");

     ..
 }
 </code>
 *
 * @author dominik.stadler
 *
 */
public class MockRESTServer implements Closeable {
	private static final Logger log = Logger.getLogger(MockRESTServer.class.getName());

	// The range of ports that we try to use for the listening.
	private static final int PORT_RANGE_START = 15100;
	private static final int PORT_RANGE_END = 15110;

	NanoHTTPD httpd;
	int port;

	/**
	 * Create a mock server that responds to REST requests.
	 *
	 * The server tries ports in the range listed above to find one that can be used. If none is usable, a IOException
	 * is thrown.
	 *
	 * @throws IOException
	 *             If instantiating the Server failed.
	 */
	public MockRESTServer(final String status, final String mime, final String msg) throws IOException {
		// first try to get the next free port
		port = getNextFreePort();

		httpd = new NanoHTTPD(port) {
			/**
			 * Internal method to provide the response that is set.
			 */
			@Override
			public Response serve(String uri, String method, Properties header, Properties parms) {
				return new NanoHTTPD.Response(status, mime, msg);
			}
		};
	}

	/**
	 * Create a mock server that responds to REST requests.
	 *
	 * The server tries ports in the range listed above to find one that can be used. If none is usable, a IOException
	 * is thrown.
	 *
	 * @throws IOException
	 *             If instantiating the Server failed.
	 */
	public MockRESTServer(final Runnable response, final String status, final String mime, final String msg) throws IOException {
		// first try to get the next free port
		port = getNextFreePort();

		httpd = new NanoHTTPD(port) {
			/**
			 * Internal method to run the provided Runnable
			 */
			@Override
			public Response serve(String uri, String method, Properties header, Properties parms) {
				response.run();
				return new NanoHTTPD.Response(status, mime, msg);
			}
		};
	}

	/**
	 * Method that is used to find the next available port. It uses the two constants PORT_RANGE_START and
	 * PORT_RANGE_END defined above to limit the range of ports that are tried.
	 *
	 * @return A port number that can be used.
	 * @throws IOException
	 *             If no available port is found.
	 */
	private static final int getNextFreePort() throws IOException {
		for (int port = PORT_RANGE_START; port < PORT_RANGE_END; port++) {
			ServerSocket sock;
			try {
				sock = new ServerSocket(port);
				sock.close();
				//
				return port;
			} catch (IOException e) {
				// seems to be taken, try next one
				log.warning("Port " + port + " seems to be used already, trying next one...");
			}
		}

		throw new IOException("No free port found in the range of [" + PORT_RANGE_START + " - " + PORT_RANGE_END + "]");
	}

	public int getPort() {
		return port;
	}

	@Deprecated
	public void stop() {
		try {
			close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void close() throws IOException {
		httpd.stop();
	}
}
