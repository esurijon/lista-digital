package com.surix.ld.controller;

import java.io.IOException;
import java.net.URL;

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
		URL referer = new URL(request.getHeader("Referer"));
		response.sendRedirect("http://" + referer.getHost());
	}
}
