package fr.inra.maiage.bibliome.alvisdb.impl;

import fr.inra.maiage.bibliome.alvisdb.ADBEntityType;
import fr.inra.maiage.bibliome.alvisdb.AlvisDB;

public class ADBEntityBean extends AbstractADBEntity{
	private final String[] synonyms;
	private final String[] pathIds;
	private final String[] ancestorIds;
	private final String[] childrenIds;
	
	public ADBEntityBean(AlvisDB alvisDB, String id, String name, ADBEntityType type, String[] synonyms, String[] pathIds, String[] ancestorIds, String[] childrenIds) {
		super(alvisDB, id, name, type);
		this.synonyms = synonyms;
		this.pathIds = pathIds;
		this.ancestorIds = ancestorIds;
		this.childrenIds = childrenIds;
	}

	@Override
	public String[] getSynonyms() {
		return synonyms;
	}

	@Override
	public String[] getAncestorIds() {
		return ancestorIds;
	}

	@Override
	public String[] getChildrenIds() {
		return childrenIds;
	}

	@Override
	public String[] getPathIds() {
		return pathIds;
	}
}
