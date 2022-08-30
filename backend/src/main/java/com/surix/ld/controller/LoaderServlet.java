package com.surix.ld.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.FileEntity;
import com.surix.ld.model.Permissions;
import com.surix.ld.model.Permissions.Permission;
import com.surix.ld.util.FileUtil;

@SuppressWarnings("serial")
public class LoaderServlet extends OnLineListsServlet {

	protected boolean supportsCompression(HttpServletRequest request) {
		String encoding = request.getHeader("Accept-Encoding");
		return encoding.contains("gzip");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			FileUtil fileUtil = getComponent(FileUtil.class);
			FileEntity entity = getEntity(req, new Permissions(Permission.READ_ENABLED));

			if (supportsCompression(req)) {
				resp.setHeader("Content-Encoding", "gzip");
			}

			String sVersion = req.getParameter("version");
			long version = 0;
			if (sVersion != null) {
				version = Long.parseLong(sVersion);
			}

			resp.setHeader("Expires", "0");
			fileUtil.streamOut(entity.load(version, supportsCompression(req)), resp.getOutputStream());

		} catch (LdException e) {
			throw new ServletException(e);
		}
	}
}