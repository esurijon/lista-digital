package com.surix.ld.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.surix.ld.exceptions.LdException;
import com.surix.ld.util.Config;
import com.surix.ld.util.FileUtil;
import com.surix.ld.util.IdGenerator;
import com.surix.ld.util.Obfuscator;
import com.surix.ld.util.Pair;
import com.surix.ld.util.ParamValue;
import com.surix.ld.util.XMLUtils;

public class Planner extends FileEntity {

	private static final String PAYMENTS_XSL = "payments/merge.xsl";
	private static final String MERGE_XSL = "planner/merge.xsl";
	private static final String RESET_PASSWORD_XSL = "planner/resetHostPassword.xsl";
	private static final String CHANGE_PASSWORD_XSL = "planner/changeHostPassword.xsl";
	private static final String UPDATE_PLANNER_PERMISSIONS = "planner/updateEventPermissions.xsl";

	private static final String GRANT_USER_XPATH = "/planner/events/event[hosts/host/user=$user and hosts/host/password=$password]";
	private static final String eventByIdExp = "/planner/events/event[@id=$id]";

	private static final String FILE_NAME = "/events.xml.gz";
	private static final String PAYMENTS_FILE_NAME = "/payments.xml.gz";

	private long lastRead;
	private Map<Pair<String, String>, String> grantedHosts = new HashMap<Pair<String,String>, String>();
	private Map<String, Event> events = new HashMap<String, Event>();

	private String id;
	private String name;
	private Config cfg;

	protected Planner(XMLUtils xmlUtils, FileUtil fileUtil, Config cfg, String id, Element node) {
		super(xmlUtils, fileUtil);
		this.cfg = cfg;
		this.id = id;
		this.name = node.getElementsByTagName("name").item(0).getTextContent();
	}

	public String getId() {
		return id;
	}

	@Override
	public String getFileName() {
		return cfg.getCurrentStorage() + id + "/" + FILE_NAME;
	}

	public String getPaymentsFileName() {
		return cfg.getCurrentStorage() + id + "/" + PAYMENTS_FILE_NAME;
	}

	@Override
	public String getFileName(long version) {
		String path;
		if (version > 0) {
			path = cfg.getBackupStorage() + id + "/" + FILE_NAME + "." + Long.toString(version, 16);
		} else {
			path = getFileName();
		}
		return path;
	}

	@Override
	public String getNewVesrionFileName() {
		return cfg.getBackupStorage() + id + "/" + FILE_NAME + "." + IdGenerator.createId();
	}

	@Override
	protected Transformer getMergeTransformer() throws TransformerException {
		Transformer transformer = xmlUtils.getTransformer(
			MERGE_XSL,
			new ParamValue<Object>("planner-id", id),
			new ParamValue<Object>("planner-name", name));
		return transformer;
	}

	@Override
	protected Transformer getResetPasswordTransformer(String user, String uuid) throws TransformerException {
		Transformer transformer = xmlUtils.getTransformer(
			RESET_PASSWORD_XSL,
			new ParamValue<Object>("event-host", user),
			new ParamValue<Object>("uuid", uuid));
		return transformer;
	}

	@Override
	protected Transformer getChangePasswordTransformer(String user, String resetToken, String newPassword) throws TransformerException {
		Transformer transformer = xmlUtils.getTransformer(
			CHANGE_PASSWORD_XSL,
			new ParamValue<Object>("event-host", user),
			new ParamValue<Object>("reset-token", resetToken),
			new ParamValue<Object>("new-password", newPassword));
		return transformer;
	}

	private void cleanExpired() {
		File file = new File(getFileName());
		if (file.lastModified() > lastRead) {
			grantedHosts.clear();
			events.clear();
			lastRead = file.lastModified();
		}
	}
	
	public Event getEvent(String id) throws EntityException {
		cleanExpired();
		Event event = events.get(id);
		if (event == null) {
			event = buildEvent(id);
			addToCache(events, id, event, 20);
		}
		return event;
	}

	private Event buildEvent(String id) throws EntityException {
		try {
			XPath xpath = xmlUtils.getXpath(new ParamValue<Object>("id", id));
			InputSource in = new InputSource(fileUtil.inflateFile(getFileName()));
			Element node = (Element) xpath.evaluate(eventByIdExp, in, XPathConstants.NODE);
			if (node != null) {
				Event event = new Event(xmlUtils, fileUtil, cfg, this.id + "/" + id, node);
				NodeList permList = (NodeList) xmlUtils.getXpath().compile(".//permission").evaluate(node, XPathConstants.NODESET);
				Permissions permissions = parsePermissions(permList);
				event.setPermissions(permissions.mask(this.getPermissions()));
				return event;
			} else {
				throw new EntityNotFoundException(id, Event.class);
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
		String id = grantedHosts.get(pair);
		
		if (id == null) {
			String encPassword;
			try {
				encPassword = Obfuscator.sha1(password);
			} catch (NoSuchAlgorithmException ex) {
				throw new EntityException("Cannot retrieve entity for user" + user, ex);
			}
			XPath xpath = xmlUtils.getXpath(new ParamValue<Object>("user", user), new ParamValue<Object>("password", encPassword));
			try {
				InputSource in = new InputSource(fileUtil.inflateFile(getFileName()));
				Element node = (Element) xpath.evaluate(GRANT_USER_XPATH, in, XPathConstants.NODE);
				if (node != null) {
					id = node.getAttribute("id");
					addToCache(grantedHosts, pair, id, 20);
				}
			} catch (IOException e) {
				throw new EntityException("Cannot retrieve entity for user" + user, e);
			} catch (XPathExpressionException e) {
				throw new EntityException("Cannot retrieve entity for user" + user, e);
			}
		}
		return id != null ? this.id + "/" + id : null;
	}

	@Override
	protected String getPath() {
		return cfg.getBackupStorage() + id;
	}

	@Override
	protected String getPrefix() {
		return FILE_NAME;
	}
	
	public String getName() {
		return name;
	}

	public InputStream loadPayment(boolean deflated) throws IOException {
		InputStream in;
		String file = getPaymentsFileName();
		if (deflated) {
			in = fileUtil.loadFile(file);
		} else {
			in = fileUtil.inflateFile(file);
		}
		return in;
	}

	public synchronized void paymentUpdate(Source xmlSource) throws IOException, TransformerException, LdException {
		OutputStream out = null;
		try {
			out = fileUtil.deflateFile(getPaymentsFileName() + ".temp");
			Result outputTarget = new StreamResult(out);
			Transformer transformer = getUpdatePaymentTransformer();
			transformer.transform(xmlSource, outputTarget);
			transformer = null;
			out.close();

			File current = new File(getPaymentsFileName());
			File modified = new File(getPaymentsFileName() + ".temp");

			if (current.exists()) {
				current.delete();
			}

			// Rename new version
			if (!modified.renameTo(current)) {
				throw new LdException("Error renaming new version, from: " + modified.toString() + " to: " + current.toString());
			}

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

	private Transformer getUpdatePaymentTransformer() throws TransformerException {
		Transformer transformer = xmlUtils.getTransformer(PAYMENTS_XSL, new ParamValue<Object>("planner-id", id));
		return transformer;
	}

	public synchronized void permissionUpdate(String eventId, String timePeriod) throws IOException, TransformerException, LdException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = fileUtil.inflateFile(getFileName());
			Source xmlSource = new StreamSource(in);
			out = fileUtil.deflateFile(getTemporaryFileName());
			Result outputTarget = new StreamResult(out);
			Transformer transformer = xmlUtils.getTransformer(
					UPDATE_PLANNER_PERMISSIONS,
					new ParamValue<Object>("event-id", eventId),
					new ParamValue<Object>("time-period", timePeriod));
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
