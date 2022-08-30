package com.surix.ld.model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.surix.ld.controller.OnLineListsServlet.Params;

public enum Role {
	ADMIN("Administrador"),
	PLANNER("Organizar de Eventos"),
	HOST("Anfitrión"),
	GUEST("Invitado");

	private String label;

	private Role(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	public static Role getFromContext(HttpServletRequest context) {
		try {
			HttpSession session = context.getSession(false);
			User user = (User)session.getAttribute(Params.USER.toString());
			return user.getRole();
		} catch (Exception e) {
			return GUEST;
		}		
	}
}
