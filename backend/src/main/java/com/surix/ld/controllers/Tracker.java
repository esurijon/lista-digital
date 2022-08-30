package com.surix.ld.controllers;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.surix.ld.ioc.Mapping;
import com.surix.ld.util.Obfuscator;
import com.surix.ld.util.XMLUtils;

@SuppressWarnings("serial")
public class Tracker {

	private static Logger logger = Logger.getLogger("Tracker");

	private XMLUtils xmlUtils;

	public Tracker(XMLUtils xmlUtils) {
		this.xmlUtils = xmlUtils;
	}

	@Mapping(uri = "/tracker/report")
	public void report(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session = req.getSession(false);
		if (session != null) {
			StringBuilder msg = new StringBuilder();
			appendParams(req.getParameterMap(), msg);
			logger.info(msg.toString());
			xmlUtils.streamOut(new ResponseMsg("MESSAGE_TRACKED", "MESSAGE_TRACKED"), resp);
		}
	}

	@Mapping(uri = "/tracker/track")
	public void track(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		StringBuilder msg = new StringBuilder();
		String encodedParams = req.getQueryString();
		String decodedParams = Obfuscator.base64Decode(encodedParams);
		logger.info(decodedParams);
		xmlUtils.streamOut(new ResponseMsg("MESSAGE_TRACKED", "MESSAGE_TRACKED"), resp);
	}

	private void appendParams(Map<String, Object> params, StringBuilder msg) {
		Set<Entry<String, Object>> entries = params.entrySet();
		Iterator<Entry<String, Object>> iterator = entries.iterator();
		while (iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			msg.append(entry.getKey()).append(": ");
			String[] values = (String[]) entry.getValue();
			if (values.length == 1) {
				msg.append(values[0]);
			} else {
				msg.append("[");
				for (int i = 0; i < values.length; i++) {
					msg.append(values[i]);
					if (i < values.length) {
						msg.append(", ");
					}
				}
				msg.append("]");
			}
			msg.append("; ");
		}
	}

}
