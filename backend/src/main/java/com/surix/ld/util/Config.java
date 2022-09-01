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
	
	public String getCurrentStorage(){
		return props.getProperty("storage.root") + "CURRENT/";
	} 

	public String getBackupStorage(){
		return props.getProperty("storage.root") + "BACKUP/";
	}

	public String getXslPath() {
		return props.getProperty("xsl.path");
	} 

	public String get(String key) {
		return props.getProperty(key);
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
