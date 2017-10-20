package fr.inra.maiage.bibliome.alvisdb.impl.lucene;

import org.apache.lucene.document.Document;

import fr.inra.maiage.bibliome.alvisdb.ADBArgument;
import fr.inra.maiage.bibliome.alvisdb.ADBDocument;
import fr.inra.maiage.bibliome.alvisdb.ADBDocumentFactory;
import fr.inra.maiage.bibliome.alvisdb.ADBEntity;
import fr.inra.maiage.bibliome.alvisdb.ADBEntityOccurrence;
import fr.inra.maiage.bibliome.alvisdb.ADBEntityType;
import fr.inra.maiage.bibliome.alvisdb.ADBException;
import fr.inra.maiage.bibliome.alvisdb.ADBRelationType;
import fr.inra.maiage.bibliome.alvisdb.AlvisDB;
import fr.inra.maiage.bibliome.alvisdb.ConnectionManager;
import fr.inra.maiage.bibliome.alvisdb.impl.AbstractADBRelation;
import fr.inra.maiage.bibliome.alvisdb.impl.lucene.LuceneUtils.Fields.ArgSide;

class LuceneRelation extends AbstractADBRelation {
	public LuceneRelation(AlvisDB alvisDB, ConnectionManager mngr, ADBRelationType type, Document doc) throws ADBException {
		super(alvisDB, doc.get(LuceneUtils.Fields.ID), doc.get(LuceneUtils.Fields.NAME), mngr, type, new LuceneEntityOccurrence(doc, type, ArgSide.LEFT), new LuceneEntityOccurrence(doc, type, ArgSide.RIGHT), doc.getValues(ArgSide.LEFT.ANCESTORS), doc.getValues(ArgSide.RIGHT.ANCESTORS));
	}
	
	private static class LuceneEntityOccurrence implements ADBEntityOccurrence {
		private final Document doc;
		private final ADBRelationType type;
		private final LuceneUtils.Fields.ArgSide argSide;
		
		private LuceneEntityOccurrence(Document doc, ADBRelationType type, ArgSide argSide) {
			super();
			this.doc = doc;
			this.type = type;
			this.argSide = argSide;
		}

		@Override
		public ADBEntity getEntity(ConnectionManager mngr) throws ADBException {
			String id = doc.get(argSide.ENTITY);
			ADBArgument arg = argSide.getArgument(type);
			ADBEntityType argType = arg.getType();
			return argType.getEntity(mngr, id);
		}

		@Override
		public String getDocumentId() {
			return doc.get(argSide.DOCUMENT);
		}

		@Override
		public String getSection() {
			return doc.get(argSide.SECTION);
		}

		@Override
		public int getStart() {
			try {
				return Integer.parseInt(doc.get(argSide.START));
			}
			catch (NumberFormatException e) {
				return 0;
			}
		}

		@Override
		public int getEnd() {
			try {
				return Integer.parseInt(doc.get(argSide.END));
			}
			catch (NumberFormatException e) {
				return 0;
			}
		}

		@Override
		public String getForm(ConnectionManager mngr, ADBDocumentFactory docFactory) throws ADBException {
			ADBDocument doc = docFactory.getDocument(mngr, getDocumentId());
			String section = doc.getSection(getSection());
			int start = getStart();
			int end = getEnd();
			try {
				return section.substring(start, end);
			}
			catch (StringIndexOutOfBoundsException e) {
				throw new RuntimeException(String.format("'%s':%d %d-%d", section, section.length(), start, end), e);
			}
		}
		
	}
}
