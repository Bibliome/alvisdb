package org.bibliome.alvisdb;

public interface ADBDocumentFactory {
	ADBDocument getDocument(ConnectionManager mngr, String id) throws ADBException;
}
