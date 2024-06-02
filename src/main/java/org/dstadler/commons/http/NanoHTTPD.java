package org.dstadler.commons.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.dstadler.commons.util.SuppressForbidden;

/**
 * A simple, tiny, nicely embeddable HTTP 1.0 server in Java
 *
 * <p> NanoHTTPD version 1.14,
 * Copyright &copy; 2001,2005-2010 Jarno Elonen (elonen@iki.fi, <a href="http://iki.fi/elonen/">http://iki.fi/elonen/</a>)
 *
 * <p><b>Features + limitations: </b><ul>
 *
 *    <li> Only one Java file </li>
 *    <li> Java 1.1 compatible </li>
 *    <li> Released as open source, Modified BSD licence </li>
 *    <li> No fixed config files, logging, authorization etc. (Implement yourself if you need them.) </li>
 *    <li> Supports parameter parsing of GET and POST methods </li>
 *    <li> Supports both dynamic content and file serving </li>
 *    <li> Never caches anything </li>
 *    <li> Doesn't limit bandwidth, request time or simultaneous connections </li>
 *    <li> Default code serves files and shows all HTTP parameters and headers</li>
 *    <li> File server supports directory listing, index.html and index.htm </li>
 *    <li> File server does the 301 redirection trick for directories without '/'</li>
 *    <li> File server supports simple skipping for files (continue download) </li>
 *    <li> File server uses current directory as a web root </li>
 *    <li> File server serves also very long files without memory overhead </li>
 *    <li> Contains a built-in list of most common mime types </li>
 *    <li> All header names are converted lowercase so they don't vary between browsers/clients </li>
 *
 * </ul>
 *
 * <p><b>Ways to use: </b><ul>
 *
 *    <li> Run as a standalone app, serves files from current directory and shows requests</li>
 *    <li> Subclass serve() and embed to your own program </li>
 *    <li> Call serveFile() from serve() with your own base directory </li>
 *
 * </ul>
 *
 * See the end of the source file for distribution license
 * (Modified BSD licence)
 */
public class NanoHTTPD
{
    private final static Logger logger = Logger.getLogger(NanoHTTPD.class.getName());

	/**
	 * GMT date formatter, have a local instance to avoid multi-threading issues
	 */
    private static final FastDateFormat gmtFrmt = FastDateFormat.getInstance( "E, d MMM yyyy HH:mm:ss 'GMT'", TimeZone.getTimeZone("GMT"), Locale.US);

    private static String encoding = null;

	public static void setEncoding(String encoding) {
		NanoHTTPD.encoding = encoding;
	}

    // ==================================================
	// API parts
	// ==================================================
	/**
	 * Override this to customize the server.<p>
	 *
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 *
	 * @param uri	Percent-decoded URI without parameters, for example "/index.cgi"
	 * @param method	"GET", "POST" etc.
	 * @param parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
	 * @param header	Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	public Response serve( String uri, String method, Properties header, Properties parms )
	{
		System.out.println( method + " '" + uri + "' " );

		Enumeration<?> e = header.propertyNames();
		while ( e.hasMoreElements())
		{
			String value = (String)e.nextElement();
			System.out.println( "  HDR: '" + value + "' = '" +
								header.getProperty( value ) + "'" );
		}
		e = parms.propertyNames();
		while ( e.hasMoreElements())
		{
			String value = (String)e.nextElement();
			System.out.println( "  PRM: '" + value + "' = '" +
								parms.getProperty( value ) + "'" );
		}

		return serveFile( uri, header, new File("."), true );
	}

	/**
	 * HTTP response.
	 * Return one of these from serve().
	 */
	public static class Response
	{
		/**
		 * Default constructor: response = HTTP_OK, data = mime = 'null'
		 */
		public Response()
		{
			this.status = HTTP_OK;
		}

		/**
		 * Basic constructor.
		 *
		 * @param status The HTTP status code to report
		 * @param mimeType The mime-type of the response
		 * @param data The contents of hte response
		 */
		public Response( String status, String mimeType, InputStream data )
		{
			this.status = status;
			this.mimeType = mimeType;
			this.data = data;
		}

		/**
		 * Convenience method that creates a {@link Response} out of
		 * given text.
		 *
		 * @param status The HTTP status code to report
		 * @param mimeType The mime-type of the response
		 * @param txt The contents of the response
		 */
		public Response( String status, String mimeType, String txt )
		{
			this.status = status;
			this.mimeType = mimeType;
			try {
				this.data = new ByteArrayInputStream( encoding != null ? txt.getBytes(encoding) : txt.getBytes());         // NOSONAR
			} catch (UnsupportedEncodingException e) {
				logger.log(Level.WARNING, "Unsupported encoding: " + encoding, e);
			}
		}

		/**
		 * Adds given line to the header.
		 *
		 * @param name The name of the HTTP header
		 * @param value The value of the HTTP header
		 */
		public void addHeader( String name, String value )
		{
			header.put( name, value );
		}

		/**
		 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
		 */
		public String status;

		/**
		 * MIME type of content, e.g. "text/html"
		 */
		public String mimeType;

		/**
		 * Data of the response, may be null.
		 */
		public InputStream data;

		/**
		 * Headers for the HTTP response. Use addHeader()
		 * to add lines.
		 */
		public Properties header = new Properties();
	}

	/**
	 * Some HTTP response status codes
	 */
	public static final String
		HTTP_OK = "200 OK",
		HTTP_REDIRECT = "301 Moved Permanently",
		HTTP_FORBIDDEN = "403 Forbidden",
		HTTP_NOTFOUND = "404 Not Found",
		HTTP_BADREQUEST = "400 Bad Request",
		HTTP_INTERNALERROR = "500 Internal Server Error",
		HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String
		MIME_PLAINTEXT = "text/plain",
		MIME_HTML = "text/html",
		MIME_JSON = "application/json",
		MIME_DEFAULT_BINARY = "application/octet-stream";

	// ==================================================
	// Socket & server code
	// ==================================================

	/**
	 * Starts a HTTP server to given port and binds on all host-names.<p>
	 *
	 * @param port The port to listen for HTTP connections
	 *
	 * @throws IOException if the socket is already in use
	 *
	 */
	public NanoHTTPD( int port) throws IOException {
		this(port, null);
	}

	/**
	 * Starts a HTTP server to given port and only binds on the given name if specifed non-null.<p>
	 *
	 * @param port The port to listen for HTTP connections
	 * @param bindHost If non-null, the hostanme/address to bind to.
	 *
	 * @throws IOException if the socket is already in use
	 */
	public NanoHTTPD( int port, InetAddress bindHost ) throws IOException {
		this(port, bindHost, 0);
	}

	/**
	 * Starts a HTTP server to given port and only binds on the given name if specifed non-null.<p>
	 *
	 * @param port The port to listen for HTTP connections
	 * @param bindHost If non-null, the hostanme/address to bind to.
	 * @param sessionTimeout Timeout in milliseconds after which reading from the HTTP client side is terminated
	 *                       with a timeout-error.
	 *
	 * @throws IOException if the socket is already in use
	 */
	public NanoHTTPD( int port, InetAddress bindHost, final int sessionTimeout ) throws IOException
	{
		myServerSocket = new ServerSocket( port, 50, bindHost );
		myThread = new Thread("NanoHTTPD Micro Webserver Thread") {
			@Override
			public void run()
			{
				try
				{
					//noinspection InfiniteLoopStatement
					while( true ) {
						Socket socket = myServerSocket.accept();
						if(sessionTimeout > 0) {
							socket.setSoTimeout(sessionTimeout);
						}
						HTTPSession httpSession = new HTTPSession(socket);
						httpSession.start();
					}
				}
				catch ( IOException e ) {
					if(stopping) {
						logger.log(Level.INFO, "Stopping socket connections: " + e);
					} else {
						logger.log(Level.WARNING, "Failed while accepting socket connections.", e);
					}
				}
			}
		};
		myThread.setDaemon( true );
		myThread.start();
	}

	/**
	 * Stops the server.
	 */
	public void stop()
	{
		try
		{
			stopping = true;
			myServerSocket.close();
			myThread.join();
		}
		catch ( IOException | InterruptedException e ) {
			logger.log(Level.WARNING, "Had unexpected exception during stop", e);
		}
	}

	/**
	 * Starts as a standalone file server and waits for Enter.
	 *
	 * @param args The commandline arguments, e.g. for specifying the port
	 *
	 * @throws IOException If starting the web-server fails.
	 */
	@SuppressForbidden(reason = "Allow to exit the application with a return code here")
	public static void main( String[] args ) throws IOException
	{
		System.out.println( "NanoHTTPD 1.14 (C) 2001,2005-2010 Jarno Elonen\n" +
							"(Command line options: [port] [--licence])\n" );

		// Show licence if requested
		int lopt = -1;
		for ( int i=0; i<args.length; ++i ) {
			if ( args[i].toLowerCase().endsWith( "licence" ))
			{
				lopt = i;
				System.out.println( LICENCE + "\n" );
			}
		}

		// Change port if requested
		int port = 80;
		if ( args.length > 0 && lopt != 0 ) {
			port = Integer.parseInt( args[0] );
		}

		if ( args.length > 1 &&
			 args[1].toLowerCase().endsWith( "licence" )) {
			System.out.println( LICENCE + "\n" );
		}

		final NanoHTTPD nh;
		try
		{
			nh = new NanoHTTPD( port );
		}
		catch( IOException ioe )
		{
			System.err.println( "Couldn't start server:\n" + ioe );
			System.exit( -1 );
			return;
		}

		System.out.println( "Now serving files in port " + port + " from \"" +
							new File("").getAbsolutePath() + "\"" );
		System.out.println( "Hit Enter to stop.\n" );

		try {
			System.in.read();
		} finally {
			nh.stop();
		}
	}

	/**
	 * Handles one session, i.e. parses the HTTP request
	 * and returns the response.
	 */
	private class HTTPSession implements Runnable
	{
		public HTTPSession(Socket s)
		{
			mySocket = s;
		}

		public void start() {
			Thread t = new Thread( this, "NanoHTTPD Session Thread" );
			t.setDaemon( true );
			t.start();
		}

		@Override
		public void run()
		{
			try
			{
                InputStream is = mySocket.getInputStream();
				if ( is == null) {
					return;
				}
                BufferedReader in = new BufferedReader( new InputStreamReader( is ));       // NOSONAR - test class works only locally anyway

				// Read the request line
				String inLine = in.readLine();
				try {
					if (inLine == null) {
						return;
					}
					StringTokenizer st = new StringTokenizer( inLine );
					if ( !st.hasMoreTokens()) {
						sendError( HTTP_BADREQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html" );
					}

					String method = st.nextToken();

					if ( !st.hasMoreTokens()) {
						sendError( HTTP_BADREQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html" );
					}

					String uri = st.nextToken();

					// Decode parameters from the URI
					Properties parms = new Properties();
					int qmi = uri.indexOf( '?' );
					if ( qmi >= 0 )
					{
						decodeParms( uri.substring( qmi+1 ), parms );
						uri = decodePercent( uri.substring( 0, qmi ));
					} else {
						uri = decodePercent(uri);
					}


					// If there's another token, it's protocol version,
					// followed by HTTP headers. Ignore version but parse headers.
					// NOTE: this now forces header names uppercase since they are
					// case insensitive and vary by client.
					Properties header = new Properties();
					if ( st.hasMoreTokens())
					{
						readProperties(in, header);
					}

					// If the method is POST, there may be parameters
					// in data section, too, read it:
					if ( method.equalsIgnoreCase( "POST" ))
					{
						handlePOST(in, parms, header);
					}

					// Ok, now do the serve()
					Response r = serve( uri, method, header, parms );
					if ( r == null ) {	// NOSONAR - server() can be overwritten and thus could return null!
						sendError( HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response." );
					} else {
						sendResponse( r.status, r.mimeType, r.header, r.data );
					}
				} catch ( InterruptedException ie ) {
					// Thrown by sendError, ignore and exit the thread.
				} catch (Throwable e) {       // NOSONAR - test class works only locally anyway
					logger.log(Level.WARNING, "Had Exception in HTTPSession handling thread", e);

					String msg = "<html><body>Exception in HTTPSession handling thread, error: " + e.getMessage() + "</body></html>";
					try
					{
						sendError( HTTP_INTERNALERROR, msg);
					}
					catch ( Throwable t ) { // NOPMD - imported code
						// imported code
					}
				} finally {
					in.close();
				}
			}
			catch ( IOException ioe ) {
				try {
					sendError(HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
				} catch (Throwable t) { // NOPMD - imported code
					// imported code
				}
			}
		}

        private void readProperties(BufferedReader in, Properties header) throws IOException {
			// if the InputStream does not provide full lines, we can get stuck reading more data
			// and if the other party actually waits for our response, we can get "stuck" here
			// waiting for the next line

			// Thus we forcefully stop and interrupt the Socket-handling here by detecting
			// that reading the properties took to long and thus we close the input of the socket
			// forcefully to avoid a deadlock

			Semaphore sem = new Semaphore(1);
			sem.acquireUninterruptibly();

			ForkJoinTask<?> task = ForkJoinPool.commonPool().submit(() -> {
				try {
					// wait 1 second for reading lines to finish, otherwise interrupt the thread
					if (!sem.tryAcquire(3, TimeUnit.SECONDS)) {
						logger.warning("Timeout reading input from socket " + mySocket + " while reading properties");
						mySocket.shutdownInput();
					}
				} catch (InterruptedException | IOException e) {
					throw new RuntimeException(e);
				}
			});

			try {
				while (true) {
					String line = in.readLine();
					if (line == null) {
						break;
					}

					int p = line.indexOf(':');
					if (p == -1) {
						logger.warning("Could not parse property " + line);
					} else {
						header.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
					}
				}
			} finally {
				// the task is not needed any more
				task.cancel(true);
				sem.release();
			}
        }

        private void handlePOST(BufferedReader in, Properties parms, Properties header) throws IOException, InterruptedException {
            long size = 0x7FFFFFFFFFFFFFFFL;
            String contentLength = header.getProperty("content-length");
            if (contentLength != null)
            {
            	try { size = Integer.parseInt(contentLength); }
				catch (NumberFormatException ex) { // NOPMD - imported code
					// imported code
				}
            }
            StringBuilder postLine = new StringBuilder();
			char[] buf = new char[512];
            int read = in.read(buf);
            while ( read >= 0 && size > 0 && !postLine.toString().endsWith("\r\n") )
            {
            	size -= read;
            	postLine.append(String.valueOf(buf, 0, read));
            	if ( size > 0 ) {
            		read = in.read(buf);
            	}
            }
            decodeParms( postLine.toString().trim(), parms );
        }

		/**
		 * Decodes the percent encoding scheme. <br/>
		 * For example: "an+example%20string" -> "an example string"
		 */
		private String decodePercent( String str ) throws InterruptedException
		{
			try
			{
				StringBuilder sb = new StringBuilder();
				for( int i=0; i<str.length(); i++ )
				{
				    char c = str.charAt( i );
				    switch ( c )
					{
				        case '+':
				            sb.append( ' ' );
				            break;
				        case '%':
			                sb.append((char)Integer.parseInt( str.substring(i+1,i+3), 16 ));
				            i += 2;
				            break;
				        default:
				            sb.append( c );
				            break;
				    }
				}
				return sb.toString();
			}
			catch( Exception e )
			{
				sendError( HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding." );
				return null;
			}
		}

		/**
		 * Decodes parameters in percent-encoded URI-format
		 * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
		 * adds them to given Properties. NOTE: this doesn't support multiple
		 * identical keys due to the simplicity of Properties -- if you need multiples,
		 * you might want to replace the Properties with a Hastable of Vectors or such.
		 */
		private void decodeParms( String parms, Properties p )
			throws InterruptedException
		{
			if ( parms == null ) {
				return;
			}

			StringTokenizer st = new StringTokenizer( parms, "&" );
			while ( st.hasMoreTokens())
			{
				String e = st.nextToken();
				int sep = e.indexOf( '=' );
				if ( sep >= 0 ) {
					p.put( decodePercent( e.substring( 0, sep )).trim(),
						   decodePercent( e.substring( sep+1 )));
				}
			}
		}

		/**
		 * Returns an error message as a HTTP response and
		 * throws InterruptedException to stop furhter request processing.
		 */
		private void sendError( String status, String msg ) throws InterruptedException
		{
			sendResponse( status, MIME_PLAINTEXT, null, new ByteArrayInputStream( msg.getBytes()));       // NOSONAR - test class works only locally anyway
			throw new InterruptedException();
		}

		/**
		 * Sends given response to the socket.
		 */
		private void sendResponse( String status, String mime, Properties header, InputStream data )
		{
			try
			{
				if ( status == null )
				 {
					throw new Error( "sendResponse(): Status can't be null." ); // NOPMD - imported code
				}

				try (OutputStream out = mySocket.getOutputStream()) {
					try (PrintWriter pw = new PrintWriter( out )) {
						pw.print("HTTP/1.0 " + status + " \r\n");

						if ( mime != null ) {
							pw.print("Content-Type: " + mime + "\r\n");
						}

						if ( header == null || header.getProperty( "Date" ) == null ) {
							pw.print( "Date: " + gmtFrmt.format( new Date()) + "\r\n");
						}

						if ( header != null )
						{
							Enumeration<?> e = header.keys();
							while ( e.hasMoreElements())
							{
								String key = (String)e.nextElement();
								String value = header.getProperty( key );
								pw.print( key + ": " + value + "\r\n");
							}
						}

						pw.print("\r\n");
						pw.flush();

						if ( data != null )
						{
							IOUtils.copy(data, out);
						}
						out.flush();
					}
				}
				if ( data != null ) {
					data.close();
				}
			}
			catch( IOException ioe )
			{
				// Couldn't write? No can do.
				try {
					mySocket.close();
				} catch( Throwable t ) { // NOPMD - imported code
					// imported code
				}
			}
		}

		private final Socket mySocket;
	}

	/**
	 * URL-encodes everything between '/'-characters.
	 * Encodes spaces as '%20' instead of '+'.
	 */
	private String encodeUri( String uri )
	{
		StringBuilder newUri = new StringBuilder();
		StringTokenizer st = new StringTokenizer( uri, "/ ", true );
		while ( st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if ( tok.equals( "/" )) {
				newUri.append('/');
			} else if ( tok.equals( " " )) {
				newUri.append("%20");
			} else {
				try {
					newUri.append(URLEncoder.encode( tok, "UTF-8" ));
				} catch ( UnsupportedEncodingException uee ) { // NOPMD - imported code
					// imported code
				}
			}
		}
		return newUri.toString();
	}

	private final ServerSocket myServerSocket;
	private final Thread myThread;
	// helper to not log exceptions during shutdown
	private volatile boolean stopping = false;

	// ==================================================
	// File server code
	// ==================================================

	/**
	 * Serves file from homeDir and its' subdirectories (only).
	 * Uses only URI, ignores all headers and HTTP parameters.
	 *
	 * @param uriIn Which file to serve
	 * @param header HTTP headers, currently ignored
	 * @param homeDir The base-dir from where to server the file
	 * @param allowDirectoryListing If contents of directories can be listed
	 *
	 * @return The resulting response-object.
	 */
	public Response serveFile( String uriIn, Properties header, File homeDir,
							   boolean allowDirectoryListing )
	{
		// Make sure we won't die of an exception later
		if ( !homeDir.isDirectory()) {
			return new Response( HTTP_INTERNALERROR, MIME_PLAINTEXT,
								 "INTERNAL ERRROR: serveFile(): given homeDir is not a directory." );
		}

		// Remove URL arguments
		String uri = uriIn.trim().replace( File.separatorChar, '/' );
		if ( uri.indexOf( '?' ) >= 0 ) {
			uri = uri.substring(0, uri.indexOf( '?' ));
		}

		// Prohibit getting out of current directory
		if ( uri.startsWith( ".." ) || uri.endsWith( ".." ) || uri.contains( "../" ) ) {
			return new Response( HTTP_FORBIDDEN, MIME_PLAINTEXT,
								 "FORBIDDEN: Won't serve ../ for security reasons." );
		}

		File f = new File( homeDir, uri );
		if ( !f.exists()) {
			return new Response( HTTP_NOTFOUND, MIME_PLAINTEXT,
								 "Error 404, file not found." );
		}

		// List the directory, if necessary
		if ( f.isDirectory())
		{
			// Browsers get confused without '/' after the
			// directory, send a redirect.
			if ( !uri.endsWith( "/" ))
			{
				uri += '/';
				Response r = new Response( HTTP_REDIRECT, MIME_HTML,
										   "<html><body>Redirected: <a href=\"" + uri + "\">" +
										   uri + "</a></body></html>");
				r.addHeader( "Location", uri );
				return r;
			}

			// First try index.html and index.htm
			if ( new File( f, "index.html" ).exists()) {
				f = new File( homeDir, uri + "/index.html" );
			} else if ( new File( f, "index.htm" ).exists()) {
				f = new File( homeDir, uri + "/index.htm" );
			} else if ( allowDirectoryListing )
			{
				String msg = createDirListing(uri, f);
				return new Response( HTTP_OK, MIME_HTML, msg );
			}
			else
			{
				return new Response( HTTP_FORBIDDEN, MIME_PLAINTEXT,
								 "FORBIDDEN: No directory listing." );
			}
		}

		try
		{
			// Get MIME type from file name extension, if possible
			String mime = getMIMEType(f);

			// Support (simple) skipping:
			long startFrom = getRange(header);

			try (InputStream fis = new FileInputStream( f )) {
				if(fis.skip( startFrom ) != startFrom) {
					logger.info("Skipped less bytes than expected: " + startFrom);
				}
				Response r = new Response( HTTP_OK, mime, fis );
				r.addHeader( "Content-length", "" + (f.length() - startFrom));
				r.addHeader( "Content-range", "" + startFrom + "-" +
							(f.length()-1) + '/' + f.length());
				return r;
			}
		}
		catch( IOException ioe )
		{
			return new Response( HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: Reading file failed." );
		}
	}

    private long getRange(Properties header) {
        String range = header.getProperty( "range" );
        if ( range != null && range.startsWith( "bytes=" )) {
        	range = range.substring( "bytes=".length());
        	int minus = range.indexOf( '-' );
        	if ( minus >= 0 ) {
        		range = range.substring( 0, minus );
        	}

        	try	{
        		return Long.parseLong( range );
        	}
        	catch ( NumberFormatException nfe ) {
        	    logger.log(Level.WARNING, "Could not parse range specified in the headers: " + range, nfe);
        	}
        }
        return 0;
    }

    private String getMIMEType(File f) throws IOException {
        String mime = null;
        int dot = f.getCanonicalPath().lastIndexOf( '.' );
        if ( dot >= 0 ) {
        	mime = theMimeTypes.get( f.getCanonicalPath().substring( dot + 1 ).toLowerCase());
        }
        if ( mime == null ) {
        	mime = MIME_DEFAULT_BINARY;
        }
        return mime;
    }

    private String createDirListing(String uri, File f) {
        String[] files = f.list();
        StringBuilder msg = new StringBuilder("<html><body><h1>Directory " + uri + "</h1><br/>");

        if ( uri.length() > 1 )
        {
        	String u = uri.substring( 0, uri.length()-1 );
        	int slash = u.lastIndexOf( '/' );
        	if ( slash >= 0 && slash  < u.length()) {
        		msg.append("<b><a href=\"").append(uri, 0, slash + 1).append("\">..</a></b><br/>");
        	}
        }

        for ( int i=0; i<files.length; ++i )
        {
        	File curFile = new File( f, files[i] );
        	boolean dir = curFile.isDirectory();
        	if ( dir )
        	{
        		msg.append("<b>");
        		files[i] += '/';
        	}

        	msg.append("<a href=\"").append(encodeUri(uri + files[i])).append("\">").append(files[i]).append("</a>");

        	// Show file size
        	if ( curFile.isFile())
        	{
        		long len = curFile.length();
        		msg.append(" &nbsp;<font size=2>(");
        		if ( len < 1024 ) {
        			msg.append(curFile.length()).append(" bytes");
        		} else if ( len < 1024 * 1024 ) {
        			msg.append(curFile.length() / 1024).append(".").append(curFile.length() % 1024 / 10 % 100).append(" KB");
        		} else {
        			msg.append(curFile.length() / (1024 * 1024)).append(".").append(curFile.length() % (1024 * 1024) / 10 % 100).append(" MB");
        		}

        		msg.append(")</font>");
        	}
        	msg.append("<br/>");
        	if ( dir ) {
        		msg.append("</b>");
        	}
        }
        return msg.toString();
    }

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	private static final Map<String,String> theMimeTypes = new HashMap<>();
	static
	{
		StringTokenizer st = new StringTokenizer(
			"htm		text/html "+
			"html		text/html "+
			"txt		text/plain "+
			"asc		text/plain "+
			"gif		image/gif "+
			"jpg		image/jpeg "+
			"jpeg		image/jpeg "+
			"png		image/png "+
			"mp3		audio/mpeg "+
			"m3u		audio/mpeg-url " +
			"pdf		application/pdf "+
			"doc		application/msword "+
			"ogg		application/x-ogg "+
			"zip		application/octet-stream "+
			"exe		application/octet-stream "+
			"class		application/octet-stream " );
		while ( st.hasMoreTokens()) {
			theMimeTypes.put( st.nextToken(), st.nextToken());
		}
	}

	/**
	 * The distribution licence
	 */
	private static final String LICENCE =
		"Copyright (C) 2001,2005-2010 by Jarno Elonen <elonen@iki.fi>\n"+
		"\n"+
		"Redistribution and use in source and binary forms, with or without\n"+
		"modification, are permitted provided that the following conditions\n"+
		"are met:\n"+
		"\n"+
		"Redistributions of source code must retain the above copyright notice,\n"+
		"this list of conditions and the following disclaimer. Redistributions in\n"+
		"binary form must reproduce the above copyright notice, this list of\n"+
		"conditions and the following disclaimer in the documentation and/or other\n"+
		"materials provided with the distribution. The name of the author may not\n"+
		"be used to endorse or promote products derived from this software without\n"+
		"specific prior written permission. \n"+
		" \n"+
		"THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n"+
		"IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"+
		"OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"+
		"IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n"+
		"INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n"+
		"NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n"+
		"DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n"+
		"THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n"+
		"(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n"+
		"OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
}
