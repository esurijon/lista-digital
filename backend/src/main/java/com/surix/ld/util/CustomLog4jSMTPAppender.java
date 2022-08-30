package com.surix.ld.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Message.RecipientType;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;

import com.sun.mail.smtp.SMTPTransport;

public class CustomLog4jSMTPAppender extends SMTPAppender {

	private Session session;

	public CustomLog4jSMTPAppender() {
		super();
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.debug", "true");

		session = Session.getInstance(props);
	}
	
	@Override
	protected void sendBuffer() {
		// Note: this code already owns the monitor for this
		// appender. This frees us from needing to synchronize on 'cb'.
		try {
			StringBuffer sbuf = new StringBuffer();
			String t = layout.getHeader();
			if (t != null)
				sbuf.append(t);
			int len = cb.length();
			for (int i = 0; i < len; i++) {
				// sbuf.append(MimeUtility.encodeText(layout.format(cb.get())));
				LoggingEvent event = cb.get();
				sbuf.append(layout.format(event));
				if (layout.ignoresThrowable()) {
					String[] s = event.getThrowableStrRep();
					if (s != null) {
						for (int j = 0; j < s.length; j++) {
							sbuf.append(s[j]);
							sbuf.append(Layout.LINE_SEP);
						}
					}
				}
			}
			t = layout.getFooter();
			if (t != null)
				sbuf.append(t);

			msg.setContent(sbuf.toString(), layout.getContentType());

			msg.setSentDate(new Date());
			SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
			transport.connect(getSMTPHost(), getSMTPPort(), getSMTPUsername(), getSMTPPassword());
			transport.sendMessage(msg, msg.getRecipients(RecipientType.TO));
			transport.close();
		} catch (Exception e) {
			LogLog.error("Error occured while sending e-mail notification.", e);
		}
	}
}
