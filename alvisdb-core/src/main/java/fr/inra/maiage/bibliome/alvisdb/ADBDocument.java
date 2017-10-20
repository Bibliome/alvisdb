package fr.inra.maiage.bibliome.alvisdb;

public interface ADBDocument {
	String getId();
	String getSection(String name) throws ADBException;
}
