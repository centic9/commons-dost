package org.dstadler.commons.selenium;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;

/**
 * Specialized FilterInputStream which looks for ChromeDriver download URLs
 * in the given InputStream.
 *
 * The method consumeAndClose() can be used to fully consume the data and
 * close the given stream.
 *
 * Note: The stream needs to be closed before results are available fully.
 */
public class ChromeVersionInputStream extends FilterInputStream {
	// how many characters to keep in the buffer for regex matching
	// the matched URLs are around 110 characters, but we keep it a bit longer here to not fail if the length changes somewhat
	private static final int REGEX_WINDOW = 200;

	private final Pattern regex;

	private final StringBuilder buffer = new StringBuilder();

	private String driverVersion;
	private String url;

	public ChromeVersionInputStream(InputStream in, String chromeVersion) {
		super(in);

		// match the latest build with that version
		// https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/115.0.5790.170/linux64/chromedriver-linux64.zip
		// https://storage.googleapis.com/chrome-for-testing-public/121.0.6167.0/linux64/chromedriver-linux64.zip
		this.regex = Pattern.compile(
				"https://(edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing|storage.googleapis.com/chrome-for-testing-public)/(" + chromeVersion + "[0-9.]+)/linux64/chromedriver-linux64.zip");
	}

	@Override
	public int read() throws IOException {
		int b = in.read();
		if (b != -1) {
			buffer.append((char)b);

			checkAndReduceBuffer(false);
		}
		return b;
	}

	@Override
	public int read(@Nonnull byte[] buf, int off, int len) throws IOException {
		len = in.read(buf, off, len);
		if (len != -1) {
			buffer.append(new String(buf, off, len));

			checkAndReduceBuffer(false);
		}
		return len;
	}

	private void checkAndReduceBuffer(boolean atEnd) {
		// only apply regex if we accumulate enough data or at the end
		if (atEnd || buffer.length() > 2 * REGEX_WINDOW) {
			Matcher matcher = regex.matcher(buffer);
			while (matcher.find()) {
				driverVersion = matcher.group(2);
				url = matcher.group(1);
			}

			if (!atEnd) {
				buffer.delete(0, buffer.length() - REGEX_WINDOW);
			}
		}
	}

	@Override
	public void close() throws IOException {
		checkAndReduceBuffer(true);

		super.close();
	}

	/**
	 * Consume the given stream and close it afterwards.
	 *
	 * This will populate DriverVersion and Url properly.
	 *
	 * @throws IOException if an exception is thrown while
	 * reading or closing the stream
	 */
	public void consumeAndClose() throws IOException {
		IOUtils.consume(this);
		this.close();
	}

	/**
	 * Get the found driver version.
	 *
	 * Only available after fully consuming and closing the stream.
	 *
	 * @return The found version-string or null if not found
	 */
	public String getDriverVersion() {
		return driverVersion;
	}

	/**
	 * Get the found download-url.
	 *
	 * Only available after fully consuming and closing the stream.
	 *
	 * @return The found url for downloading the driver or null if not found
	 */
	public String getUrl() {
		return url;
	}
}
