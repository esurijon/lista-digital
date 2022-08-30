package com.surix.ld.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.Admin;
import com.surix.ld.model.EntityException;
import com.surix.ld.model.Planner;
import com.surix.ld.util.XMLUtils;

@SuppressWarnings("serial")
public class RegisterStandAloneEvent extends OnLineListsServlet {

	private static final String ADD_STAND_ALONE_EVENT = "planner/addStandAloneEvent.xsl";

	private static Logger tracker = Logger.getLogger("Tracker");	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		Admin admin = getComponent(Admin.class);
		try {
			Planner defaultPlanner = admin.getPlanner("0");
			defaultPlanner.save(getStandAloneEvent(request));
			tracker.info("NEW EVENT REGISTERED");
			streamOut(new ResponseMsg("OK", "EVENT_REGISTERED"), response);
		} catch (EntityException e) {
			throw new ServletException(e);
		} catch (TransformerException e) {
			throw new ServletException(e);
		} catch (LdException e) {
			throw new ServletException(e);
		}
	}

	private Source getStandAloneEvent(HttpServletRequest req) throws IOException, TransformerException {
		Source xmlSource = new StreamSource(req.getInputStream());
		DOMResult output = new DOMResult();
		Transformer transformer = getComponent(XMLUtils.class).getTransformer(ADD_STAND_ALONE_EVENT);
		transformer.transform(xmlSource, output);
		return new DOMSource(output.getNode());
	}
}
