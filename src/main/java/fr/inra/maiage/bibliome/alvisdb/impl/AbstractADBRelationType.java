package fr.inra.maiage.bibliome.alvisdb.impl;

import fr.inra.maiage.bibliome.alvisdb.ADBArgument;
import fr.inra.maiage.bibliome.alvisdb.ADBEntity;
import fr.inra.maiage.bibliome.alvisdb.ADBException;
import fr.inra.maiage.bibliome.alvisdb.ADBRelation;
import fr.inra.maiage.bibliome.alvisdb.ADBRelationType;
import fr.inra.maiage.bibliome.alvisdb.AlvisDB;
import fr.inra.maiage.bibliome.alvisdb.ConnectionManager;

public abstract class AbstractADBRelationType extends AbstractADBElement implements ADBRelationType {
	private final ADBArgument left;
	private final ADBArgument right;

	protected AbstractADBRelationType(AlvisDB alvisDB, String id, String name, ADBArgument left, ADBArgument right) throws ADBException {
		super(alvisDB, id, name);
		if (left == null) {
			throw new NullPointerException();
		}
		if (right == null) {
			throw new NullPointerException();
		}
		this.left = left;
		this.right = right;
		alvisDB.addRelationType(this);
	}

	@Override
	public ADBArgument getLeft() {
		return left;
	}

	@Override
	public ADBArgument getRight() {
		return right;
	}

	@Override
	public ADBArgument getArgument(String role) throws ADBException {
		if (left.getId().equals(role)) {
			return left;
		}
		if (right.getId().equals(role)) {
			return right;
		}
		throw new ADBException("no argument with role: " + role);
	}

	@Override
	public ADBArgument getOther(String role) throws ADBException {
		if (left.getId().equals(role)) {
			return right;
		}
		if (right.getId().equals(role)) {
			return left;
		}
		throw new ADBException("no argument with role: " + role);
	}

	@Override
	public int countRelations(ConnectionManager mngr) throws ADBException {
		return countRelations(mngr, null, false, null, false);
	}
	
	@Override
	public ADBRelation[] listRelations(ConnectionManager mngr, int skip, int max, String sort, boolean descSort) throws ADBException {
		return listRelations(mngr, null, true, null, true, skip, max, sort, descSort);
	}

	@Override
	public int countRelations(ConnectionManager mngr, String role, ADBEntity entity, boolean useHierarchy) throws ADBException {
		return countRelations(mngr, role, entity, useHierarchy, null, true);
	}

	@Override
	public ADBRelation[] listRelations(ConnectionManager mngr, String role, ADBEntity entity, boolean useHierarchy, int skip, int max, String sort, boolean descSort) throws ADBException {
		return listRelations(mngr, role, entity, useHierarchy, null, true, skip, max, sort, descSort);
	}

	@Override
	public int countRelations(ConnectionManager mngr, String role, ADBEntity entity, boolean useHierarchy, ADBEntity other, boolean otherUseHierarchy) throws ADBException {
		if (left.getId().equals(role)) {
			return countRelations(mngr, entity, useHierarchy, other, otherUseHierarchy);
		}
		if (right.getId().equals(role)) {
			return countRelations(mngr, other, otherUseHierarchy, entity, useHierarchy);
		}
		return -1;
	}

	@Override
	public ADBRelation[] listRelations(ConnectionManager mngr, String role, ADBEntity entity, boolean useHierarchy, ADBEntity other, boolean otherUseHierarchy, int skip, int max, String sort, boolean descSort) throws ADBException {
		if (left.getId().equals(role)) {
			return listRelations(mngr, entity, useHierarchy, other, otherUseHierarchy, skip, max, sort, descSort);
		}
		if (right.getId().equals(role)) {
			return listRelations(mngr, other, otherUseHierarchy, entity, useHierarchy, skip, max, sort, descSort);
		}
		return new ADBRelation[0];
	}
}
