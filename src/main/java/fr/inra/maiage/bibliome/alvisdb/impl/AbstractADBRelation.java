package fr.inra.maiage.bibliome.alvisdb.impl;

import fr.inra.maiage.bibliome.alvisdb.ADBArgument;
import fr.inra.maiage.bibliome.alvisdb.ADBEntity;
import fr.inra.maiage.bibliome.alvisdb.ADBEntityOccurrence;
import fr.inra.maiage.bibliome.alvisdb.ADBEntityType;
import fr.inra.maiage.bibliome.alvisdb.ADBException;
import fr.inra.maiage.bibliome.alvisdb.ADBRelation;
import fr.inra.maiage.bibliome.alvisdb.ADBRelationType;
import fr.inra.maiage.bibliome.alvisdb.AlvisDB;
import fr.inra.maiage.bibliome.alvisdb.ConnectionManager;

public class AbstractADBRelation extends AbstractADBElement implements ADBRelation {
	private final ADBRelationType type;
	private final ADBEntityOccurrence left;
	private final ADBEntityOccurrence right;
	private final String[] leftAncestors;
	private final String[] rightAncestors;

	protected AbstractADBRelation(AlvisDB alvisDB, String id, String name, ConnectionManager mngr, ADBRelationType type, ADBEntityOccurrence left, ADBEntityOccurrence right, String[] leftAncestors, String[] rightAncestors) throws ADBException {
		super(alvisDB, id, name);
		if (type == null) {
			throw new NullPointerException();
		}
		if (left == null) {
			throw new NullPointerException();
		}
		if (right == null) {
			throw new NullPointerException();
		}
		checkArgumentType(mngr, left, type.getLeft());
		checkArgumentType(mngr, right, type.getRight());
		this.type = type;
		this.left = left;
		this.right = right;
		this.leftAncestors = leftAncestors;
		this.rightAncestors = rightAncestors;
	}

	private static void checkArgumentType(ConnectionManager mngr, ADBEntityOccurrence arg, ADBArgument spec) throws ADBException {
		ADBEntityType actualType = arg.getEntity(mngr).getType();
		ADBEntityType expectedType = spec.getType();
		if (!actualType.equals(expectedType)) {
			throw new ADBException(spec.getId() + " argument has type " + actualType.getId() + ", expected " + expectedType.getId());
		}
	}

	@Override
	public ADBRelationType getType() {
		return type;
	}

	@Override
	public ADBEntityOccurrence getLeft() {
		return left;
	}

	@Override
	public ADBEntityOccurrence getRight() {
		return right;
	}

	@Override
	public ADBEntityOccurrence get(String role) throws ADBException {
		if (type.getLeft().getId().equals(role)) {
			return left;
		}
		if (type.getRight().getId().equals(role)) {
			return right;
		}
		throw new ADBException("no argument with role: " + role);
	}

	@Override
	public String[] getLeftAncestorIds() {
		return leftAncestors;
	}

	@Override
	public ADBEntity[] getLeftAncestors(ConnectionManager mngr) throws ADBException {
		ADBEntity[] result = new ADBEntity[leftAncestors.length];
		ADBEntityType et = type.getLeft().getType();
		for (int i = 0; i < result.length; ++i) {
			result[i] = et.getEntity(mngr, leftAncestors[i]);
		}
		return result;
	}

	@Override
	public String[] getRightAncestorIds() {
		return rightAncestors;
	}

	@Override
	public ADBEntity[] getRightAncestors(ConnectionManager mngr) throws ADBException {
		ADBEntity[] result = new ADBEntity[rightAncestors.length];
		ADBEntityType et = type.getRight().getType();
		for (int i = 0; i < result.length; ++i) {
			result[i] = et.getEntity(mngr, rightAncestors[i]);
		}
		return result;
	}
}
