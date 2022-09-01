package com.surix.ld.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.FileEntity;
import com.surix.ld.model.Permissions;
import com.surix.ld.model.Permissions.Permission;

@SuppressWarnings("serial")
public class RestoreEntity extends OnLineListsServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			FileEntity entity = getEntity(req, new Permissions(Permission.WRITE_ENABLED));

			String returnUrl = getReturnUrl(req);

			String sVersion = req.getParameter("version");
			if (sVersion != null) {
				long version = 0;
				version = Long.parseLong(sVersion);
				entity.restore(version);
				resp.sendRedirect(returnUrl);
			}
		} catch (LdException e) {
			throw new ServletException(e);
		}
	}

	private String getReturnUrl(HttpServletRequest req) {
		String returnUrl = "/event/main.html";

		String urlParams = "?";

		String eventId = req.getParameter(Params.EVENT_ID.toString());
		if (eventId != null) {
			urlParams += Params.EVENT_ID.toString() + "=" + eventId;
			String plannerId = req.getParameter(Params.PLANNER_ID.toString());
			if (plannerId != null) {
				urlParams += "&" + Params.PLANNER_ID.toString() + "=" + plannerId;
			}
		}

		if(urlParams.length()>1){
			returnUrl += urlParams;
		}

		return returnUrl;
	}
}