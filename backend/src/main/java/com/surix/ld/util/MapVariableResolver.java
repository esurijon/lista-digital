package com.surix.ld.util;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

public class MapVariableResolver implements XPathVariableResolver {

	private Map<String, Object> varMap;

	public MapVariableResolver(Map<String, Object> varMap) {
		this.varMap = varMap;
	}

	public Object resolveVariable(QName variableName) {
		return varMap.get(variableName.toString());
	}

}
