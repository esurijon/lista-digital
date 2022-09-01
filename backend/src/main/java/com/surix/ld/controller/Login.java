package com.surix.ld.controller;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.filter.SecurityFilter;
import com.surix.ld.model.Admin;
import com.surix.ld.model.EntityException;
import com.surix.ld.model.Planner;
import com.surix.ld.model.Role;
import com.surix.ld.model.User;
import com.surix.ld.util.TokenSecuence;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet implementation class Login
 */
@SuppressWarnings("serial")
public class Login extends OnLineListsServlet {

	@XmlRootElement(name = "login")
	public static class LoginResult {
		private String status;
		private String role;

		public LoginResult() {
		}

		public LoginResult(String status, String role) {
			this.status = status;
			this.role = role;
		}

		@XmlElement(name = "status")
		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		@XmlElement(name = "role")
		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}
	}

	private String role;

	@Override
	public void init() throws ServletException {
		super.init();
		role = getInitParameter("role");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		try {
			String plannerId = request.getParameter(Params.PLANNER_ID.toString());
			String user = request.getParameter(Params.USER.toString());
			String password = request.getParameter(Params.PASSWORD.toString());
			String callback = request.getParameter("callback");

			HttpSession session = request.getSession(true);
			boolean granted = role.equals("user") ? validateUser(plannerId, user, password, session) : validateAdmin(
					user, password, session);
			if (granted) {
				TokenSecuence tg = new TokenSecuence();
				session.setAttribute(SecurityFilter.TOKEN_GENERATOR, tg);
				Cookie authCookie = getAuthCookie(request);
				authCookie.setValue(tg.nextToken());
				response.addCookie(authCookie);
				String redirect = request.getParameter("redirect");
				if (redirect != null) {
					response.sendRedirect(redirect);
				} else {
					respondOk(response, Role.getFromContext(request), callback);
				}
			} else {
				respondWrong(request, response, callback);
			}
		} catch (LdException e) {
			throw new ServletException(e);
		}
	}

	private void logoutCurrent(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(SecurityFilter.TOKEN_GENERATOR);
			session.removeAttribute(Params.ROLE.toString());
			session.removeAttribute(Params.USER.toString());
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					if (SecurityFilter.SECURITY_COOKIE.equals(cookies[i].getName())) {
						cookies[i].setMaxAge(0);
						break;
					}
				}
			}
		}
	}

	private void respondWrong(HttpServletRequest request, HttpServletResponse response, String callback)
			throws IOException, LdException {
		logoutCurrent(request);

		PrintWriter writer = response.getWriter();
		if (callback != null) {
			response.setHeader("Content-type", "text/javascript");
			writer.append(callback).append("({");
			writer.append("status: '").append("error").append("',");
			writer.append("type: '").append("CANT_LOG_USER").append("',");
			writer.append("message: '").append("cant log user").append("'");
			writer.append("})");
		} else {
			LoginResult result = new LoginResult("NOT_LOGGED", Role.GUEST.toString());
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			streamOut(result, response);
		}
	}

	private void respondOk(HttpServletResponse response, Role role, String callback) throws IOException, LdException {
		PrintWriter writer = response.getWriter();
		if (callback != null) {
			response.setHeader("Content-type", "text/javascript");
			writer.append(callback).append("({");
			writer.append("status: '").append("ok").append("',");
			writer.append("role: '").append(role.toString()).append("'");
			writer.append("})");
		} else {
			LoginResult result = new LoginResult("LOGGED", role.toString());
			streamOut(result, response);
		}
	}

	private Cookie getAuthCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		Cookie cookie = null;
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (SecurityFilter.SECURITY_COOKIE.equals(cookies[i].getName())) {
					cookie = cookies[i];
					break;
				}
			}
		}
		if (cookie == null) {
			cookie = new Cookie(SecurityFilter.SECURITY_COOKIE, null);
			cookie.setPath("/");
		}
		return cookie;
	}

	private boolean validateUser(String plannerId, String user, String password, HttpSession session)
			throws EntityException {
		boolean result = false;
		Admin admin = getComponent(Admin.class);
		Role role;

		String id = admin.grantUser(user, password);
		if (id != null) {
			role = Role.PLANNER;
		} else {
			Planner planner = admin.getPlanner(plannerId);
			id = planner.grantUser(user, password);
			role = Role.HOST;
		}

		if (id != null) {
			User usr = new User(id, user, role);
			session.setAttribute(Params.USER.toString(), usr);
			result = true;
		}
		return result;
	}

	private boolean validateAdmin(String user, String password, HttpSession session) throws EntityException {
		boolean result = false;
		Admin admin = getComponent(Admin.class);
		if (admin.grantAdmin(user, password)) {
			User usr = new User("full", user, Role.ADMIN);
			session.setAttribute(Params.USER.toString(), usr);
			result = true;
		}
		return result;
	}
}
