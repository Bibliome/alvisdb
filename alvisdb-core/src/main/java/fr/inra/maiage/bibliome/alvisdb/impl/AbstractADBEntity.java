package fr.inra.maiage.bibliome.alvisdb.impl;

import fr.inra.maiage.bibliome.alvisdb.ADBEntity;
import fr.inra.maiage.bibliome.alvisdb.ADBEntityType;
import fr.inra.maiage.bibliome.alvisdb.ADBException;
import fr.inra.maiage.bibliome.alvisdb.AlvisDB;
import fr.inra.maiage.bibliome.alvisdb.ConnectionManager;

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
