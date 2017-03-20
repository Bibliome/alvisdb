package org.bibliome.alvisdb.impl;

import org.bibliome.alvisdb.ADBArgument;
import org.bibliome.alvisdb.ADBEntityType;
import org.bibliome.alvisdb.AlvisDB;

public class ADBArgumentImpl extends AbstractADBElement implements ADBArgument {
	private final ADBEntityType type;
	
	public ADBArgumentImpl(AlvisDB alvisDB, String id, String name, ADBEntityType type) {
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
