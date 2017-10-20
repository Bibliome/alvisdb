package fr.inra.maiage.bibliome.alvisdb;


public interface ADBEntityType extends ADBElement {
	boolean hasHierarchy();
	ADBEntity getRoot(ConnectionManager mngr) throws ADBException;
	ADBEntity getEntity(ConnectionManager mngr, String id) throws ADBException;
	ADBEntity[] getEntities(ConnectionManager mngr, String[] ids) throws ADBException;
	ADBEntity[] listEntities(ConnectionManager mngr, int start, int n) throws ADBException;
	ADBEntity[] completeEntities(ConnectionManager mngr, String s, CompletionOperator op, int max) throws ADBException;
}
