package com.surix.ld.controllers;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.surix.ld.controller.OnLineListsServlet.Params;
import com.surix.ld.exceptions.LdException;
import com.surix.ld.ioc.Mapping;
import com.surix.ld.model.Role;
import com.surix.ld.model.User;
import com.surix.ld.util.Config;
import com.surix.ld.util.Obfuscator;
import com.surix.ld.util.XMLUtils;

/**
 * Servlet implementation class AdminUtils
 */
public class AdminUtils {
	private static final long serialVersionUID = 1L;

	private Config cfg;
	private XMLUtils xmlUtils;

	public AdminUtils(Config cfg, XMLUtils xmlUtils) {
		this.cfg = cfg;
		this.xmlUtils = xmlUtils;
	}

	private void isAdmin(HttpServletRequest request) throws LdException {
		HttpSession session = request.getSession(false);
		User user = (User) session.getAttribute(Params.USER.toString());
		if (!Role.ADMIN.equals(user.getRole())) {
			throw new LdException("UNAUTHORIZED ROLE");
		}
	}

	@Mapping(uri = "/admin/utils/sha")
	public void sha1(HttpServletRequest request, HttpServletResponse response) throws LdException {
		isAdmin(request);
		String sha1;
		try {
			sha1 = Obfuscator.sha1(request.getParameter("password"));
		} catch (NoSuchAlgorithmException e) {
			sha1 = e.getMessage();
		}
		ResponseMsg msg = new ResponseMsg("OK", sha1);
		xmlUtils.streamOut(msg, response);
	}

	@Mapping(uri = "/admin/utils/sha")
	public void md5(HttpServletRequest request, HttpServletResponse response) throws LdException {
		isAdmin(request);
		String md5;
		try {
			md5 = Obfuscator.md5(request.getParameter("msg"));
		} catch (NoSuchAlgorithmException e) {
			md5 = e.getMessage();
		}
		ResponseMsg msg = new ResponseMsg("OK", md5);
		xmlUtils.streamOut(msg, response);
	}

	@Mapping(uri = "/admin/utils/reload")
	public void reload(HttpServletRequest request, HttpServletResponse response) throws IOException, LdException {
		isAdmin(request);
		cfg.reload();
		ResponseMsg msg = new ResponseMsg("OK", "PROPERTIES RELOADED");
		xmlUtils.streamOut(msg, response);
	}
}
