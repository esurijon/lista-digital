package com.surix.ld.controller;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.FileEntity;
import com.surix.ld.model.Permissions;
import com.surix.ld.model.Permissions.Permission;
import com.surix.ld.util.XMLUtils;

@SuppressWarnings("serial")
public class Download extends OnLineListsServlet {

	private static final String EVENT_CSV_EXPORT_XSL = "event/csv.xsl";

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			FileEntity entity = getEntity(req, new Permissions(Permission.READ_ENABLED));
			resp.setHeader("Content-type", "text/csv; charset=ISO-8859-1");
			resp.setHeader("Content-Disposition", "attachment; filename=" + entity.getName() + ".csv");
			downloadEvent(entity, resp.getWriter());
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private void downloadEvent(FileEntity entity, Writer writer) throws IOException, TransformerException, LdException {
		XMLUtils xmlUtils = getComponent(XMLUtils.class);
		Transformer transformer = xmlUtils.getTransformer(EVENT_CSV_EXPORT_XSL);
		Source source = new StreamSource(entity.load(false));
		Result output = new StreamResult(writer);
		transformer.transform(source, output);
	}

	
}
