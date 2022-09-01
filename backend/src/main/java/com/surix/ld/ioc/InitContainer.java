package com.surix.ld.ioc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoBuilder;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.Admin;
import com.surix.ld.util.Config;
import com.surix.ld.util.FileUtil;
import com.surix.ld.util.MailSender;
import com.surix.ld.util.URIResolverImpl;
import com.surix.ld.util.XMLUtils;
import com.surix.ld.util.XslExtensions;

public class InitContainer implements ServletContextListener {

	private static Logger logger = Logger.getLogger(InitContainer.class);

	public void contextDestroyed(ServletContextEvent ctx) {
		logger.info("Context is being destroyed");
	}

	public void contextInitialized(ServletContextEvent ctx) {
		try {
			MutablePicoContainer picoContainer = new PicoBuilder().withCaching().build();
			picoContainer.addComponent(Config.class);
			picoContainer.getComponent(Config.class);
			picoContainer.addComponent(FileUtil.class);
			picoContainer.addComponent(XMLUtils.class);
			picoContainer.addComponent(Admin.class);
			picoContainer.addComponent(MailSender.class);
			picoContainer.addComponent(URIResolverImpl.class);
			XslExtensions.init(picoContainer.getComponent(Config.class), picoContainer.getComponent(FileUtil.class), picoContainer.getComponent(MailSender.class));
			checkEnv(picoContainer);
			ctx.getServletContext().setAttribute("CONTAINER", picoContainer);
			logger.info("Context started succesfully");
		} catch (Error err) {
			logger.fatal("Error initializing context", err);
			throw err;
		} catch (RuntimeException rex) {
			logger.fatal("Error initializing context", rex);
			throw rex;
		} catch (Throwable t) {
			logger.fatal("Error initializing context", t);
		}
	}

	private void checkEnv(MutablePicoContainer container) throws LdException {
		Config cfg = container.getComponent(Config.class);
		File backupDir = new File(cfg.getBackupStorage());
		directoryCheck(backupDir);
		File currentDir = new File(cfg.getCurrentStorage());
		directoryCheck(currentDir);
		plannersCheck(container.getComponent(FileUtil.class), currentDir);
		defaultPalnnerCheck(container.getComponent(FileUtil.class), currentDir);
		demoEventCheck(container.getComponent(FileUtil.class), currentDir);
	}

	private void directoryCheck(File dir) throws LdException {
		if (!dir.isDirectory()) {
			logger.info("Directory " + dir + " does not exists. Creating dir");
			if (!dir.mkdirs()) {
				throw new LdException("Can't create directory: " + dir);
			}
			dir.setWritable(true, false);
			dir.setReadable(true, false);
			dir.setExecutable(true, false);
		}
	}

	private void plannersCheck(FileUtil fUtil, File currentDir) throws LdException {
		String planners = currentDir.getPath() + "/planners.xml.gz";
		File plannersFile = new File(planners);
		if (!plannersFile.isFile()) {
			logger.info("Planners file does not exists. Creating file");
			try {
				InputStream in = getClass().getResourceAsStream("/planners.xml");
				OutputStream out = fUtil.deflateFile(planners);
				fUtil.streamOut(in, out);
			} catch (IOException e) {
				throw new LdException("Can't create file: " + planners, e);
			}
			plannersFile.setReadable(true, false);
			plannersFile.setWritable(true, false);
		}
	}

	private void defaultPalnnerCheck(FileUtil fUtil, File currentDir) throws LdException {
		String defaultPlanner = currentDir.getPath() + "/0/events.xml.gz";
		File defaultPlannerFile = new File(defaultPlanner);
		if (!defaultPlannerFile.isFile()) {
			logger.info("Default planner file does not exists. Creating file");
			defaultPlannerFile.getParentFile().mkdirs();
			try {
				InputStream in = getClass().getResourceAsStream("/default_planner.xml");
				OutputStream out = fUtil.deflateFile(defaultPlanner);
				fUtil.streamOut(in, out);
			} catch (IOException e) {
				throw new LdException("Can't create file: " + defaultPlanner, e);
			}
			defaultPlannerFile.setReadable(true, false);
			defaultPlannerFile.setWritable(true, false);
		}

		String defaultPlannerPmts = currentDir.getPath() + "/0/payments.xml.gz";
		File defaultPlannerPmtsFile = new File(defaultPlannerPmts);
		if (!defaultPlannerPmtsFile.isFile()) {
			logger.info("Default planner payments file does not exists. Creating file");
			try {
				InputStream in = getClass().getResourceAsStream("/default_payments.xml");
				OutputStream out = fUtil.deflateFile(defaultPlannerPmts);
				fUtil.streamOut(in, out);
			} catch (IOException e) {
				throw new LdException("Can't create file: " + defaultPlannerPmts, e);
			}
			defaultPlannerPmtsFile.setReadable(true, false);
			defaultPlannerPmtsFile.setWritable(true, false);
		}
	}

	private void demoEventCheck(FileUtil fUtil, File currentDir) throws LdException {
		String demoEvent = currentDir.getPath() + "/0/0.xml.gz";
		File demoEventFile = new File(demoEvent);
		if (!demoEventFile.isFile()) {
			logger.info("Demo event file does not exists. Creating file");
			try {
				InputStream in = getClass().getResourceAsStream("/demo_event.xml");
				OutputStream out = fUtil.deflateFile(demoEvent);
				fUtil.streamOut(in, out);
			} catch (IOException e) {
				throw new LdException("Can't create file: " + demoEvent, e);
			}
			demoEventFile.setReadable(true, false);
			demoEventFile.setWritable(true, false);
		}
	}
}
