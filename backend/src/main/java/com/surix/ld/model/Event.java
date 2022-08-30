package com.surix.ld.model;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.surix.ld.util.Config;
import com.surix.ld.util.FileUtil;
import com.surix.ld.util.IdGenerator;
import com.surix.ld.util.ParamValue;
import com.surix.ld.util.XMLUtils;

public class Event extends FileEntity {

	private static final String MERGE_XSL = "event/merge.xsl";

	private String id;
	private String name = "";

	private Config cfg;

	protected Event(XMLUtils xmlUtils, FileUtil fileUtil, Config cfg, String id, Element node) {
		super(xmlUtils, fileUtil);
		this.cfg = cfg;
		this.id = id;
		NodeList hosts = node.getElementsByTagName("host");
		for(int i = 0; i<hosts.getLength(); i++) {
			Element host = (Element) hosts.item(i);
			this.name += host.getElementsByTagName("lastname").item(0).getTextContent() + "_";
		}
		this.name += node.getElementsByTagName("date").item(0).getTextContent();
	}

	@Override
	public String getFileName() {
		return cfg.getCurrentStorage() + "/" + id + ".xml.gz";
	}

	@Override
	public String getFileName(long version) {
		String path;
		if (version > 0) {
			path = cfg.getBackupStorage() + "/" + id + ".xml.gz"+ "." + Long.toString(version, 16);
		} else {
			path = getFileName();
		}
		return path;
	}

	@Override
	public String getNewVesrionFileName() {
		return cfg.getBackupStorage() + "/" + id + ".xml.gz" + "." + IdGenerator.createId();
	}

	@Override
	protected Transformer getMergeTransformer() throws TransformerException {
		Transformer transformer = xmlUtils.getTransformer(
			MERGE_XSL,
			new ParamValue<Object>("event-id", id));
		return transformer;
	}

	@Override
	protected Transformer getResetPasswordTransformer(String user, String uuid) throws TransformerException {
		return null;
	}

	@Override
	public synchronized String resetPassword(String user) throws EntityException {
		throw new EntityException("There is  no password to reset");
	}

	@Override
	protected Transformer getChangePasswordTransformer(String user, String resetToken, String newPassword) throws TransformerException {
		return null;
	}

	@Override
	public synchronized boolean changePassword(String user, String uuid, String newPassword) throws EntityException {
		throw new EntityException("There is  no password to change");
	}

	@Override
	public String grantUser(String user, String password) throws EntityException {
		return null;
	}

	@Override
	protected String getPath() {
		return cfg.getBackupStorage() + id.split("/")[0];
	}

	@Override
	protected String getPrefix() {
		return id.split("/")[1];
	}

	@Override
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
}