package com.surix.ld.util;


public class ParamValue<V> extends Pair<String,V> {

	public ParamValue(String first, V second) {
		super(first, second);
	}
	
	String getKey() {
		return getFirst();
	}

	V getValue() {
		return getSecond();
	}
}
