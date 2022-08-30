package com.surix.ld.controller;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.Admin;
import com.surix.ld.model.FileEntity;
import com.surix.ld.model.Role;
import com.surix.ld.util.Obfuscator;

@SuppressWarnings("serial")
public class ChangePassword extends OnLineListsServlet {

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Parse into UUID in order to validate parameter format
		String encodedParams = req.getParameter(Params.ENCODED.toString());
		String decodedParams = Obfuscator.base64Decode(encodedParams);

		UUID uuid = UUID.fromString(extractParam(decodedParams, Params.RESET_TOKEN.toString()));
		String user = extractParam(decodedParams, Params.USER.toString());
		String plannerId = extractParam(decodedParams, Params.PLANNER_ID.toString());
		Role role = Role.valueOf(extractParam(decodedParams, "ROLE"));
		String newPassword = req.getParameter(Params.NEW_PASSWORD.toString());

		try {
			Admin admin = getComponent(Admin.class);
			FileEntity entity;
			switch (role) {
			case PLANNER:
				entity = admin;
			break;
			case HOST:
				entity = admin.getPlanner(plannerId);
			break;
			default:
				throw new LdException("UNAUTHORIZED ROLE");
			}
			if(entity.changePassword(user, uuid.toString(), newPassword)) {
				streamOut(new ResponseMsg("PASSWORD_CHANGED", "Password changed succesfully"), resp);
			} else {
				streamOut(new ResponseMsg("NO_SUCH_USER", "No such user or token is expired"), resp);
			}
		} catch (LdException e) {
			throw new ServletException(e);
		}
	}

	private static String extractParam(String text, String param) {
		String value = null;
		int from = text.indexOf(param) + param.length() + 1;
		if (from != -1 && text.charAt(from - 1) == '=') {
			int to = text.indexOf("&", from);
			if (to == -1) {
				to = text.length();
			}
			value = text.substring(from, to);
		}
		return value;
	}
}
