package fr.inra.maiage.bibliome.alvisdb;

public interface ADBEntityOccurrence {
	ADBEntity getEntity(ConnectionManager mngr) throws ADBException;
	String getDocumentId();
	String getSection();
	int getStart();
	int getEnd();
	String getForm(ConnectionManager mngr, ADBDocumentFactory docFactory) throws ADBException ;
}
