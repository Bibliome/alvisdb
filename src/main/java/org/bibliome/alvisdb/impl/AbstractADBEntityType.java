package org.bibliome.alvisdb.impl;

import org.bibliome.alvisdb.ADBEntity;
import org.bibliome.alvisdb.ADBEntityType;
import org.bibliome.alvisdb.ADBException;
import org.bibliome.alvisdb.AlvisDB;
import org.bibliome.alvisdb.ConnectionManager;

public abstract class AbstractADBEntityType extends AbstractADBElement implements ADBEntityType {
	private final boolean hierarchy;
	private final String rootId;
	
	protected AbstractADBEntityType(AlvisDB alvisDB, String id, String name, String rootId) throws ADBException {
		super(alvisDB, id, name);
		this.hierarchy = rootId != null;
		this.rootId = rootId;
		alvisDB.addEntityType(this);
	}

	@Override
	public boolean hasHierarchy() {
		return hierarchy;
	}

	@Override
	public ADBEntity[] getEntities(ConnectionManager mngr, String[] ids) throws ADBException {
		ADBEntity[] result = new ADBEntity[ids.length];
		for (int i = 0; i < result.length; ++i) {
			result[i] = getEntity(mngr, ids[i]);
		}
		return result;
	}

	@Override
	public ADBEntity getRoot(ConnectionManager mngr) throws ADBException {
		return getEntity(mngr, rootId);
	}
}
