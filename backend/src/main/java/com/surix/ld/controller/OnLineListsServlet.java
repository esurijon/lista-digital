package com.surix.ld.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.picocontainer.MutablePicoContainer;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.Admin;
import com.surix.ld.model.FileEntity;
import com.surix.ld.model.Permissions;
import com.surix.ld.model.Planner;
import com.surix.ld.model.User;
import com.surix.ld.model.Permissions.Permission;
import com.surix.ld.util.XMLUtils;

@SuppressWarnings("serial")
public abstract class OnLineListsServlet extends HttpServlet {

	public enum Params {
		USER, PASSWORD, ROLE, ADMIN_ID, PLANNER_ID, EVENT_ID, RESET_TOKEN, ACTIVATE_ACCOUNT, CHANGE_PASSWORD, ENCODED, NEW_PASSWORD;
	};

	private final static Permissions RO = new Permissions(Permission.READ_ENABLED);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	protected FileEntity getEntity(HttpServletRequest req, Permissions permissions) throws LdException {
		FileEntity.Types type = FileEntity.Types.getFromContext(req);
		FileEntity entity = getEntity(req, type, permissions);
		return entity;
	}

	protected FileEntity getEntity(HttpServletRequest req, FileEntity.Types type) throws LdException {
		return getEntity(req, type, new Permissions());
	}

	protected FileEntity getEntity(HttpServletRequest req, FileEntity.Types type, Permissions permissions) throws LdException {
		Admin admin = getComponent(Admin.class);
		HttpSession session = req.getSession(false);
		User user = (User) session.getAttribute(Params.USER.toString());
		FileEntity entity = null;

		switch (type) {
		case ADMIN:
			switch (user.getRole()) {
			case ADMIN:
				entity = admin;
				validateAdminPermissions(permissions, admin, user);
			break;
			default:
				throw new LdException("Unauthorized role");
			}
		break;
		case PLANNER:
			switch (user.getRole()) {
			case ADMIN:
				validateAdminPermissions(permissions, admin, user);
				String plannerId = req.getParameter(Params.PLANNER_ID.toString());
				entity = admin.getPlanner(plannerId);
			break;
			case PLANNER:
				entity = admin.getPlanner(user.getId());
				if (!entity.getPermissions().implies(permissions)) {
					throw new LdException("Permissions denied: " + permissions);
				}
			break;
			default:
				throw new LdException("Unauthorized role");
			}
		break;
		case EVENT:
			switch (user.getRole()) {
			case ADMIN:
				validateAdminPermissions(permissions, admin, user);
				Planner planner = admin.getPlanner(req.getParameter(Params.PLANNER_ID.toString()));
				entity = planner.getEvent(req.getParameter(Params.EVENT_ID.toString()));
			break;
			case PLANNER:
				planner = admin.getPlanner(user.getId());
				if (!planner.getPermissions().implies(permissions)) {
					throw new LdException("Permissions denied: " + permissions);
				}
				entity = planner.getEvent(req.getParameter(Params.EVENT_ID.toString()));
			break;
			case HOST:
				String[] ids = user.getId().split("/");
				planner = admin.getPlanner(ids[0]);
				entity = planner.getEvent(ids[1]);
				if (!entity.getPermissions().implies(permissions)) {
					throw new LdException("Permissions denied: " + permissions);
				}
			break;
			default:
				throw new LdException("Unauthorized role");
			}
		break;
		}
		return entity;
	}

	private void validateAdminPermissions(Permissions permissions, Admin admin, User user) throws LdException {
		if(user.getId().equals("readonly")) {
			if (!RO.implies(permissions)) {
				throw new LdException("Permissions denied: " + permissions);
			}
		} else {
			if (!admin.getPermissions().implies(permissions)) {
				throw new LdException("Permissions denied: " + permissions);
			}
		}
	}

	private MutablePicoContainer getContainer() {
		return (MutablePicoContainer) getServletContext().getAttribute("CONTAINER");
	}

	protected <T> T getComponent(Class<T> clazz) {
		return getContainer().getComponent(clazz);
	}

	protected <T> void streamOut(T data, HttpServletResponse response) throws LdException {
		streamOut(data, response, null);
	}

	protected <T> void streamOut(T data, HttpServletResponse response, String xsl) throws LdException {
		streamOut(data, response, xsl, null);
	}

	protected <T> void streamOut(T data, HttpServletResponse response, String xsl, Map<String, String> headers) throws LdException {
		if (headers != null) {
			Iterator<Entry<String, String>> it = headers.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> header = it.next();
				response.setHeader(header.getKey(), header.getValue());
			}
		}
		if (headers == null || !headers.containsKey("Content-type")) {
			response.setHeader("Content-type", "text/xml; charset=UTF-8");
		}
		XMLUtils xmlUtils = getComponent(XMLUtils.class);
		Transformer transformer = xmlUtils.getTransformer(xsl);
		Source source;
		try {
			source = new DOMSource(xmlUtils.marshal(data));
			Result result = new StreamResult(response.getWriter());
			transformer.transform(source, result);
		} catch (JAXBException e) {
			throw new LdException(e);
		} catch (ParserConfigurationException e) {
			throw new LdException(e);
		} catch (IOException e) {
			throw new LdException(e);
		} catch (TransformerException e) {
			throw new LdException(e);
		}
	}
}
