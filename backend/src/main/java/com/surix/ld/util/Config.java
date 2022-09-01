package com.surix.ld.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.surix.ld.exceptions.EnvironmentException;
import org.apache.log4j.PropertyConfigurator;

@SuppressWarnings("serial")
public class Config {

	private Properties props = new Properties();
	
	public Config() throws IOException {
		reload();
	}

	private String getOrFail(String property) {
		if(props.containsKey(property)) {
			return props.getProperty(property);
		} else {
			throw new EnvironmentException("Missing property: " + property);
		}
	}
	public String getCurrentStorage(){
		return getOrFail("storage.root") + "CURRENT/";
	} 

	public String getBackupStorage(){
		return getOrFail("storage.root") + "BACKUP/";
	}

	public String getExternalLandingPageUrl() {
		return getOrFail("external.landing-page.url");
	}

	public String getAdminUserName() {
		return getOrFail("admin.user");
	}
	public String getAdminUserPassword() {
		return getOrFail("admin.password");
	}
	public String getMercadoPagoAccountId() {
		return getOrFail("mp.accountId");
	}
	public String getMercadoPagoSondaKey() {
		return getOrFail("mp.sonda.key");
	}
	public String getMercadoPagoSondaUrl() {
		return getOrFail("mp.sonda.url");
	}
	public String get(String key) {
		return getOrFail(key);
	}

	public void reload() throws IOException {
		File confFile = null;
		InputStream configInputStream = null;

		String propertiesFilePath = System.getenv("properties-file");
		if(propertiesFilePath != null) {
			confFile = new File(propertiesFilePath);
		}
		if(confFile == null || !confFile.exists()) {
			String defaultPropertiesFilePath = System.getProperty("user.home") + "/listadigital.properties";
			confFile = new File(defaultPropertiesFilePath);
		}
		if (confFile.exists()) {
			configInputStream = new FileInputStream(confFile);
		}
		if(configInputStream != null) {
			props.load(configInputStream);
			PropertyConfigurator.configure(props);
		} else {
			throw new EnvironmentException("No config file found");
		}
	}

}
