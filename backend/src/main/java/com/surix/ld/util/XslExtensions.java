package com.surix.ld.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.surix.ld.controller.OnLineListsServlet.Params;
import com.surix.ld.exceptions.LdException;

public class XslExtensions {

	private static FileUtil fileUtil;
	private static MailSender mailSender;
	private static Config config;

	private static Logger logger = Logger.getLogger(XslExtensions.class);
	private static Pattern rollDatePatern = Pattern.compile("(-?\\d+)([yMwdhms])");
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd Z");

	public static void init(Config conf, FileUtil fUtil, MailSender mSender) {
		config = conf;
		fileUtil = fUtil;
		mailSender = mSender;
		dateFormat = new SimpleDateFormat("yyyy/MM/dd Z");	
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-3:00"));
	}

	/**
	 * @return null for success or an error message
	 */
	public static String createStorage(String fileName, String fileContent) {
		try {
			boolean result = true;
			String resultMsg;
			String storagePath = config.getCurrentStorage() + fileName;
			String backupPath = config.getBackupStorage() + fileName;

			File backupDir = new File(backupPath).getParentFile();
			if (!backupDir.exists()) {
				result &= backupDir.mkdirs();
				result &= backupDir.setExecutable(true, false);
				result &= backupDir.setWritable(true, false);
				result &= backupDir.setReadable(true, false);
			}

			File storageDir = new File(storagePath).getParentFile();
			if (!storageDir.exists()) {
				result &= storageDir.mkdirs();
				result &= storageDir.setExecutable(true, false);
				result &= storageDir.setWritable(true, false);
				result &= storageDir.setReadable(true, false);
			}

			if (result) {
				File file = new File(storagePath);
				result &= file.createNewFile();
				result &= file.setReadable(true, false);
				result &= file.setWritable(true, false);
				if (result) {
					OutputStream out = fileUtil.deflateFile(file.getAbsolutePath());
					ByteArrayInputStream bais = new ByteArrayInputStream(fileContent.trim().getBytes());
					fileUtil.streamOut(bais, out);
					resultMsg = "";
				} else {
					resultMsg = "Can't override file " + file.getAbsolutePath();
				}
			} else {
				resultMsg = "Can't create directories " + storagePath + ", " + backupPath;
			}
			return resultMsg;
		} catch (Exception e) {
			logger.error("Error creating storage for: " + fileName, e);
			return "Error creating storage for " + fileName + ": " + e.getMessage();
		}
	}

	/**
	 * 
	 * @param pathName
	 * @return null for success or an error message
	 */
	public static String deleteStorage(String fileName) {
		try {
			File file = new File(config.getCurrentStorage() + fileName);
			File backup = new File(config.getBackupStorage() + fileName + "." + IdGenerator.createId());

			String result = file.renameTo(backup) ? "" : "Can't delete resource: " + fileName;
			return result;
		} catch (Exception e) {
			logger.error("Error deleting storage for: " + fileName, e);
			return "Error deleting storage for " + fileName + ": " + e.getMessage();
		}
	}

	public static String sendActivatePlannerAccountMail(String to, String planner, String activationToken) {
		try {
			String activationParams = Obfuscator.base64Encode("PLANNER=" + planner + "&" + Params.USER + "=" + to + "&" + Params.RESET_TOKEN + "=" + activationToken + "&ROLE=PLANNER");
			String queryString = Obfuscator.base64Encode((Params.ACTIVATE_ACCOUNT + "=" + activationParams));
			String activationURL = config.getExternalLandingPageUrl() + "?" + queryString;
			mailSender.sendActivatePlannerAccountMail(to, planner, activationURL);
			return "";
		} catch (LdException e) {
			logger.error("Error sending activattion account mail to " + to, e);
			return "Error sending activattion account mail to " + to + ": " + e.getMessage();
		}
	}

	public static String sendActivateHostAccountMail(String to, String plannerId, String plannerName, String activationToken) {
		try {
			String activationParams = Obfuscator.base64Encode("PLANNER=" + plannerName + "&" + Params.PLANNER_ID + "=" + plannerId + "&" + Params.USER + "=" + to + "&" + Params.RESET_TOKEN + "=" + activationToken
					+ "&ROLE=HOST");
			String queryString = Obfuscator.base64Encode(Params.ACTIVATE_ACCOUNT + "=" + activationParams);
			String activationURL = config.getExternalLandingPageUrl() + "?" + queryString;
			mailSender.sendActivateHostAccountMail(to, plannerName, activationURL);
			return "";
		} catch (LdException e) {
			logger.error("Error sending activattion account mail to " + to, e);
			return "Error sending activattion account mail to " + to + ": " + e.getMessage();
		}
	}

	public static String createToken() {
		return UUID.randomUUID().toString();
	}

	public static String createId() {
		return IdGenerator.createId();
	}

	public static String encodeBase64(String msg) {
		return Obfuscator.base64Encode(msg);
	}

	public static String decodeBase64(String msg) {
		return Obfuscator.base64Decode(msg);
	}

	public static String sha1(String msg) {
		try {
			return Obfuscator.sha1(msg);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Can't encode message: " + msg, e);
			return e.getMessage();
		}
	}

	public static String md5(String msg) {
		try {
			msg = URLDecoder.decode(msg, "UTF-8");
			return Obfuscator.md5(msg);
		} catch (UnsupportedEncodingException e) {
			logger.error("Can't unescape message: " + msg, e);
			return e.getMessage();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Can't encode message: " + msg, e);
			return e.getMessage();
		}
	}

	public static String getProperty(String key) {
		return config.get(key);
	}

	public static String rollDate(String dateString, String roll) {
		try {
			Date date = getDateFormat().parse(dateString);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-3:00"));
			cal.setTime(date);
			Matcher matcher = rollDatePatern.matcher(roll);
			if(matcher.matches()) {
				int amount = Integer.parseInt(matcher.group(1));
				int field = getCalField(matcher.group(2).charAt(0));
				cal.add(field, amount);
				return getDateFormat().format(cal.getTime());
			}
			return "bad roll spec";
		} catch (ParseException e) {
			logger.error(e);
			return e.getMessage();
		}
	}

	public static String maxDate(String dateString1, String dateString2) {
		try {
			Date date1 = getDateFormat().parse(dateString1);
			Date date2 = getDateFormat().parse(dateString2);
			return date1.after(date2) ? dateString1 : dateString2;
		} catch (ParseException e) {
			logger.error(e);
			return e.getMessage();
		}
	}

	public static String currentDate() {
		return getDateFormat().format(new Date());
	}

	private static int getCalField(char fieldChar) {
		int field = -1;
		switch (fieldChar) {
			case 'y': field = Calendar.YEAR; break;
			case 'M': field = Calendar.MONTH; break;
			case 'w': field = Calendar.WEEK_OF_MONTH; break;			
			case 'd': field = Calendar.DAY_OF_MONTH; break;
			case 'h': field = Calendar.HOUR_OF_DAY; break;
			case 'm': field = Calendar.MINUTE; break;
			case 's': field = Calendar.SECOND; break;
		}
		return field;
	}

	private static DateFormat getDateFormat() {
		return (DateFormat) dateFormat.clone();
	}
}
