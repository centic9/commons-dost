package org.dstadler.commons.email;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Configuration object for the properties that are necessary to configure
 * an SMTP email server.
 *
 * Note: properties for sending an email are stored in a separate object,
 * see @link EmailConfig
 *
 * The value "verificationEmail()" is just a way to remember the email that
 * was entered in the dialog across restarts, it is only
 * used for sending emails to verify that the configuration is okay.
 *
 * @author dominik.stadler
 *
 */
public class MailserverConfig {
	//private static final Log logger = LogFactory.getLog(MailserverConfig.class);

	public static final int SERVER_PORT_DEFAULT = 25;

	private String serverAddress = "";
	private int serverPort = SERVER_PORT_DEFAULT;

	private String userId = "";
	private String password = "";
	private String bounce = "";
	private String subjectPrefix = "";
	private boolean sslEnabled = false;

	private boolean debug = false;

	public MailserverConfig() {
		super();
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		if (this.password == null) {
			this.password = "";
		}
	}


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBounce() {
		return bounce;
	}

	public void setBounce(String bounce) {
		this.bounce = bounce;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getSubjectPrefix() {
		return subjectPrefix;
	}

	public void setSubjectPrefix(String subjectPrefix) {
		this.subjectPrefix = subjectPrefix;
	}

	public boolean isSSLEnabled() {
		return sslEnabled;
	}

	public void setSSLEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	@Override
	public String toString() {
		//return ToStringBuilder.reflectionToString(this);
		return new ToStringBuilder(this).
	       append("serverAddress", serverAddress).
	       append("serverPort", serverPort).
	       append("userId", userId).
	       // excluded! append("password", password).
	       append("bounce", bounce).
	       append("subjectPrefix", subjectPrefix).
	       append("sslEnabled", sslEnabled).
	       append("debug", debug).
	       toString();
	}
}
