package com.surix.ld.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surix.ld.util.Config;

/**
 * Servlet implementation class Login
 */
@SuppressWarnings("serial")
public class Logout extends OnLineListsServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getSession().invalidate();
		String home = getComponent(Config.class).get("home.url");
		response.sendRedirect(home.substring(home.indexOf('/',7)));
	}
}
