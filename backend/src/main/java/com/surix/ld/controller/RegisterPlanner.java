package com.surix.ld.controller;


import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surix.ld.model.Admin;

@SuppressWarnings("serial")
public class RegisterPlanner extends OnLineListsServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Admin admin = getComponent(Admin.class);
		try {
			InputStream in = request.getInputStream();
			if(admin.addPlanner(in)) {
				streamOut(new ResponseMsg("PLANNER_REGISTERED", "PLANNER_REGISTERED"), response);
			} else {
				streamOut(new ResponseMsg("DUPLICATED_PLANNER", "Planner name is aleready used"), response);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
