package org.dstadler.commons.selenium;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class ChromeVersionInputStream extends FilterInputStream {
	// how many characters to keep in the buffer for regex matching
	private static final int REGEX_WINDOW = 200;

	private final Pattern regex;

	private final StringBuilder buffer = new StringBuilder();

	private String driverVersion;
	private String url;

	public ChromeVersionInputStream(InputStream in, String chromeVersion) {
		super(in);
		this.regex = Pattern.compile(
				"https://(edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing|storage.googleapis.com/chrome-for-testing-public)/(" + chromeVersion + "[0-9.]+)/linux64/chromedriver-linux64.zip");
	}

	public int read() throws IOException {
		int b = in.read();
		if (b != -1) {
			buffer.append((char)b);

			checkAndReduceBuffer(false);
		}
		return b;
	}

	public int read(byte[] buf, int off, int len) throws IOException {
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

	public void consumeAndClose() throws IOException {
		IOUtils.consume(this);
		this.close();
	}

	public String getDriverVersion() {
		return driverVersion;
	}

	public String getUrl() {
		return url;
	}
}
