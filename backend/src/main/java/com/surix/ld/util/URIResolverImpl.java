package com.surix.ld.util;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class URIResolverImpl implements URIResolver {

	private FileUtil fileUtil;
	private Config cfg;

	public URIResolverImpl(FileUtil fileUtil, Config cfg) {
		this.fileUtil = fileUtil;
		this.cfg = cfg;
	}

	public Source resolve(String href, String base) throws TransformerException {
		try {
			return new StreamSource(fileUtil.inflateFile(cfg.getCurrentStorage() + href));
		} catch (IOException e) {
			throw new TransformerException(e);
		}
	}
}
