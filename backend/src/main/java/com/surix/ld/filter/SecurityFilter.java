package com.surix.ld.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.surix.ld.util.TokenSecuence;

/**
 * Servlet Filter implementation class SecurityFilter
 */
public class SecurityFilter implements Filter {

	public static final String SECURITY_COOKIE = "SEC_TOKEN";
	public static final String TOKEN_GENERATOR = "TOKEN_GENERATOR";

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		if (isAuthenticated((HttpServletRequest) request, (HttpServletResponse) response)) {
			chain.doFilter(request, response);
		} else {
			((HttpServletResponse)response).setStatus(HttpServletResponse.SC_FORBIDDEN);
			PrintWriter writer = response.getWriter();
			writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
			writer.append("<error type=\"").append("USER_NOT_AUTHENTICATED").append("\">");
			writer.append("<message>").append("User is not authenticated").append("</message>");
			writer.write("</error>");
		}
	}

	private boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) {
		boolean isAuth = false;
		HttpSession session = request.getSession(false);
		if (session != null) {
			Cookie cookie = getSecurityCookie(request.getCookies());
			if (cookie != null) {
				TokenSecuence tokenSecuence = (TokenSecuence) session.getAttribute(TOKEN_GENERATOR);
				if (tokenSecuence != null && tokenSecuence.lastToken().equals(cookie.getValue())) {
					cookie.setValue(tokenSecuence.nextToken());
					response.addCookie(cookie);
					isAuth = true;
				}
			}
		}
		return isAuth || session != null;
	}

	private Cookie getSecurityCookie(Cookie[] cookies) {
		Cookie cookie = null;
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (SECURITY_COOKIE.equals(cookies[i].getName())) {
					cookie = cookies[i];
					break;
				}
			}
		}
		return cookie;
	}
}
