package com.surix.ld.util;


public class IdGenerator {
	
	private static long prevId = System.currentTimeMillis();
	
	static public synchronized String createId() {
		long id = System.currentTimeMillis();
		if(id > prevId) {
			prevId = id;
		} else {
			prevId++;
		}
		return Long.toHexString(prevId);
	}

}
