package com.surix.ld.exceptions;

@SuppressWarnings("serial")
public class MailSenderException extends LdException {

	private String to;
	private String template;

	public MailSenderException(String to, String template, Throwable cause) {
		super("ERROR_WHILE_SENDING_MAIL", cause);
		this.to =to;
		this.template = template;
	}

	public String getTo() {
		return to;
	}

	public String getTemplate() {
		return template;
	}

}
