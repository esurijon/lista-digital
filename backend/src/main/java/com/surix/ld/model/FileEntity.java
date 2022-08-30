package com.surix.ld.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
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
import com.surix.ld.model.Permissions.Permission;
import com.surix.ld.util.FileUtil;
import com.surix.ld.util.Obfuscator;
import com.surix.ld.util.ParamValue;
import com.surix.ld.util.XMLUtils;

public abstract class FileEntity {

	protected Permissions permissions;
	protected XMLUtils xmlUtils;
	protected FileUtil fileUtil;
	private SimpleDateFormat dateFormat;

	public enum Types {
		ADMIN(Admin.class), PLANNER(Planner.class), EVENT(Event.class);

		private Class<? extends FileEntity> clazz;

		private Types(Class<? extends FileEntity> clazz) {
			this.clazz = clazz;
		}

		public Class<? extends FileEntity> getType() {
			return clazz;
		}

		public static Types getFromContext(HttpServletRequest context) {
			int from = 1;
			String pathInfo = context.getPathInfo();
			int to = pathInfo.indexOf('/', from);
			if (from > to) {
				to = pathInfo.length();
			}
			String entity = pathInfo.substring(from, to).toUpperCase();
			return Types.valueOf(entity);
		}
	}

	@SuppressWarnings("unused")
	private FileEntity() {
	}

	protected FileEntity(XMLUtils xmlUtils, FileUtil fileUtil) {
		this.xmlUtils = xmlUtils;
		this.fileUtil = fileUtil;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd Z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-3:00"));
	}

	abstract public String grantUser(String user, String password) throws EntityException;

	abstract public String getFileName();

	abstract public String getFileName(long version);

	public String getTemporaryFileName() {
		return getFileName() + ".temp";
	}

	abstract public String getNewVesrionFileName();

	public InputStream load(boolean deflated) throws IOException {
		return load(0, deflated);
	}

	public InputStream load(long version, boolean deflated) throws IOException {
		InputStream in;
		String file = getFileName(version);
		if (deflated) {
			in = fileUtil.loadFile(file);
		} else {
			in = fileUtil.inflateFile(file);
		}
		return in;
	}

	public List<Long> getVersions() {
		List<Long> versions = new LinkedList<Long>();
		String path = getPath();
		final String prefix = getPrefix();
		File dir = new File(path);
		if (dir.isDirectory()) {
			String[] files = dir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith(prefix);
				}
			});
			for (String file : files) {
				versions.add(Long.parseLong(file.substring(file.lastIndexOf(".") + 1, file.length()), 16));
			}
		}
		Collections.sort(versions, Collections.reverseOrder());
		return versions;
	}

	abstract protected String getPrefix();

	abstract protected String getPath();

	public synchronized String resetPassword(String user) throws EntityException {
		UUID uuid = UUID.randomUUID();
		OutputStream out = null;
		try {
			InputStream in = fileUtil.inflateFile(getFileName());
			Source xmlSource = new StreamSource(in);
			out = fileUtil.deflateFile(getTemporaryFileName());
			Result outputTarget = new StreamResult(out);
			Transformer transformer = getResetPasswordTransformer(user, uuid.toString());
			transformer.transform(xmlSource, outputTarget);
			out.close();

			storeEntity();

			if (isPasswordReset(user, uuid.toString())) {
				return uuid.toString();
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new EntityException(e);
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

	private boolean isPasswordReset(String user, String uuid) throws IOException, XPathExpressionException {
		XPath xPath = xmlUtils.getXpath(new ParamValue<Object>("user", user));
		InputSource in = new InputSource(load(false));
		String resetToken = (String) xPath.evaluate("//resetToken[../user = $user]", in, XPathConstants.STRING);
		return uuid.equals(resetToken);
	}

	public synchronized boolean changePassword(String user, String uuid, String newPassword) throws EntityException {
		OutputStream out = null;
		try {
			InputStream in = fileUtil.inflateFile(getFileName());
			Source xmlSource = new StreamSource(in);
			out = fileUtil.deflateFile(getTemporaryFileName());
			Result outputTarget = new StreamResult(out);
			String encPassword = Obfuscator.sha1(newPassword);
			Transformer transformer = getChangePasswordTransformer(user, uuid.toString(), encPassword );
			transformer.transform(xmlSource, outputTarget);
			out.close();

			storeEntity();

			return grantUser(user, newPassword) != null;
		} catch (Exception e) {
			throw new EntityException(e);
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

	public synchronized void restore(long version) throws LdException {
		FileOutputStream temp;
		try {
			temp = new FileOutputStream(getTemporaryFileName());
			fileUtil.streamOut(load(version, true), temp);
			backupEntity();
		} catch (IOException e) {
			throw new LdException(e);
		}
	}

	public synchronized void save(Source xmlSource) throws IOException, TransformerException, LdException {
		OutputStream out = null;
		try {
			out = fileUtil.deflateFile(getTemporaryFileName());
			Result outputTarget = new StreamResult(out);
			Transformer transformer = getMergeTransformer();
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

	protected void backupEntity() throws LdException {
		File currentVersion = new File(getFileName());
		File backUp = new File(getNewVesrionFileName());

		if(!backUp.getParentFile().isDirectory()) {
			backUp.getParentFile().mkdirs();			
		}
		// Backup CURRENT version
		if (!currentVersion.renameTo(backUp)) {
			throw new LdException("Error generating backup version, from: " + currentVersion.toString() + " to: " + backUp.toString());
		}

		storeEntity();
	}

	protected void storeEntity() throws LdException {
		File current = new File(getFileName());
		File modified = new File(getTemporaryFileName());

		if (current.exists()) {
			current.delete();
		}

		// Rename new version
		if (!modified.renameTo(current)) {
			throw new LdException("Error renaming new version, from: " + modified.toString() + " to: " + current.toString());
		}
	}

	public Permissions getPermissions() {
		return permissions;
	}

	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
	}

	abstract protected Transformer getChangePasswordTransformer(String user, String resetToken, String newPassword) throws TransformerException;

	abstract protected Transformer getMergeTransformer() throws TransformerException;

	abstract protected Transformer getResetPasswordTransformer(String user, String uuid) throws TransformerException;

	abstract public String getName();

	protected Permissions parsePermissions(NodeList permList) {
		Permissions permissions = new Permissions();
		for (int i = 0; i < permList.getLength(); i++) {
			Element perm = (Element) permList.item(i);
			String type = perm.getAttribute("type");
			boolean granted = Boolean.parseBoolean(perm.getAttribute("granted"));
			String expiration = perm.getAttribute("expires");
			addPermission(permissions, type, granted, expiration);
		}
		return permissions;
	}

	protected void addPermission(Permissions permissions, String type, boolean granted, String expiration) {
		boolean grant;
		if (granted) {
			Date expires = null;
			if (!"never".equals(expiration)) {
				try {
					expires = getDateFormat().parse(expiration);
					grant = expires.after(new Date());
				} catch (ParseException e) {
					grant = false;
				}
			} else {
				grant = true;
			}
		} else {
			grant = false;
		}
		if (grant) {
			if ("readEnabled".equals(type)) {
				permissions.compose(Permission.READ_ENABLED);
			}
			if ("writeEnabled".equals(type)) {
				permissions.compose(Permission.WRITE_ENABLED);
			}

		}
	}

	protected <K,V> void addToCache(Map<K,V> cache, K key, V value, int max) {
		cache.put(key, value);
		if(cache.size()>max) {
			Set<K> keys = cache.keySet();
			for (K k : keys) {
				if(!k.equals(key)) {
					cache.remove(k);
					break;
				}
			}
		}
	}

	protected DateFormat getDateFormat() {
		return (DateFormat) dateFormat.clone();
	}
}
