package fr.inra.maiage.bibliome.alvisdb.impl;

import fr.inra.maiage.bibliome.alvisdb.ADBArgument;
import fr.inra.maiage.bibliome.alvisdb.ADBEntityType;
import fr.inra.maiage.bibliome.alvisdb.AlvisDB;

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
