package org.bibliome.alvisdb.impl.lucene;

import org.bibliome.alvisdb.ADBArgument;
import org.bibliome.alvisdb.ADBEntityType;
import org.bibliome.alvisdb.ADBException;
import org.bibliome.alvisdb.ADBRelationType;
import org.bibliome.alvisdb.AlvisDB;
import org.bibliome.alvisdb.ADBTypeFactory;
import org.bibliome.alvisdb.impl.ADBArgumentImpl;

public class LuceneAlvisDBFactory implements ADBTypeFactory {
	private final String indexPath;

	public LuceneAlvisDBFactory(String indexPath) {
		super();
		this.indexPath = indexPath;
	}

	@Override
	public ADBEntityType createEntityType(AlvisDB adb, String id, String name, String rootId) throws ADBException {
		return new LuceneEntityType(adb, id, name, rootId, indexPath);
	}

	@Override
	public ADBRelationType createRelationType(AlvisDB adb, String id, String name, String leftRole, String leftName, String leftType, String rightRole, String rightName, String rightType) throws ADBException {
		ADBArgument left = new ADBArgumentImpl(adb, leftRole, leftName, adb.getEntityType(leftType));
		ADBArgument right = new ADBArgumentImpl(adb, rightRole, rightName, adb.getEntityType(rightType));
		return new LuceneRelationType(adb, id, name, left, right, indexPath);
	}
}
