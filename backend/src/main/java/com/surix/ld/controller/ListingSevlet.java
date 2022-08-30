package com.surix.ld.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.Admin;
import com.surix.ld.model.EntityException;
import com.surix.ld.model.Event;
import com.surix.ld.model.FileEntity;
import com.surix.ld.model.Permissions;
import com.surix.ld.model.Planner;
import com.surix.ld.model.Role;
import com.surix.ld.model.User;
import com.surix.ld.model.Permissions.Permission;
import com.surix.ld.util.ParamValue;
import com.surix.ld.util.XMLUtils;

@SuppressWarnings("serial")
public class ListingSevlet extends OnLineListsServlet {
	
	private static final String PLANNERS_LISTING_XSL = "admin/plannersListing.xsl";
	private static final String EVENT_INFO_XSL = "planner/eventInfo.xsl";
	private static final String PLANNER_INFO_XSL = "admin/plannerInfo.xsl";

	private String getListType(HttpServletRequest req) {
		String type = req.getRequestURI();
		type = type.replace(req.getContextPath() + "/list/", "");
		return type;
	}

	private void listPlanners(HttpServletResponse resp) throws EntityException, IOException, TransformerException {
		XMLUtils xmlUtils = getComponent(XMLUtils.class);
		Admin admin = getComponent(Admin.class);

		Transformer transformer = xmlUtils.getTransformer(PLANNERS_LISTING_XSL);
		Source source = new StreamSource(admin.load(false));
		Result output = new StreamResult(resp.getWriter());
		transformer.transform(source, output);
	}

	private void eventInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException, TransformerException, LdException {
		XMLUtils xmlUtils = getComponent(XMLUtils.class);
		Admin admin = getComponent(Admin.class);

		Event event = (Event) getEntity(req, FileEntity.Types.EVENT);
		String[] ids = event.getId().split("/");
		Planner planner = admin.getPlanner(ids[0]);
		Transformer transformer = xmlUtils.getTransformer(
			EVENT_INFO_XSL, 
			new ParamValue<Object>("planner", planner.getName()),
			new ParamValue<Object>("event-id", ids[1]));
		Source source = new StreamSource(planner.load(false));
		Result output = new StreamResult(resp.getWriter());
		transformer.transform(source, output);
	}

	private void plannerInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException, TransformerException, LdException {
		XMLUtils xmlUtils = getComponent(XMLUtils.class);
		Admin admin = getComponent(Admin.class);

		Planner planner = (Planner) getEntity(req, FileEntity.Types.PLANNER);

		Transformer transformer = xmlUtils.getTransformer(
			PLANNER_INFO_XSL,
			new ParamValue<Object>("planner-id", planner.getId()));
		Source source = new StreamSource(admin.load(false));
		Result output = new StreamResult(resp.getWriter());
		transformer.transform(source, output);
	}

	private void listEntityVersions(HttpServletRequest req, HttpServletResponse resp) throws LdException {
		FileEntity entity = getEntity(req, new Permissions(Permission.READ_ENABLED));
		List<Long> versions = entity.getVersions();
		Collections.sort(versions, Collections.reverseOrder());
		versions = versions.subList(0, Math.min(20, versions.size()));
		try {
			streamOut(new ResponseMsg(versions), resp);
		} catch (Exception e) {
			throw new LdException(e);
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			resp.setContentType("text/xml; charset=UTF-8");
			String listType = getListType(req);
			if ("planners".equals(listType)) {
				listPlanners(resp);
			} else if ("event/versions".equals(listType)) {
				listEntityVersions(req, resp);
			} else if ("event/info".equals(listType)) {
				eventInfo(req, resp);
			} else if ("planner/info".equals(listType)) {
				plannerInfo(req, resp);
			} else if ("user/info".equals(listType)) {
				userInfo(req, resp);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	private void userInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession(false);
		User user = null;
		if(session != null) {
			user = (User)session.getAttribute(Params.USER.toString());
		}
		if (session == null || user == null) {
			user = new User("", "NOT_LOGUED", Role.GUEST);
		}
		resp.getWriter().write(user.toXml());
	}
}
