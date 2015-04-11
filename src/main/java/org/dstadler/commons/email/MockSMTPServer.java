package org.dstadler.commons.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dstadler.commons.net.SocketUtils;

import com.dumbster.smtp.SmtpMessage;

/**
 * Simple SMTP server that can be used for in-process email sending in unit tests.
 *
 * Usage is as simple as:
 *
 * <code>
 MockSMTpServer server = new MockSMTPServer();

 server.start();

 ... send email, use server.getPort() to retrieve the automatically chosen SMTP port  ...

 assertEquals(expectedcount, server.getMessageCount());

 Iterator iterator = server.getMessages();

 </code>
 *
 * SMTP Port is automatically chosen in the range of {@value #PORT_RANGE_START} and {@value #PORT_RANGE_END}.
 *
 * @author dominik.stadler
 *
 */
public class MockSMTPServer {
	// The range of ports that we try to use for the listening.
	private static final int PORT_RANGE_START = 15110;
	private static final int PORT_RANGE_END = 15119;

	private SafeCloseSmtpServer server;

	private int port = -1;

	/**
	 * Start the server, port is chosen automatically in the range of {@value #PORT_RANGE_START} and
	 * {@value #PORT_RANGE_END}.
	 *
	 * @throws IOException
	 */
	public void start() throws IOException {
		// try to automatically retrieve a port
		port = SocketUtils.getNextFreePort(PORT_RANGE_START, PORT_RANGE_END);

		server = SafeCloseSmtpServer.start(port);
	}

	/**
	 * Returns if the server is currently running, i.e. start() was called and stop() was not yet called.
	 *
	 * @return True, if the server is currently running, false otherwise.
	 */
	public boolean isRunning() {
		return server != null && !server.isStopped();
	}

	/**
	 * Stops the server from accepting emails.
	 */
	public void stop() {
		port = -1;

		server.stop();
	}

	/**
	 * The SMTP Port that the server uses for receiving email.
	 *
	 * Note: this is only available after {@link #start()} is called and {@link #stop()} is not yet called.
	 *
	 * The port is chosen automatically in the range of {@value #PORT_RANGE_START} and {@value #PORT_RANGE_END}.
	 *
	 * @return The server-port that is used by this instance.
	 */
	public int getPort() {
		return port;
	}


	/**
	 * Returns the number of messages that were received by this server.
	 *
	 * Note: calling start() again on this instance resets this counter.
	 *
	 * @return The number of messages that were received by this server.
	 */
	public int getMessageCount() {
		return server.getReceivedEmailSize();
	}

	/**
	 * Returns an iterator of messages that were received by this server.
	 *
	 * The iterator is of type SmtpMessage from the dumbster-jar file.
	 *
	 * Note: calling start() again on this instance resets this list.
	 *
	 * @return An iterator of the messages that were received.
	 */
	public Iterator<String> getMessages() {
		Iterator<SmtpMessage> it = server.getReceivedEmail();

		List<String> msgs = new ArrayList<>();
		while(it.hasNext()) {
			SmtpMessage msg = it.next();
			msgs.add(msg.toString());
		}

		return msgs.iterator();
	}
}
