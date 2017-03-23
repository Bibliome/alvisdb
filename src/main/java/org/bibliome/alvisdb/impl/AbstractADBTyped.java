package org.bibliome.alvisdb.impl;

import org.bibliome.alvisdb.ADBEntityType;
import org.bibliome.alvisdb.ADBTyped;
import org.bibliome.alvisdb.AlvisDB;

public class AbstractADBTyped extends AbstractADBElement implements ADBTyped {
	private final ADBEntityType type;

	protected AbstractADBTyped(AlvisDB alvisDB, String id, String name, ADBEntityType type) {
		super(alvisDB, id, name);
		if (type == null) {
			throw new NullPointerException();
		}
		this.type = type;
	}

	@Override
	public ADBEntityType getType() {
		return type;
	}
}
