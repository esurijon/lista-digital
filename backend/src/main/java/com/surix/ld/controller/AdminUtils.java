package com.surix.ld.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.Role;
import com.surix.ld.model.User;
import com.surix.ld.util.Config;
import com.surix.ld.util.Obfuscator;

/**
 * Servlet implementation class AdminUtils
 */
public class AdminUtils extends OnLineListsServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public AdminUtils() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String operation = request.getRequestURI();
		operation = operation.replace(request.getContextPath() + "/admin/utils/", "");

		HttpSession session = request.getSession(false);
		User user = (User) session.getAttribute(Params.USER.toString());
		if (Role.ADMIN.equals(user.getRole()) && user.getId().equals("full")) {
			try {
				if ("reload".equals(operation)) {
					reload(request, response);
				} else if ("sha1".equals(operation)) {
					sha1(request, response);
				} else if ("md5".equals(operation)) {
					md5(request, response);
				}
			} catch (LdException e) {
				throw new ServletException(e);
			}
		} else {
			throw new ServletException("UNAUTHORIZED ROLE");
		}

	}

	private void sha1(HttpServletRequest request, HttpServletResponse response)
			throws LdException {
		String sha1;
		try {
			sha1 = Obfuscator.sha1(request.getParameter("password"));
		} catch (NoSuchAlgorithmException e) {
			sha1 = e.getMessage();
		}
		ResponseMsg msg = new ResponseMsg("OK", sha1);
		streamOut(msg, response);
	}

	private void md5(HttpServletRequest request, HttpServletResponse response) throws LdException {
		String md5;
		try {
			md5 = Obfuscator.md5(request.getParameter("msg"));
		} catch (NoSuchAlgorithmException e) {
			md5 = e.getMessage();
		}
		ResponseMsg msg = new ResponseMsg("OK", md5);
		streamOut(msg, response);
	}

	private void reload(HttpServletRequest request, HttpServletResponse response)
			throws IOException, LdException {
		Config cfg = getComponent(Config.class);
		cfg.reload();
		ResponseMsg msg = new ResponseMsg("OK", "PROPERTIES RELOADED");
		streamOut(msg, response);
	}

}
