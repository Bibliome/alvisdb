package fr.inra.maiage.bibliome.alvisdb;


public interface ADBRelationType extends ADBElement {
	ADBArgument getLeft();
	ADBArgument getRight();
	ADBArgument getArgument(String role) throws ADBException;
	ADBArgument getOther(String role) throws ADBException;
	
	int countRelations(ConnectionManager mngr) throws ADBException;
	ADBRelation[] listRelations(ConnectionManager mngr, int skip, int max, String sort, boolean descSort) throws ADBException;

	int countRelations(ConnectionManager mngr, String role, ADBEntity entity, boolean useHierarchy) throws ADBException;
	ADBRelation[] listRelations(ConnectionManager mngr, String role, ADBEntity entity, boolean useHierarchy, int skip, int max, String sort, boolean descSort) throws ADBException;

	int countRelations(ConnectionManager mngr, String role, ADBEntity entity, boolean useHierarchy, ADBEntity other, boolean otherUseHierarchy) throws ADBException;
	ADBRelation[] listRelations(ConnectionManager mngr, String role, ADBEntity entity, boolean useHierarchy, ADBEntity other, boolean otherUseHierarchy, int skip, int max, String sort, boolean descSort) throws ADBException;

	int countRelations(ConnectionManager mngr, ADBEntity left, boolean leftUseHierarchy, ADBEntity right, boolean rightUseHierarchy) throws ADBException;
	ADBRelation[] listRelations(ConnectionManager mngr, ADBEntity left, boolean leftUseHierarchy, ADBEntity right, boolean rightUseHierarchy, int skip, int max, String sort, boolean descSort) throws ADBException;
}
