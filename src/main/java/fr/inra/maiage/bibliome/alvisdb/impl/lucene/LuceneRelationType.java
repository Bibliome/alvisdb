package fr.inra.maiage.bibliome.alvisdb.impl.lucene;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;

import fr.inra.maiage.bibliome.alvisdb.ADBArgument;
import fr.inra.maiage.bibliome.alvisdb.ADBDocument;
import fr.inra.maiage.bibliome.alvisdb.ADBDocumentFactory;
import fr.inra.maiage.bibliome.alvisdb.ADBEntity;
import fr.inra.maiage.bibliome.alvisdb.ADBException;
import fr.inra.maiage.bibliome.alvisdb.ADBRelation;
import fr.inra.maiage.bibliome.alvisdb.AlvisDB;
import fr.inra.maiage.bibliome.alvisdb.ConnectionManager;
import fr.inra.maiage.bibliome.alvisdb.impl.AbstractADBRelationType;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;

public class LuceneRelationType extends AbstractADBRelationType implements LuceneElement {
	private final String indexPath;
	private final Query typeQuery;

	public LuceneRelationType(AlvisDB alvisDB, String id, String name, ADBArgument left, ADBArgument right, String indexPath) throws ADBException {
		super(alvisDB, id, name, left, right);
		this.indexPath = indexPath;
		Term term = new Term(LuceneUtils.Fields.TYPE, id);
		typeQuery = new TermQuery(term);
	}
	
	private Query getQuery(ADBEntity left, boolean leftUseHierarchy, ADBEntity right, boolean rightUseHierarchy) {
		BooleanQuery result = new BooleanQuery();
		result.add(typeQuery, Occur.MUST);
		addClause(result, LuceneUtils.Fields.ArgSide.LEFT, left, leftUseHierarchy);
		addClause(result, LuceneUtils.Fields.ArgSide.RIGHT, right, rightUseHierarchy);
		return result;
	}

	private static void addClause(BooleanQuery result, LuceneUtils.Fields.ArgSide argSide, ADBEntity arg, boolean useHierarchy) {
		if (arg == null) {
			return;
		}
		String id = arg.getId();
		String field = useHierarchy && arg.getType().hasHierarchy() ? argSide.ANCESTORS : argSide.ENTITY;
		Term term = new Term(field, id);
		Query query = new TermQuery(term);
		result.add(query, Occur.MUST);
	}

	@Override
	public int countRelations(ConnectionManager mngr, ADBEntity left, boolean leftUseHierarchy, ADBEntity right, boolean rightUseHierarchy) throws ADBException {
		try {
			IndexSearcher searcher = LuceneUtils.ensureIndex(mngr, this);
			Query query = getQuery(left, leftUseHierarchy, right, rightUseHierarchy);
			TotalHitCountCollector collector = new TotalHitCountCollector();
			searcher.search(query, collector);
			return collector.getTotalHits();
		}
		catch (IOException e) {
			throw new ADBException(e);
		}
	}
	
	private LuceneUtils.Fields.ArgSide getArgSide(String role) throws ADBException {
		if (getLeft().getId().equals(role)) {
			return LuceneUtils.Fields.ArgSide.LEFT;
		}
		if (getRight().getId().equals(role)) {
			return LuceneUtils.Fields.ArgSide.RIGHT;
		}
		throw new ADBException("no argument with role: " + role);
	}
	
	private TopDocs search(ConnectionManager mngr, ADBDocumentFactory docFactory, String title, IndexSearcher searcher, Query query, int n, String sort, boolean descSort) throws IOException, ADBException {
		if (sort == null || sort.isEmpty()) {
			return searcher.search(query, n);
		}
		TopDocs result = searcher.search(query, Integer.MAX_VALUE);
		Comparator<ScoreDoc> comp = getSortField(mngr, docFactory, title, searcher, sort, descSort);
		Arrays.sort(result.scoreDocs, comp);
		return result;
	}
	
	private static class HitComparator implements Comparator<ScoreDoc> {
		private final IndexSearcher searcher;
		private final String field;
		private final int factor;
		private final ConnectionManager mngr;
		private final ADBDocumentFactory docFactory;
		private final String title;
		
		public HitComparator(IndexSearcher searcher, String field, boolean desc, ConnectionManager mngr, ADBDocumentFactory docFactory, String title) {
			super();
			this.searcher = searcher;
			this.field = field;
			this.factor = desc ? -1 : 1;
			this.mngr = mngr;
			this.docFactory = docFactory;
			this.title = title;
		}

//		private HitComparator(IndexSearcher searcher, String field, boolean desc) {
//			this(searcher, field, desc, null, null, null);
//		}

		@Override
		public int compare(ScoreDoc o1, ScoreDoc o2) {
			try {
				int did1 = o1.doc;
				int did2 = o2.doc;
				Document doc1 = searcher.doc(did1);
				Document doc2 = searcher.doc(did2);
				String f1 = doc1.get(field);
				String f2 = doc2.get(field);
				if (f1 == null) {
					if (f2 == null) {
						return 0;
					}
					return -1*factor;
				}
				if (f2 == null) {
					return 1*factor;
				}
				if (docFactory != null) {
					ADBDocument adbdoc1 = docFactory.getDocument(mngr, f1);
					ADBDocument adbdoc2 = docFactory.getDocument(mngr, f2);
					f1 = adbdoc1.getSection(title);
					f2 = adbdoc2.getSection(title);
				}
				return f1.compareToIgnoreCase(f2)*factor;
			}
			catch (IOException|ADBException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private HitComparator getSortField(ConnectionManager mngr, ADBDocumentFactory docFactory, String title, IndexSearcher searcher, String sort, boolean desc) throws ADBException {
		String sortArg;
		String sortProp;
		int dash = sort.indexOf('-');
		if (dash == -1) {
			sortArg = sort;
			sortProp = "name";
		}
		else {
			sortArg = sort.substring(0, dash);
			sortProp = sort.substring(dash+1);
		}
		LuceneUtils.Fields.ArgSide argSide = getArgSide(sortArg);
		switch (sortProp) {
			case "name":
				return new HitComparator(searcher, argSide.NAME, desc, null, null, null);
			case "title":
				return new HitComparator(searcher, argSide.DOCUMENT, desc, mngr, docFactory, title);
			default: 
				return new HitComparator(searcher, argSide.NAME, desc, null, null, null);
		}
	}

	@Override
	public ADBRelation[] listRelations(ConnectionManager mngr, ADBEntity left, boolean leftUseHierarchy, ADBEntity right, boolean rightUseHierarchy, int skip, int max, String sort, boolean descSort) throws ADBException {
		try {
			IndexSearcher searcher = LuceneUtils.ensureIndex(mngr, this);
			Query query = getQuery(left, leftUseHierarchy, right, rightUseHierarchy);
			AlvisDB adb = getAlvisDB();
			TopDocs topDocs = search(mngr, adb, "title", searcher, query, skip + max, sort, descSort);
//			TopDocs topDocs = searcher.search(query, skip + max);
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			int n = Math.min(max, Math.max(0, scoreDocs.length - skip));
			ADBRelation[] result = new ADBRelation[n];
			for (int i = 0; i < n; ++i) {
				Document doc = searcher.doc(scoreDocs[skip+i].doc);
				result[i] = new LuceneRelation(getAlvisDB(), mngr, this, doc);
			}
			return result;
		}
		catch (IOException e) {
			throw new ADBException(e);
		}
	}

	@Override
	public String getIndexPath() {
		return indexPath;
	}
}
