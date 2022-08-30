package com.surix.ld.ioc;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

public class SessionListener implements HttpSessionListener {

	private static Logger logger = Logger.getLogger(SessionListener.class);

	public void sessionCreated(HttpSessionEvent ev) {
		logger.debug("Session created");
	}

	public void sessionDestroyed(HttpSessionEvent ev) {
		logger.debug("Session invalidated");		
	}

}
