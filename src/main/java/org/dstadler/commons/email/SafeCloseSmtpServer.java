/*****************************************************
 * /*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Modified to handle closing better by:
 * @author: dominik.stadler
 *
 */

package org.dstadler.commons.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpActionType;
import com.dumbster.smtp.SmtpMessage;
import com.dumbster.smtp.SmtpRequest;
import com.dumbster.smtp.SmtpResponse;
import com.dumbster.smtp.SmtpState;

/**
 * Dummy SMTP server for testing purposes.
 *
 * Derived from SimpleSmtpServer, but enhanced so that closing
 * does not leave threads still doing work and thus failing to
 * get the same port on re-start().
 *
 * @author dominik.stadler
 *
 */
public final class SafeCloseSmtpServer implements Runnable {

	private static final Logger log = Logger.getLogger(SafeCloseSmtpServer.class.getName());

	// needed for proper close handling
	private static final int MAXIMUM_CONCURRENT_READERS = 100;

	/**
	 * Timeout listening on server socket.
	 */
	private static final int TIMEOUT = 500;

	private final CountDownLatch startupBarrier = new CountDownLatch(1);

	/**
	 * Stores all of the email received since this instance started up.
	 */
	private List<SmtpMessage> receivedMail;

	/**
	 * Indicates whether this server is stopped or not.
	 */
	private volatile boolean stopped = true;

	/**
	 * Handle to the server socket this server listens to.
	 */
	private ServerSocket serverSocket;

	/**
	 * Port the server listens on - set to the default SMTP port initially.
	 */
	private int port = SimpleSmtpServer.DEFAULT_SMTP_PORT;

	private Semaphore semaphore = new Semaphore(MAXIMUM_CONCURRENT_READERS);

	/**
	 * private Constructor to only create instances in the static start() method below.
	 *
	 * @param port port number
	 */
	private SafeCloseSmtpServer(int port) {
		receivedMail = new ArrayList<>();
		this.port = port;
	}

	/**
	 * Main loop of the SMTP server.
	 */
	@Override
	public void run() {
		stopped = false;
		try {
			try {
				serverSocket = new ServerSocket(port);
				serverSocket.setSoTimeout(TIMEOUT); // Block for maximum of 1.5 seconds
			} finally {
				// Notify when server socket has been created
				startupBarrier.countDown();
			}

			// Server: loop until stopped
			while (!isStopped()) {
				// get a semaphore so we can ensure below that no thread is still doing stuff when we want to close the server
				if (!semaphore.tryAcquire()) {
					throw new IllegalStateException("Could not get semaphore, number of possible threads is too low.");
				}

				try {
					// Start server socket and listen for client connections
					Socket socket = null;
					try {
						socket = serverSocket.accept();
					} catch (Exception e) {
						continue; // Non-blocking socket timeout occurred: try accept() again
					}

					// Get the input and output streams
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));         // NOSONAR - test class works only locally anyway
					PrintWriter out = new PrintWriter(socket.getOutputStream());       // NOSONAR - test class works only locally anyway

					synchronized (this) {
						/*
						 * We synchronize over the handle method and the list update because the client call completes inside
						 * the handle method and we have to prevent the client from reading the list until we've updated it.
						 * For higher concurrency, we could just change handle to return void and update the list inside the
						 * method
						 * to limit the duration that we hold the lock.
						 */
						List<SmtpMessage> msgs = handleTransaction(out, input);
						receivedMail.addAll(msgs);
					}
					socket.close();
				} finally {
					semaphore.release();
				}
			}
		} catch (Exception e) {
			/** @todo Should throw an appropriate exception here. */
			log.log(Level.SEVERE, "Caught exception: ", e);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					log.log(Level.SEVERE, "Caught exception: ", e);
				}
			}
		}
	}

	/**
	 * Check if the server has been placed in a stopped state. Allows another thread to
	 * stop the server safely.
	 *
	 * @return true if the server has been sent a stop signal, false otherwise
	 */
	public synchronized boolean isStopped() {
		return stopped;
	}

	/**
	 * Stops the server. Server is shutdown after processing of the current request is complete.
	 */
	public synchronized void stop() {
		// Mark us closed
		stopped = true;
		try {
			// Kick the server accept loop
			serverSocket.close();

			// acquire all semaphores so that we wait for all connections to finish before we report back as closed
			semaphore.acquireUninterruptibly(MAXIMUM_CONCURRENT_READERS);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Caught exception: ", e);
		}
	}

	/**
	 * Handle an SMTP transaction, i.e. all activity between initial connect and QUIT command.
	 *
	 * @param out output stream
	 * @param input input stream
	 * @return List of SmtpMessage
	 * @throws IOException
	 */
	private List<SmtpMessage> handleTransaction(PrintWriter out, BufferedReader input) throws IOException {
		// Initialize the state machine
		SmtpState smtpState = SmtpState.CONNECT;
		SmtpRequest smtpRequest = new SmtpRequest(SmtpActionType.CONNECT, "", smtpState);

		// Execute the connection request
		SmtpResponse smtpResponse = smtpRequest.execute();

		// Send initial response
		sendResponse(out, smtpResponse);
		smtpState = smtpResponse.getNextState();

		List<SmtpMessage> msgList = new ArrayList<>();
		SmtpMessage msg = new SmtpMessage();

		while (smtpState != SmtpState.CONNECT) {
			String line = input.readLine();

			if (line == null) {
				break;
			}

			// Create request from client input and current state
			SmtpRequest request = SmtpRequest.createRequest(line, smtpState);
			// Execute request and create response object
			SmtpResponse response = request.execute();
			// Move to next internal state
			smtpState = response.getNextState();
			// Send reponse to client
			sendResponse(out, response);

			// Store input in message
			String params = request.getParams();
			msg.store(response, params);

			// If message reception is complete save it
			if (smtpState == SmtpState.QUIT) {
				msgList.add(msg);
				msg = new SmtpMessage();
			}
		}

		return msgList;
	}

	/**
	 * Send response to client.
	 *
	 * @param out socket output stream
	 * @param smtpResponse response object
	 */
	private static void sendResponse(PrintWriter out, SmtpResponse smtpResponse) {
		if (smtpResponse.getCode() > 0) {
			int code = smtpResponse.getCode();
			String message = smtpResponse.getMessage();
			out.print(code + " " + message + "\r\n");
			out.flush();
		}
	}

	/**
	 * Get email received by this instance since start up.
	 *
	 * @return List of String
	 */
	public synchronized Iterator<SmtpMessage> getReceivedEmail() {
		return receivedMail.iterator();
	}

	/**
	 * Get the number of messages received.
	 *
	 * @return size of received email list
	 */
	public synchronized int getReceivedEmailSize() {
		return receivedMail.size();
	}

	/**
	 * Creates an instance of SimpleSmtpServer and starts it.
	 *
	 * @param port port number the server should listen to
	 * @return a reference to the SMTP server
	 */
	public static SafeCloseSmtpServer start(int port) {
		SafeCloseSmtpServer server = new SafeCloseSmtpServer(port);
		Thread t = new Thread(server, "Mock SMTP Server Thread");
		t.start();

		// Block until the server socket is created
		try {
			server.startupBarrier.await();
		} catch (InterruptedException e) {
			log.log(Level.WARNING, "Interrupted", e);
		}

		return server;
	}
}
