package com.surix.ld.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.EntityException;
import com.surix.ld.model.FileEntity;
import com.surix.ld.model.Permissions;
import com.surix.ld.model.Permissions.Permission;

@SuppressWarnings("serial")
public class SaverServlet extends OnLineListsServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			Source source = new StreamSource(req.getInputStream());
			FileEntity entity = getEntity(req, new Permissions(Permission.WRITE_ENABLED));

			try {
				entity.save(source);
			} catch (Exception e) {
				throw new EntityException(e);
			}

			req.getRequestDispatcher("/load" + req.getPathInfo()).forward(req, resp);

		} catch (LdException e) {
			throw new ServletException(e);
		}
	}

}