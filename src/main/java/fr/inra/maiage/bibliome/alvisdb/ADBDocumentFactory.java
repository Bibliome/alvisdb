package fr.inra.maiage.bibliome.alvisdb;

public interface ADBDocumentFactory {
	ADBDocument getDocument(ConnectionManager mngr, String id) throws ADBException;
}
