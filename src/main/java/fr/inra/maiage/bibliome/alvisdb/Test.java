package fr.inra.maiage.bibliome.alvisdb;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Test {
	private static void title(String s) {
		System.out.println(s);
	}
	
	private static void value(String s) {
		System.out.print("   ");
		System.out.println(s);
	}
	
	private static void values(String... sa) {
		for (String s : sa) {
			value(s);
		}
	}
	
	private static void entity(ADBEntity e) {
		value(e.getId() + ": " + e.getName());
	}
	
	private static void entities(ADBEntity... ea) {
		for (ADBEntity e : ea) {
			entity(e);
		}
	}

	public static void main(String[] args) throws ADBException, ParserConfigurationException {
		AlvisDBBuilder builder = new AlvisDBBuilder();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbf.newDocumentBuilder();
		builder.buildFromDOM(docBuilder, args[0]);
		AlvisDB adb = builder.getAlvisDB();
		ConnectionManager mngr = new ConnectionManager();
		ADBEntityType habitats = adb.getEntityType("habitat");
		ADBEntity e = habitats.getEntity(mngr, "MBTO:00001144");
		
		title("Name");
		value(e.getName());
		
		title("Synonyms");
		values(e.getSynonyms());

		title("Children");
		entities(e.getChildren(mngr));

		title("Ancestors");
		entities(e.getAncestors(mngr));

		title("Path");
		entities(e.getPath(mngr));

		title("Complete medi");
		entities(habitats.completeEntities(mngr, "medi", CompletionOperator.STARTS_WITH, 10));
		
		title("Complete ter");
		entities(habitats.completeEntities(mngr, "ter", CompletionOperator.CONTAINS, 10));
	}
}
