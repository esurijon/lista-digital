package com.surix.ld.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.Admin;
import com.surix.ld.model.Planner;
import com.surix.ld.model.Role;
import com.surix.ld.util.Config;
import com.surix.ld.util.MailSender;
import com.surix.ld.util.Obfuscator;

@SuppressWarnings("serial")
public class ResetPassword extends OnLineListsServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String user = req.getParameter(Params.USER.toString());
		String plannerId = req.getParameter(Params.PLANNER_ID.toString());

		Admin admin = getComponent(Admin.class);
		try {
			boolean isHostUser = false;
			Planner planner = admin.getPlanner(plannerId);
			String uuid = admin.resetPassword(user);
			String plannerName = planner.getName();
			
			if (uuid == null) {
				uuid = planner.resetPassword(user);
				isHostUser = true;
			}

			if (uuid != null) {
				Config conf = getComponent(Config.class);

				String role = isHostUser ? Role.HOST.toString() : Role.PLANNER.toString();
				String changePasswordParams = "PLANNER=" + plannerName + "&" + Params.USER + "=" + user + "&" + Params.RESET_TOKEN + "=" + uuid.toString() + "&" + Params.PLANNER_ID + "=" + plannerId + "&ROLE=" + role;
				changePasswordParams = Obfuscator.base64Encode(changePasswordParams);
				String queryString = Obfuscator.base64Encode((Params.CHANGE_PASSWORD + "=" + changePasswordParams));
				String changePasswordURL = conf.getExternalLandingPageUrl() + "?" + queryString;

				MailSender ms = getComponent(MailSender.class);
				ms.sendResetPasswordMail(user, plannerName, changePasswordURL);

				streamOut(new ResponseMsg("PASSWORD_RESET", "Password was succesfully reset"), resp);
			} else {
				streamOut(new ResponseMsg("NO_SUCH_USER", "No such user"), resp);
			}
		} catch (LdException e) {
			throw new ServletException(e);
		}
	}
}
