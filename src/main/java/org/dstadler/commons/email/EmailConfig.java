package org.dstadler.commons.email;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.dstadler.commons.logging.jdk.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
* Configuration object for all properties necessary to send an email.
*
* Note: properties for the mail server configuration are provided in a
* separate object, see @link MailserverConfig.
*/
public class EmailConfig {
	private static final Logger logger = LoggerFactory.make();

	private String subject = "";

	private String from = null;

	private List<String> to = new ArrayList<>(),
					cc = new ArrayList<>(),
					bcc = new ArrayList<>();

	public EmailConfig() {
		super();
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getTo() {
		return to;
	}

	public String getToAsEmail() {
		return listToEmail(to);
	}

	public void addTo(String toIn) {
		this.to.add(toIn);
	}

	public void setTo(final List<String> toIn) {
		if(toIn == null) {
			this.to = new ArrayList<>();
		} else {
			this.to = new ArrayList<>(toIn);
		}
	}

	public List<String> getCc() {
		return cc;
	}

	public String getCcAsEmail() {
		return listToEmail(cc);
	}

	public void addCc(String ccIn) {
		this.cc.add(ccIn);
	}

	public void setCc(final List<String> ccIn) {
		if(ccIn == null) {
			this.cc = new ArrayList<>();
		} else {
			this.cc = new ArrayList<>(ccIn);
		}
	}

	public List<String> getBcc() {
		return bcc;
	}

	public String getBccAsEmail() {
		return listToEmail(bcc);
	}

	public void addBcc(String bccIn) {
		this.bcc.add(bccIn);
	}

	public void setBcc(final List<String> bccIn) {
		if(bccIn == null) {
			this.bcc = new ArrayList<>();
		} else {
			this.bcc = new ArrayList<>(bccIn);
		}
	}

	public static String listToEmail(List<String> list) {
		StringBuilder email = new StringBuilder();
		for(String address : list) {
			if(address == null || address.length() == 0) {
				logger.warning("Trying to use email recipient without email address: " + address + " cannot send email to this recipient.");
			} else {
				email.append(address).append(',');
			}
		}

		// trim any trailing commas
		while(email.length() > 0 && ',' == email.charAt(email.length()-1)) {
			email.setLength(email.length()-1);
		}

		return email.toString();
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
