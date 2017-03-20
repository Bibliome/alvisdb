package org.bibliome.alvisdb.impl.lucene;

import org.apache.lucene.document.Document;
import org.bibliome.alvisdb.ADBEntityType;
import org.bibliome.alvisdb.AlvisDB;
import org.bibliome.alvisdb.impl.AbstractADBEntity;

class LuceneEntity extends AbstractADBEntity {
	private final Document doc;

	LuceneEntity(AlvisDB alvisDB, ADBEntityType type, Document doc) {
		super(alvisDB, doc.get(LuceneUtils.Fields.ID), doc.get(LuceneUtils.Fields.NAME), type);
		this.doc = doc;
	}

	@Override
	public String[] getSynonyms() {
		return doc.getValues(LuceneUtils.Fields.SYNONYMS);
	}

	@Override
	public String[] getAncestorIds() {
		return doc.getValues(LuceneUtils.Fields.ANCESTORS);
	}

	@Override
	public String[] getChildrenIds() {
		return doc.getValues(LuceneUtils.Fields.CHILDREN);
	}

	@Override
	public String[] getPathIds() {
		return doc.getValues(LuceneUtils.Fields.PATH);
	}
}
