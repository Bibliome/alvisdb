package org.bibliome.alvisdb;

public interface ADBTypeFactory {
	ADBEntityType createEntityType(AlvisDB adb, String id, String name, String rootId) throws ADBException;
	ADBRelationType createRelationType(AlvisDB adb, String id, String name, String leftRole, String leftName, String leftType, String rightRole, String rightName, String rightType) throws ADBException;
}
