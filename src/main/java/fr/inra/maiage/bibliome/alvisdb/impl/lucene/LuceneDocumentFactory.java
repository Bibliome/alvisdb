package fr.inra.maiage.bibliome.alvisdb.impl.lucene;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import fr.inra.maiage.bibliome.alvisdb.ADBDocument;
import fr.inra.maiage.bibliome.alvisdb.ADBDocumentFactory;
import fr.inra.maiage.bibliome.alvisdb.ADBException;
import fr.inra.maiage.bibliome.alvisdb.ConnectionManager;

public class LuceneDocumentFactory implements ADBDocumentFactory, LuceneElement {
	private final String indexPath;
	private final String idField;
	
	public LuceneDocumentFactory(String indexPath, String idField) {
		super();
		this.indexPath = indexPath;
		this.idField = idField;
	}

	@Override
	public ADBDocument getDocument(ConnectionManager mngr, String id) throws ADBException {
		try (IndexSearcher searcher = LuceneUtils.ensureIndex(mngr, this)) {
			Term term = new Term(idField, id);
			Query query = new TermQuery(term);
			TopDocs topDocs = searcher.search(query, 1);
			if (topDocs.totalHits == 0) {
				throw new ADBException("no document with id: " + id);
			}
			Document doc = searcher.doc(topDocs.scoreDocs[0].doc);
			return new LuceneDocument(doc);
		}
		catch (IOException e) {
			throw new ADBException(e);
		}
	}
	
	@Override
	public String getIndexPath() {
		return indexPath;
	}

	private class LuceneDocument implements ADBDocument {
		private final Document doc;

		private LuceneDocument(Document doc) {
			super();
			this.doc = doc;
		}

		@Override
		public String getId() {
			return doc.get(idField);
		}

		@Override
		public String getSection(String name) throws ADBException {
			String result = doc.get(name);
			if (result == null) {
				throw new ADBException("no section " + name);
			}
			return result;
		}
	}
}
