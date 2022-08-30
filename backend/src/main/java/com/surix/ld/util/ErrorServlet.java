package com.surix.ld.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.surix.ld.controller.OnLineListsServlet;

public class ErrorServlet extends OnLineListsServlet {
	private static final long serialVersionUID = 1L;

	Logger logger = Logger.getLogger(ErrorServlet.class);

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Throwable error = (Exception) request.getAttribute("javax.servlet.error.exception");
		if (error != null) {
			logger.error("ERROR", error);
			PrintWriter writer = response.getWriter();
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			printErr(error, writer);
		}
	}

	private void printErr(Throwable error, PrintWriter writer) {
		writer.append("<error type=\"").append(error.getClass().toString()).append("\">");
		writer.append("<message>").append(error.getMessage()).append("</message>");
		writer.write("<stack><![CDATA[");
		error.printStackTrace(writer);
		writer.write("]]></stack>");
		Throwable cause = error.getCause();
		if (cause != null) {
			writer.append("<cause>");
			printErr(cause, writer);
			writer.append("</cause>");
			writer.write("</error>");
		} else {
			writer.write("</error>");
			return;
		}
	}

}
