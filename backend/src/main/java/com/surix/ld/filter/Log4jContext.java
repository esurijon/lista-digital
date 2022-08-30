package com.surix.ld.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.MDC;

import com.surix.ld.controller.OnLineListsServlet.Params;
import com.surix.ld.model.User;

/**
 * Servlet Filter implementation class Log4jContext
 */
public class Log4jContext implements Filter {

    /**
     * Default constructor. 
     */
    public Log4jContext() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		User user = getUser(req);
		if(user != null) {
			MDC.put("role", user.getRole());
			MDC.put("user", user.getEmail());
			MDC.put("id", user.getId());
		} else {
			MDC.clear();
		}
		MDC.put("agent",req.getHeader("User-Agent"));
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	private User getUser(HttpServletRequest request) {
		User user = null;
		HttpSession session = request.getSession(false);
		if (session != null) {
			user = (User) session.getAttribute(Params.USER.toString());
		}
		return user;
	}
}
