package com.surix.ld.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.w3c.dom.Document;

import com.surix.ld.model.Role;
import com.surix.ld.model.User;
import com.surix.ld.util.MailSender;

@SuppressWarnings("serial")
public class Mailer extends MultipartServlet {

	private DocumentBuilder docBuilder;
	private TransformerFactory tFactory;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			tFactory = TransformerFactory.newInstance();
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ServletException(e);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		HttpSession session = request.getSession(false);
		User user = (User) session.getAttribute(Params.USER.toString());
		if (Role.ADMIN.equals(user.getRole()) && user.getId().equals("full")) {
			MailSender mailSender = getComponent(MailSender.class);
			try {
				FileItemIterator parts = getParts(request);
				Transformer xslt = null;
				Document xml = null;
				while (parts.hasNext()) {
					FileItemStream part = parts.next();
					if ("xml".equals(part.getFieldName())) {
						xml = docBuilder.parse(part.openStream());
					} else if ("xsl".equals(part.getFieldName())) {
						Source xsl = new StreamSource(part.openStream());
						xslt = tFactory.newTransformer(xsl);
					}
				}
				if(xslt != null && xml != null) {
					List<String> results = mailSender.sendMails(xslt, xml);
					response.setHeader("Content-type", "text/xml; charset=UTF-8");
					Writer writer = response.getWriter();
					writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
					.append("<results>");
					for (String result : results) {
						writer.append("<result><![CDATA[").append(result).append("]]></result>");
					}
					writer.append("</results>");
				} else {
					throw new ServletException("MISSING PARAMETERS");
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		} else {
			throw new ServletException("NO AUTHORIZED ROLE");
		}
	}
}
