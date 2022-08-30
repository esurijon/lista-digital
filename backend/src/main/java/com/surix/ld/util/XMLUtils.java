package com.surix.ld.util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.surix.ld.exceptions.EnvironmentException;

public class XMLUtils {

	private XPathFactory xPathFactory = XPathFactory.newInstance();
	private TransformerFactory tf = TransformerFactory.newInstance();

	private FileCache<Templates> templates = new FileCache<Templates>();
	private Config conf;
	private URIResolver uriResolver;

	public XMLUtils(Config conf, URIResolver uriResolver) {
		this.conf = conf;
		this.uriResolver = uriResolver;
	}

	public Transformer getTransformer(String xsl, ParamValue<Object>... params) {
		try {
			Transformer transformer;
			if (xsl != null && xsl.length() > 0) {
				File templateFile = new File(conf.getXslPath() + xsl);
				Templates template = templates.get(templateFile);
				if (template == null) {
					URL sourceURL = templateFile.toURI().toURL();
					Source source = new StreamSource(sourceURL.openStream());
					source.setSystemId(sourceURL.toExternalForm());
					template = tf.newTemplates(source);
					templates.put(templateFile, template);
				}
				transformer = template.newTransformer();
				for (ParamValue<Object> param : params) {
					transformer.setParameter(param.getKey(), param.getValue());
				}
				transformer.setURIResolver(uriResolver);
			} else {
				transformer = tf.newTransformer();
			}
			return transformer;
		} catch (TransformerException e) {
			throw new EnvironmentException(e);
		} catch (IOException e) {
			throw new EnvironmentException(e);
		}
	}

	public <T> T unmarshal(Class<T> clazz, Element node) {
		return null;
	}

	/*
	 * public Node marshal(Object o) throws ParserConfigurationException {
	 * DocumentDOMView ddv = new DocumentDOMView(o); return
	 * ddv.getDocumentElement(); }
	 * 
	 * private <T> JAXBElement<T> wrap(String ns, String tag, T o) { QName qtag
	 * = new QName(ns, tag); Class<?> clazz = o.getClass();
	 * 
	 * @SuppressWarnings("unchecked") JAXBElement<T> jbe = new JAXBElement(qtag,
	 * clazz, o); return jbe; }
	 */

	public <T> Node marshal(T obj) throws JAXBException, ParserConfigurationException {
		JAXBContext jbc = JAXBContext.newInstance(obj.getClass());
		Marshaller marshaller = jbc.createMarshaller();
		Node xml = getDocument();
		marshaller.marshal(obj, xml);
		return xml;
	}

	private Node getDocument() throws ParserConfigurationException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		return doc;
	}

	public void streamOut(Node xml, Writer out, String xsl) throws TransformerException, ParserConfigurationException,
			IOException {
		Transformer transformer = getTransformer(xsl);
		Source source = new DOMSource(xml);
		Result result = new StreamResult(out);
		transformer.transform(source, result);
	}

	public <T> void streamOut(T data, HttpServletResponse response) {
		response.setHeader("Content-type", "text/xml; charset=UTF-8");
		try {
			Node xml = marshal(data);
			streamOut(xml, response.getWriter(), null);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public XPath getXpath(ParamValue<Object>... params) {
		XPath xPath = xPathFactory.newXPath();
		if (params.length > 0) {
			Map<String, Object> varMap = new HashMap<String, Object>();
			for (ParamValue<Object> param : params) {
				varMap.put(param.getKey(), param.getValue());
			}
			XPathVariableResolver resolver = new MapVariableResolver(varMap);
			xPath.setXPathVariableResolver(resolver);
		}
		return xPath;
	}

}
