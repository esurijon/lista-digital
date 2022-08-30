package com.surix.ld.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.surix.ld.util.FileUtil;

@SuppressWarnings("serial")
public abstract class MultipartServlet extends OnLineListsServlet {

	protected FileItemIterator getParts(HttpServletRequest request) throws FileUploadException, IOException {
		FileItemIterator fileItems = null;
		if (ServletFileUpload.isMultipartContent(request)) {
			ServletFileUpload servletFileUpload = new ServletFileUpload();
			servletFileUpload.setSizeMax(1024 * 1024);
			fileItems = servletFileUpload.getItemIterator(request);
		}
		return fileItems;
	}

	protected Map<String, String> getParameterMap(FileItemIterator parts, String... parameter) throws FileUploadException, IOException {
		List<String> params = Arrays.asList(parameter);
		Map<String, String> parameterMap = new HashMap<String, String>(parameter.length);
		FileUtil fUtil = getComponent(FileUtil.class);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (parts.hasNext() && parameterMap.size() < params.size()) {
			FileItemStream part = parts.next();
			String key = part.getFieldName();
			if(params.contains(key)) {
				baos.reset();
				InputStream in = part.openStream();
				fUtil.streamOut(in, baos);
				parameterMap.put(key, baos.toString());
			}
		}
		return parameterMap;
	}
}
