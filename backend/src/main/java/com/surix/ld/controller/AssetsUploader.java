package com.surix.ld.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.util.Config;
import com.surix.ld.util.FileUtil;
import com.surix.ld.util.IdGenerator;

@SuppressWarnings("serial")
public class AssetsUploader extends MultipartServlet {

	private static Map<String, String> headers;

	@Override
	public void init() throws ServletException {
		super.init();
		headers = new HashMap<String, String>();
		headers.put("Content-type", "text/html; charset=UTF-8");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String assetType = request.getPathInfo();
		try {
			if ("/plane".equals(assetType)) {
				String fileName = uploadPlane(request);
				streamOut(new ResponseMsg("OK", fileName), response, "upload.xsl", headers);
			}

		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private String uploadPlane(HttpServletRequest request) throws LdException {
		try {
			FileUtil fUtil = getComponent(FileUtil.class);
			Config cfg = getComponent(Config.class);

			String filePath = cfg.get("assets.path") + "planner-assets";
			String fileUri = cfg.get("assets.uri") + "planner-assets";
			String fileName = null;
			String fName = null;
			InputStream in = null;

			FileItemIterator parts = getParts(request);

			while (parts.hasNext()) {
				FileItemStream part = parts.next();
				if ("plane-img".equals(part.getFieldName())) {
					fName = part.getName();
					int t = fName.lastIndexOf('/');
					if(t > 0){
						fName = fName.substring(t+1);
					}
					t = fName.lastIndexOf('\\');
					if(t > 0){
						fName = fName.substring(t+1);
					}
					in = part.openStream();
					break;
				}
			}

			HttpSession session = request.getSession(false);
			String plannerId = "0";
			if(session != null) {
				plannerId = (String) session.getAttribute("ID");
			}
			fileName = plannerId + '/' + fName + "." + IdGenerator.createId();
			File file = new File(filePath + '/' + fileName);
			if(file.getParentFile().exists() || file.getParentFile().mkdirs()){
				FileOutputStream fos = new FileOutputStream(file);
				fUtil.streamOut(in, fos);
				return fileUri + '/' + fileName;
			} else {
				return "/planner-assets/hall.png";
			}
		} catch (Exception e) {
			throw new LdException(e);
		}
	}

}
