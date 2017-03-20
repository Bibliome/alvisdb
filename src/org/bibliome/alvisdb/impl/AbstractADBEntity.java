package org.bibliome.alvisdb.impl;

import org.bibliome.alvisdb.ADBEntity;
import org.bibliome.alvisdb.ADBEntityType;
import org.bibliome.alvisdb.ADBException;
import org.bibliome.alvisdb.AlvisDB;
import org.bibliome.alvisdb.ConnectionManager;

public abstract class AbstractADBEntity extends AbstractADBTyped implements ADBEntity {
	protected AbstractADBEntity(AlvisDB alvisDB, String id, String name, ADBEntityType type) {
		super(alvisDB, id, name, type);
		if (type == null) {
			throw new NullPointerException();
		}
	}

	@Override
	public ADBEntity[] getAncestors(ConnectionManager mngr) throws ADBException {
		String[] ancestorIds = getAncestorIds();
		return getType().getEntities(mngr, ancestorIds);
	}

	@Override
	public ADBEntity[] getChildren(ConnectionManager mngr) throws ADBException {
		String[] childrenIds = getChildrenIds();
		return getType().getEntities(mngr, childrenIds);
	}

	@Override
	public ADBEntity[] getPath(ConnectionManager mngr) throws ADBException {
		String[] pathIds = getPathIds();
		return getType().getEntities(mngr, pathIds);
	}
}
