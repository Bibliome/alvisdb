package org.bibliome.alvisdb;

public interface ADBEntity extends ADBTyped {
	String[] getSynonyms();
	String[] getAncestorIds();
	ADBEntity[] getAncestors(ConnectionManager mngr) throws ADBException;
	String[] getPathIds();
	ADBEntity[] getPath(ConnectionManager mngr) throws ADBException;
	String[] getChildrenIds();
	ADBEntity[] getChildren(ConnectionManager mngr) throws ADBException;
}
