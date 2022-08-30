package com.surix.ld.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.model.Permissions.Permission;
import com.surix.ld.util.Config;
import com.surix.ld.util.FileUtil;
import com.surix.ld.util.IdGenerator;
import com.surix.ld.util.Obfuscator;
import com.surix.ld.util.Pair;
import com.surix.ld.util.ParamValue;
import com.surix.ld.util.XMLUtils;

public class Admin extends FileEntity {

	private static Logger tracker = Logger.getLogger("Tracker");	
	
	private static final String MERGE_XSLT = "admin/merge.xsl";
	private static final String RESET_PASSWORD_XSL = "admin/resetPlannerPassword.xsl";
	private static final String UPDATE_PLANNER_PERMISSIONS = "admin/updatePlannerPermissions.xsl";
	private static final String CHANGE_PASSWORD_XSL = "admin/changePlannerPassword.xsl";
	private static final String ADD_PLANNER_XSL = "admin/addPlanner.xsl";

	private static final String GRANT_USER_XPATH = "/planners/planner[user=$user and password=$password]";
	private static final String plannerByIdExp = "/planners/planner[@id=$id]";
	
	private static final String FILE_NAME = "planners.xml.gz";

	private long lastRead;
	private Map<Pair<String, String>, String> grantedPlanners = new HashMap<Pair<String,String>, String>();
	private Map<String, Planner> planners = new HashMap<String, Planner>();

	private Config cfg;

	public Admin(XMLUtils xmlUtils, FileUtil fileUtil, Config cfg) {
		super(xmlUtils, fileUtil);
		this.cfg = cfg;
		Permissions p = new Permissions();
		setPermissions(p.compose(Permission.READ_ENABLED).compose(Permission.WRITE_ENABLED));
	}

	@Override
	public String getFileName() {
		return cfg.getCurrentStorage() + FILE_NAME;
	}

	@Override
	public String getFileName(long version) {
		String path;
		if (version > 0) {
			path = cfg.getBackupStorage() + FILE_NAME + "." + Long.toString(version, 16);
		} else {
			path = getFileName();
		}
		return path;
	}

	@Override
	public String getNewVesrionFileName() {
		return cfg.getBackupStorage() + FILE_NAME + "." + IdGenerator.createId();
	}

	@Override
	protected Transformer getMergeTransformer() throws TransformerException {
		Transformer transformer = xmlUtils.getTransformer(
			MERGE_XSLT,
			new ParamValue<Object>("storage-path", cfg.getCurrentStorage()));
		return transformer;
	}

	@Override
	protected Transformer getResetPasswordTransformer(String user, String uuid) throws TransformerException {
		Transformer transformer = xmlUtils.getTransformer(
			RESET_PASSWORD_XSL,
			new ParamValue<Object>("planner", user),
			new ParamValue<Object>("uuid", uuid));
		return transformer;
	}

	@Override
	protected Transformer getChangePasswordTransformer(String user, String resetToken, String newPassword) throws TransformerException {
		Transformer transformer = xmlUtils.getTransformer(
			CHANGE_PASSWORD_XSL,
			new ParamValue<Object>("planner", user),
			new ParamValue<Object>("reset-token", resetToken),
			new ParamValue<Object>("new-password", newPassword));
		return transformer;
	}

	protected Transformer getAddPlannerTransformer() throws TransformerException {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-3:00"));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		String today = getDateFormat().format(cal.getTime());

		Transformer transformer = xmlUtils.getTransformer(
			ADD_PLANNER_XSL, 
			new ParamValue<Object>("today", today));
		return transformer;
	}

	public synchronized boolean addPlanner(InputStream in) throws Exception {
		boolean result = false;
		try {
			Source xmlSource = new StreamSource(in);
			DOMResult output = new DOMResult();
			Transformer transformer = getAddPlannerTransformer();
			transformer.transform(xmlSource, output);

			XPath xpath = xmlUtils.getXpath();
			Double count = (Double) xpath.evaluate("count(//planner)", output.getNode(), XPathConstants.NUMBER);
			if (count > 0) {
				Source xmlource = new DOMSource(output.getNode());
				save(xmlource);
				tracker.info("NEW PLANNER REGISTERED");
				result = true;
			}
			return result;
		} catch (Exception e) {
			throw new EntityException(e);
		}
	}
	
	public Planner getPlanner(String id) throws EntityException {
		cleanExpired();
		Planner planner = planners.get(id);
		if (planner == null) {
			planner = buildPlanner(id);
			addToCache(planners, id, planner, 50);
		}
		return planner;
	}

	private Planner buildPlanner(String id) throws EntityException {
		try {
			XPath xpath = xmlUtils.getXpath(new ParamValue<Object>("id", id));
			InputSource in = new InputSource(fileUtil.inflateFile(getFileName()));
			Element node = (Element) xpath.evaluate(plannerByIdExp, in, XPathConstants.NODE);
			if (node != null) {
				Planner planner = new Planner(xmlUtils, fileUtil, cfg, id, node);
				NodeList permList = (NodeList) xmlUtils.getXpath().compile(".//permission").evaluate(node, XPathConstants.NODESET);
				planner.setPermissions(parsePermissions(permList));
				return planner;
			} else {
				throw new EntityNotFoundException(id, Planner.class);
			}
		} catch (IOException e) {
			throw new EntityException("Cannot retrieve entity " + id, e);
		} catch (XPathExpressionException e) {
			throw new EntityException("Cannot retrieve entity " + id, e);
		}
	}

	@Override
	public String grantUser(String user, String password) throws EntityException {
		cleanExpired();
		
		Pair<String, String> pair = new Pair<String, String>(user, password);
		String id = grantedPlanners.get(pair);
		
		if (id == null) {
			String encPassword;
			try {
				encPassword = Obfuscator.sha1(password);
			} catch (NoSuchAlgorithmException ex) {
				throw new EntityException("Cannot retrieve entity for user" + user, ex);
			}
			XPath xpath = xmlUtils.getXpath(new ParamValue<Object>("user", user), new ParamValue<Object>("password", encPassword ));
			try {
				InputSource in = new InputSource(fileUtil.inflateFile(getFileName()));
				Element node = (Element) xpath.evaluate(GRANT_USER_XPATH, in, XPathConstants.NODE);
				if (node != null) {
					id = node.getAttribute("id");
					addToCache(grantedPlanners, pair, id, 50);
				}
			} catch (IOException e) {
				throw new EntityException("Cannot retrieve entity for user" + user, e);
			} catch (XPathExpressionException e) {
				throw new EntityException("Cannot retrieve entity for user" + user, e);
			}
		}
		return id;
	}

	private void cleanExpired() {
		File file = new File(getFileName());
		if (file.lastModified() > lastRead) {
			grantedPlanners.clear();
			planners.clear();
			lastRead = file.lastModified();
		}
	}

	public boolean grantAdmin(String user, String password) {
		String adminUser = cfg.get("admin.user");
		String adminPassword = cfg.get("admin.password");
		return(adminUser.equals(user) && adminPassword.equals(password));
	}

	public boolean grantReadOnlyAdmin(String user, String password) {
		String adminUser = cfg.get("admin.readonly.user");
		String adminPassword = cfg.get("admin.readonly.password");
		return(adminUser.equals(user) && adminPassword.equals(password));
	}

	@Override
	protected String getPath() {
		return cfg.getBackupStorage();
	}

	@Override
	protected String getPrefix() {
		return FILE_NAME;
	}

	@Override
	public String getName() {
		return "planners";
	}

	public synchronized void permissionUpdate(String plannerId, String expirationDate) throws IOException, TransformerException, LdException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = fileUtil.inflateFile(getFileName());
			Source xmlSource = new StreamSource(in);
			out = fileUtil.deflateFile(getTemporaryFileName());
			Result outputTarget = new StreamResult(out);
			Transformer transformer = xmlUtils.getTransformer(
					UPDATE_PLANNER_PERMISSIONS,
					new ParamValue<Object>("planner-id", plannerId),
					new ParamValue<Object>("expiration-date", expirationDate));
			transformer.transform(xmlSource, outputTarget);
			out.close();
			backupEntity();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new EntityException(e);
				}
			}
		}
	}
}
