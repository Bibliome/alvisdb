package fr.inra.maiage.bibliome.alvisdb.impl.lucene;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import fr.inra.maiage.bibliome.alvisdb.ADBEntity;
import fr.inra.maiage.bibliome.alvisdb.ADBException;
import fr.inra.maiage.bibliome.alvisdb.AlvisDB;
import fr.inra.maiage.bibliome.alvisdb.CompletionOperator;
import fr.inra.maiage.bibliome.alvisdb.ConnectionManager;
import fr.inra.maiage.bibliome.alvisdb.impl.AbstractADBEntityType;

public class LuceneEntityType extends AbstractADBEntityType implements LuceneElement {
	private final String indexPath;
	private final Query typeQuery;

	public LuceneEntityType(AlvisDB alvisDB, String id, String name, String rootId, String indexPath) throws ADBException {
		super(alvisDB, id, name, rootId);
		this.indexPath = indexPath;
		Term term = new Term(LuceneUtils.Fields.TYPE, id);
		typeQuery = new TermQuery(term);
	}

	@Override
	public ADBEntity getEntity(ConnectionManager mngr, String id) throws ADBException {
		try {
			Term term = new Term(LuceneUtils.Fields.ID, id);
			Query termQuery = new TermQuery(term);
			Query query = getTypeConjunction(termQuery);
			IndexSearcher searcher = LuceneUtils.ensureIndex(mngr, this);
			TopDocs topDocs = searcher.search(query, 1);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			if (scoreDocs.length == 0) {
				throw new ADBException("no entity of type " + getId() + ", with id: " + id);
			}
			int docId = scoreDocs[0].doc;
			return createLuceneEntity(searcher, docId);
		}
		catch (IOException e) {
			throw new ADBException(e);
		}
	}
	
	private LuceneEntity createLuceneEntity(IndexSearcher searcher, int docId) throws IOException {
		Document doc = searcher.doc(docId);
		return new LuceneEntity(getAlvisDB(), this, doc);
	}

	@Override
	public ADBEntity[] listEntities(ConnectionManager mngr, int start, int n) throws ADBException {
		try {
			IndexSearcher searcher = LuceneUtils.ensureIndex(mngr, this);
			TopDocs topDocs = searcher.search(typeQuery, start + n);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			int len = Math.min(0, scoreDocs.length - start);
			ADBEntity[] result = new ADBEntity[len];
			for (int i = 0; i < len; ++i) {
				result[i] = createLuceneEntity(searcher, scoreDocs[i + start].doc);
			}
			return result;
		}
		catch (IOException e) {
			throw new ADBException(e);
		}
	}
	
	private static Query getQuery(String s, CompletionOperator op) {
		switch (op) {
			case CONTAINS: {
				Term term = new Term(LuceneUtils.Fields.SYNONYMS, "*" + s + "*");
				return new WildcardQuery(term);
			}
			case STARTS_WITH: {
				Term term = new Term(LuceneUtils.Fields.SYNONYMS, s);
				return new PrefixQuery(term);
			}
		}
		throw new RuntimeException("unknown completion operator: " + op);
	}

	@Override
	public ADBEntity[] completeEntities(ConnectionManager mngr, String s, CompletionOperator op, int max) throws ADBException {
		try {
			Query completeQuery = getQuery(s, op);
			Query query = getTypeConjunction(completeQuery);
			IndexSearcher searcher = LuceneUtils.ensureIndex(mngr, this);
			TopDocs topDocs = searcher.search(query, max);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			ADBEntity[] result = new ADBEntity[scoreDocs.length];
			for (int i = 0; i < scoreDocs.length; ++i) {
				result[i] = createLuceneEntity(searcher, scoreDocs[i].doc);
			}
			return result;
		}
		catch (IOException e) {
			throw new ADBException(e);
		}
	}
	
	private Query getTypeConjunction(Query query) {
		BooleanQuery result = new BooleanQuery();
		result.add(typeQuery, Occur.MUST);
		result.add(query, Occur.MUST);
		return result;
	}

	@Override
	public String getIndexPath() {
		return indexPath;
	}
}
