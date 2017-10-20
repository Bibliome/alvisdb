package fr.inra.maiage.bibliome.alvisdb.impl;

import fr.inra.maiage.bibliome.alvisdb.ADBElement;
import fr.inra.maiage.bibliome.alvisdb.AlvisDB;

public abstract class AbstractADBElement implements ADBElement {
	private final AlvisDB alvisDB;
	private final String id;
	private final String name;
	
	protected AbstractADBElement(AlvisDB alvisDB, String id, String name) {
		super();
		if (alvisDB == null) {
			throw new NullPointerException();
		}
		if (id == null) {
			throw new NullPointerException();
		}
		if (name == null) {
			throw new NullPointerException();
		}
		this.alvisDB = alvisDB;
		this.id = id;
		this.name = name;
	}

	@Override
	public AlvisDB getAlvisDB() {
		return alvisDB;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
}
