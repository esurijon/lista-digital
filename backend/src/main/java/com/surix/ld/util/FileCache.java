package com.surix.ld.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileCache<V> {

	private Map<File, Pair<Long, V>> map = new LinkedHashMap<File, Pair<Long, V>>();

	public V get(File key) {
		boolean hasExpired = true;
		Pair<Long, V> entry = map.get(key);
		if (entry != null) {
			Long lastMod = entry.getFirst();
			hasExpired = key.lastModified() > lastMod.longValue();
		}
		return !hasExpired ? entry.getSecond() : null;
	}

	public void put(File key, V value) {
		Long lastMod = key.lastModified();
		Pair<Long, V> entry = new Pair<Long, V>(lastMod, value);
		map.put(key, entry);
	}

}
