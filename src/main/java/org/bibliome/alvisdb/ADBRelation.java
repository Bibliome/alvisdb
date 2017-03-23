package org.bibliome.alvisdb;

public interface ADBRelation extends ADBElement {
	ADBRelationType getType();
	ADBEntityOccurrence getLeft();
	ADBEntityOccurrence getRight();
	ADBEntityOccurrence get(String role) throws ADBException;
	String[] getLeftAncestorIds();
	ADBEntity[] getLeftAncestors(ConnectionManager mngr) throws ADBException;
	String[] getRightAncestorIds();
	ADBEntity[] getRightAncestors(ConnectionManager mngr) throws ADBException;
}
