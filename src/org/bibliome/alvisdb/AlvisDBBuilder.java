package org.bibliome.alvisdb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bibliome.alvisdb.impl.lucene.LuceneAlvisDBFactory;
import org.bibliome.alvisdb.impl.lucene.LuceneDocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AlvisDBBuilder {
	private AlvisDB alvisDB = new AlvisDB();
	private final Map<String,ADBTypeFactory> factories = new HashMap<String,ADBTypeFactory>();
	private String defaultFactory = null;
	
	public void clear() {
		alvisDB = new AlvisDB();
		factories.clear();
		defaultFactory = null;
	}
	
	public AlvisDB getAlvisDB() {
		return alvisDB;
	}
	
	public void buildFromDOM(String uri) throws ADBException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		buildFromDOM(dbf, uri);
	}
	
	public void buildFromDOM(DocumentBuilderFactory dbf, String uri) throws ADBException {
		try {
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();
			buildFromDOM(docBuilder, uri);
		}
		catch (ParserConfigurationException e) {
			throw new ADBException(e);
		}
	}

	public void buildFromDOM(DocumentBuilder docBuilder, String uri) throws ADBException {
		try {
			Document doc = docBuilder.parse(uri);
			buildFromDOM(doc);
		}
		catch (SAXException|IOException e) {
			throw new ADBException(e);
		}
	}
	
	public void buildFromDOM(DocumentBuilder docBuilder, File file) throws ADBException {
		try {
			Document doc = docBuilder.parse(file);
			buildFromDOM(doc);
		}
		catch (SAXException|IOException e) {
			throw new ADBException(e);
		}
	}
	
	public void buildFromDOM(Document doc) throws ADBException {
		Element rootElt = doc.getDocumentElement();
		buildFromDOM(rootElt);
	}
	
	public void buildFromDOM(Element elt) throws ADBException {
		List<Element> factoryElts = new ArrayList<Element>();
		List<Element> entityElts = new ArrayList<Element>();
		List<Element> relationElts = new ArrayList<Element>();
		Element docFactoryElt = null;
		NodeList children = elt.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child instanceof Element) {
				Element childElt = (Element) child;
				String tag = childElt.getTagName();
				switch (tag) {
					case "factory":
						factoryElts.add(childElt);
						break;
					case "entity":
						entityElts.add(childElt);
						break;
					case "relation":
						relationElts.add(childElt);
						break;
					case "documents":
						if (docFactoryElt != null) {
							throw new ADBException("duplicate document factory");
						}
						docFactoryElt = childElt;
						break;
					default:
						throw new ADBException("unsupported tag: " + tag);
				}
			}
		}
		for (Element fe : factoryElts) {
			factoryFromDOM(fe);
		}
		for (Element ee : entityElts) {
			entityFromDOM(ee);
		}
		for (Element re : relationElts) {
			relationFromDOM(re);
		}
		if (docFactoryElt != null) {
			docFactoryFromDOM(docFactoryElt);
		}
	}

	private void docFactoryFromDOM(Element docFactoryElt) throws ADBException {
		String type = getAttribute(docFactoryElt, "type", defaultFactory);
		switch (type) {
			case "lucene": {
				String idField = getAttribute(docFactoryElt, "id-field", null);
				String indexPath = docFactoryElt.getTextContent();
				LuceneDocumentFactory docFactory = new LuceneDocumentFactory(indexPath, idField);
				alvisDB.setDocumentFactory(docFactory);
			}
		}
	}

	private static String getAttribute(Element elt, String attr, String defaultValue) throws ADBException {
		if (!elt.hasAttribute(attr)) {
			if (defaultValue == null) {
				throw new ADBException("missing '" + attr + "' attribute in '" + elt.getTagName() + "' tag");
			}
			return defaultValue;
		}
		return elt.getAttribute(attr);
	}

	private static ADBTypeFactory createFactory(String type, String data) throws ADBException {
		switch (type) {
			case "lucene":
				return new LuceneAlvisDBFactory(data);
		}
		throw new ADBException("unknown factory type: " + type);
	}
	
	public void addFactory(String name, ADBTypeFactory factory) throws ADBException {
		if (factories.containsKey(name)) {
			throw new ADBException("duplicate factory name: " + name);
		}
		factories.put(name, factory);
		if (defaultFactory == null) {
			defaultFactory = name;
		}
	}

	private void factoryFromDOM(Element fe) throws ADBException {
		String name = getAttribute(fe, "name", null);
		String type = getAttribute(fe, "type", null);
		String data = fe.getTextContent().trim();
		ADBTypeFactory factory = createFactory(type, data);
		addFactory(name, factory);
	}
	
	public ADBTypeFactory getFactory(String name) throws ADBException {
		if (!factories.containsKey(name)) {
			throw new ADBException("undefined factory: " + name);
		}
		return factories.get(name);
	}
	
	private void entityFromDOM(Element ee) throws ADBException {
		String factoryName = getAttribute(ee, "factory", defaultFactory);
		String id = getAttribute(ee, "id", null);
		String name = getAttribute(ee, "name", null);
		String rootId = getAttribute(ee, "root", "");
		if (rootId.isEmpty()) {
			rootId = null;
		}
		ADBTypeFactory factory = getFactory(factoryName);
		factory.createEntityType(alvisDB, id, name, rootId);
	}
	
	private void relationFromDOM(Element re) throws ADBException {
		String factoryName = getAttribute(re, "factory", defaultFactory);
		String id = getAttribute(re, "id", null);
		String name = getAttribute(re, "name", null);

		Element leftElt = null;
		Element rightElt = null;
		NodeList children = re.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child instanceof Element) {
				Element childElt = (Element) child;
				if (leftElt == null) {
					leftElt = childElt;
					continue;
				}
				if (rightElt == null) {
					rightElt = childElt;
					continue;
				}
				throw new ADBException("only binary relations are supported, extra tag: " + childElt.getTagName());
			}
		}
		String leftRole = leftElt.getTagName();
		String leftName = getAttribute(leftElt, "name", null);
		String leftType = getAttribute(leftElt, "type", null);
		String rightRole = rightElt.getTagName();
		String rightName = getAttribute(rightElt, "name", null);
		String rightType = getAttribute(rightElt, "type", null);
		
		ADBTypeFactory factory = getFactory(factoryName);
		factory.createRelationType(alvisDB, id, name, leftRole, leftName, leftType, rightRole, rightName, rightType);
	}
}
