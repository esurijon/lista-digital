package com.surix.ld.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.mail.smtp.SMTPTransport;
import com.surix.ld.exceptions.LdException;
import com.surix.ld.exceptions.MailSenderException;

public class MailSender {

	private class XsltBodyPart extends MimeBodyPart {

		private Transformer transformer;
		private Source data;

		public XsltBodyPart(Transformer transformer, Source data) {
			this.data = data;
			this.transformer = transformer;
		}

		@Override
		public void writeTo(OutputStream os) throws IOException, MessagingException {
			Result outputTarget = new StreamResult(os);
			try {
				transformer.transform(data, outputTarget);
			} catch (TransformerException e) {
				throw new MessagingException("ERROR_GENERTING MESSAGE", e);
			}
		}
	}

	private static final String RESET_PASSWORD_XSL = "mail/resetPassword.xsl";
	private static final String ACTIVATE_PLANNER_ACCOUNT_XSL = "mail/activatePlannerAccount.xsl";
	private static final String ACTIVATE_HOST_ACCOUNT_XSL = "mail/activateHostAccount.xsl";

	private Logger logger = Logger.getLogger(MailSender.class);

	private XMLUtils xmlUtils;

	private Session session;
	private Config cfg;

	public MailSender(Config cfg, XMLUtils xmlUtils) {
		this.xmlUtils = xmlUtils;
		this.cfg = cfg;

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		// props.setProperty("mail.debug", "true");

		session = Session.getInstance(props);
	}

	private void sendMail(String to, String subject, Source data, String htmlXsl, String txtXsl) throws MessagingException, TransformerException {
		Address[] ad = new Address[] { new InternetAddress(to) };
		sendMail(ad, null, null, subject, data, htmlXsl, txtXsl);
	}

	private void sendMail(Address[] to, Address[] cc, Address[] bcc, String subject, Source data, String htmlXsl, String txtXsl) throws MessagingException, TransformerException {
		String fromAddress = cfg.get("mail.from.address");
		String fromPresonal = cfg.get("mail.from.personal");
		String mailHost = cfg.get("mail.host");
		int mailPort = Integer.parseInt(cfg.get("mail.port"));
		String mailUser = cfg.get("mail.user");
		String mailPassword = cfg.get("mail.password");
		
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(fromAddress, fromPresonal));
		} catch (UnsupportedEncodingException e) {
			message.setFrom(new InternetAddress(fromAddress));
		}
		message.setRecipients(RecipientType.TO, to);
		message.setRecipients(RecipientType.CC, cc);
		message.setRecipients(RecipientType.BCC, bcc);
		message.setSubject(subject);

		Multipart mp = new MimeMultipart();

		Transformer htmlTransformer = xmlUtils.getTransformer(htmlXsl);
		XsltBodyPart htmlPart = new XsltBodyPart(htmlTransformer, data);
		mp.addBodyPart(htmlPart);

		if (txtXsl != null && txtXsl.length() > 0) {
			Transformer txtTransformer = xmlUtils.getTransformer(txtXsl);
			XsltBodyPart txtPart = new XsltBodyPart(txtTransformer, data);
			mp.addBodyPart(txtPart);
		}

		message.setContent(mp);

		SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
		transport.connect(mailHost, mailPort, mailUser, mailPassword);
		transport.sendMessage(message, message.getRecipients(RecipientType.TO));
		transport.close();
	}

	/**
	 * Sends email based on xml definitions, according xml file structure XML
	 * file format: <mails> <mail> <recipients> <to>email address could be more
	 * than one tag</to> <cc>email address could be more than one tag</cc>
	 * <bcc>email address could be more than one tag</bcc> </recipients>
	 * <subject>email subject</subject> <data> xml data to process with xsl
	 * template</data> </mail> <mail> <recipients> <to>email address could be
	 * more than one tag</to> <cc>email address could be more than one tag</cc>
	 * <bcc>email address could be more than one tag</bcc> </recipients>
	 * <subject>email subject</subject> <data> xml data to process with xsl
	 * template</data> </mail> </mails>
	 * 
	 * @param xslt
	 *            template make mail body it uses xml inside <data> tag
	 * @param xml
	 *            data to send emails
	 * @throws XPathExpressionException
	 * @throws MessagingException
	 * @throws TransformerException
	 * @throws XPathExpressionException
	 */
	public List<String> sendMails(Transformer xslt, Node xml) throws XPathExpressionException {
		XPath xPath = xmlUtils.getXpath();
		NodeList mails = (NodeList) xPath.evaluate("/mails/mail", xml, XPathConstants.NODESET);
		List<String> results = new ArrayList<String>();
		for (int i = 0; i < mails.getLength(); i++) {
			String result = sendMail(xslt, mails.item(i));
			results.add(result);
		}
		return results;
	}

	private String sendMail(Transformer xslt, Node xml) {
		String fromAddress = cfg.get("mail.from.address");
		String fromPresonal = cfg.get("mail.from.personal");
		String mailHost = cfg.get("mail.host");
		int mailPort = Integer.parseInt(cfg.get("mail.port"));
		String mailUser = cfg.get("mail.user");
		String mailPassword = cfg.get("mail.password");

		try {
			Address[] to = getTo(xml);
			Address[] cc = getCc(xml);
			Address[] bcc = getBcc(xml);
			String subject = getSubject(xml);
			String resultMsg = Arrays.toString(to) + Arrays.toString(cc) + Arrays.toString(bcc) + " | subject: " + subject;
			Source data = getData(xml);
				MimeMessage message = new MimeMessage(session);
				try {
					message.setFrom(new InternetAddress(fromAddress, fromPresonal));
				} catch (UnsupportedEncodingException e) {
					message.setFrom(new InternetAddress(fromAddress));
				}
				message.setRecipients(RecipientType.TO, to);
				message.setRecipients(RecipientType.CC, cc);
				message.setRecipients(RecipientType.BCC, bcc);
				message.setSubject(subject);

				Multipart mp = new MimeMultipart();
				XsltBodyPart part = new XsltBodyPart(xslt, data);
				mp.addBodyPart(part);
				message.setContent(mp);

				SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
				transport.connect(mailHost, mailPort, mailUser, mailPassword);
				transport.sendMessage(message, message.getRecipients(RecipientType.TO));
				transport.close();
				return "SENT OK: " + resultMsg;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			try {
				xmlUtils.streamOut(xml, sw, null);
			} catch (TransformerException ex) {
				logger.error(ex);
			} catch (ParserConfigurationException ex) {
				logger.error(ex);
			} catch (IOException ex) {
				logger.error(ex);
			}
			return "ERROR SENDING: " + " | " + e.getMessage();
			//return "ERROR SENDING: " + sw.toString() +  " | " + e.getMessage();
		}
	}

	private Source getData(Node xml) throws XPathExpressionException {
		XPath xPath = xmlUtils.getXpath();
		Node data = (Node) xPath.evaluate("data", xml, XPathConstants.NODE);
		return new DOMSource(data);
	}

	private String getSubject(Node xml) throws XPathExpressionException {
		XPath xPath = xmlUtils.getXpath();
		String subject = (String) xPath.evaluate("subject", xml, XPathConstants.STRING);
		return subject;
	}

	private Address[] getTo(Node xml) throws XPathExpressionException {
		XPath xPath = xmlUtils.getXpath();
		NodeList recipients = (NodeList) xPath.evaluate("recipients/to", xml, XPathConstants.NODESET);
		return getAddresses(recipients);
	}

	private Address[] getCc(Node xml) throws XPathExpressionException {
		XPath xPath = xmlUtils.getXpath();
		NodeList recipients = (NodeList) xPath.evaluate("recipients/cc", xml, XPathConstants.NODESET);
		return getAddresses(recipients);
	}

	private Address[] getBcc(Node xml) throws XPathExpressionException {
		XPath xPath = xmlUtils.getXpath();
		NodeList recipients = (NodeList) xPath.evaluate("recipients/bcc", xml, XPathConstants.NODESET);
		return getAddresses(recipients);
	}

	private Address[] getAddresses(NodeList addresses) {
		Address[] result = new InternetAddress[addresses.getLength()];
		for (int i = 0; i < result.length; i++) {
			try {
				InternetAddress address = new InternetAddress(addresses.item(i).getTextContent());
				result[i] = address;
			} catch (AddressException e) {
				logger.error(e);
			} catch (DOMException e) {
				logger.error(e);
			}
		}
		return result;
	}

	public void sendResetPasswordMail(String to, String planner, String changePasswordURL) throws LdException {
		Reader r = new StringReader(String.format("<data><user>%s</user><planner>%s</planner><resetUrl>%s</resetUrl></data>", to, planner, changePasswordURL));
		Source data = new StreamSource(r);
		try {
			sendMail(to, "Cambio de contraseña", data, RESET_PASSWORD_XSL, null);
		} catch (Exception e) {
			throw new MailSenderException(to, RESET_PASSWORD_XSL, e);
		}
	}

	public void sendActivatePlannerAccountMail(String to, String planner, String activateAccountURL) throws LdException {
		Reader r = new StringReader(String.format("<data><user>%s</user><planner>%s</planner><activationUrl>%s</activationUrl></data>", to, planner, activateAccountURL));

		Source data = new StreamSource(r);
		try {
			sendMail(to, "Bienvenido!!! Activación de cuenta", data, ACTIVATE_PLANNER_ACCOUNT_XSL, null);
		} catch (Exception e) {
			throw new MailSenderException(to, ACTIVATE_PLANNER_ACCOUNT_XSL, e);
		}
	}

	public void sendActivateHostAccountMail(String to, String planner, String activateAccountURL) throws LdException {
		Reader r = new StringReader(String.format("<data><user>%s</user><planner>%s</planner><activationUrl>%s</activationUrl></data>", to, planner, activateAccountURL));

		Source data = new StreamSource(r);
		try {
			sendMail(to, "Bienvenido!!! Activación de cuenta", data, ACTIVATE_HOST_ACCOUNT_XSL, null);
		} catch (Exception e) {
			throw new MailSenderException(to, ACTIVATE_HOST_ACCOUNT_XSL, e);
		}
	}
}
