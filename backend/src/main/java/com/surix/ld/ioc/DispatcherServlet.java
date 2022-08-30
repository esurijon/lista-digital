package com.surix.ld.ioc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picocontainer.MutablePicoContainer;

public class DispatcherServlet extends HttpServlet {

	private Map<String, Method> mappings = new HashMap<String, Method>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		MutablePicoContainer container = (MutablePicoContainer) getServletContext().getAttribute("CONTAINER");
		try {
			String pckgname = config.getInitParameter("lookup-package");
			List<Class<?>> classes = getClasses(pckgname);
			for (Class<?> clazz : classes) {
				if (clazz.isAnnotationPresent(Controller.class)) {
					container.addComponent(clazz);
					for (Method method : clazz.getMethods()) {
						Mapping mapping = method.getAnnotation(Mapping.class);
						System.out.println(method.getName());
						if (mapping != null && Modifier.isPublic(method.getModifiers())) {
							mappings.put(mapping.uri(), method);
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MutablePicoContainer container = (MutablePicoContainer) getServletContext().getAttribute("CONTAINER");
		String uri = req.getPathInfo();
		Method method = mappings.get(uri);
		Object obj = container.getComponent(method.getDeclaringClass());
		try {
			method.invoke(obj, req, resp);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}

	public List<Class<?>> getClasses(String pckgname) throws ClassNotFoundException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		// Get a File object for the package
		File directory = null;
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			String path = '/' + pckgname.replace('.', '/');
			URL resource = cld.getResource(path);
			if (resource == null) {
				throw new ClassNotFoundException("No resource for " + path);
			}
			directory = new File(resource.getFile());
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(pckgname + " (" + directory + ") does not appear to be a valid package");
		}
		if (directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					classes.add(Class.forName(pckgname + '.' + files[i].substring(0, files[i].length() - 6)));
				}
			}
		} else {
			throw new ClassNotFoundException(pckgname + " does not appear to be a valid package");
		}
		return classes;
	}
}
