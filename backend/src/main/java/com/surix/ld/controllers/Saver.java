package com.surix.ld.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.surix.ld.controller.OnLineListsServlet.Params;
import com.surix.ld.ioc.Mapping;
import com.surix.ld.model.Admin;
import com.surix.ld.model.User;

@SuppressWarnings("serial")
public class Saver {
	
	
	@Mapping(uri = "save/admin")
	public void saveAdmin(HttpServletRequest req, HttpServletResponse resp) {
		HttpSession session = req.getSession(false);
		User user = (User) session.getAttribute(Params.USER.toString());
		try {
			Source source = new StreamSource(req.getInputStream());
			Admin admin = null;//getEntity(req, new Permissions(Permission.WRITE_ENABLED));
			admin.save(source);
			req.getRequestDispatcher("/load" + req.getPathInfo()).forward(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Mapping(uri = "save/planner")
	public void savePlanner(HttpServletRequest req, HttpServletResponse resp) {
		
	}

	@Mapping(uri = "save/event")
	public void saveEvent(HttpServletRequest req, HttpServletResponse resp) {
		
	}


}