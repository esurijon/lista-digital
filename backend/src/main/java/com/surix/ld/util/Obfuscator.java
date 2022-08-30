package com.surix.ld.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Obfuscator {

	public static String base64Encode(String msg) {
		return new String(org.apache.commons.codec.binary.Base64.encodeBase64(msg.getBytes()));
	}

	public static String base64Decode(String msg) {
		return new String(org.apache.commons.codec.binary.Base64.decodeBase64(msg.getBytes()));
	}

	public static String sha1(String message) throws NoSuchAlgorithmException {
		return digest("SHA-1", message);
	}

	public static String md5(String message) throws NoSuchAlgorithmException {
		return digest("MD5", message);
	}

	private static String digest(String algorithm, String message) throws NoSuchAlgorithmException {
		StringBuilder hash = new StringBuilder();
		byte[] buffer = message.getBytes();
		MessageDigest md = MessageDigest.getInstance(algorithm);
		md.update(buffer);
		byte[] digest = md.digest();
		for (byte aux : digest) {
			int b = aux & 0xff;
			String hexa = Integer.toHexString(b);
			if (hexa.length() == 1) {
				hash.append("0");
			}
			hash.append(hexa);
		}
		return hash.toString();
	}
}

