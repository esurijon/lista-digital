package com.surix.ld.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
		InputStream configInputStream;
		File confFile = new File(System.getProperty("user.home") + "/listadigital.properties");
		if (confFile.exists()) {
			configInputStream = new FileInputStream(confFile);
		} else {
			configInputStream = this.getClass().getClassLoader().getResourceAsStream("listadigital.properties");
		}
		props.load(configInputStream);
		PropertyConfigurator.configure(props);
	}

}
