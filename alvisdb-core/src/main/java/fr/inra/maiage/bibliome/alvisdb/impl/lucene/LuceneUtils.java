package fr.inra.maiage.bibliome.alvisdb.impl.lucene;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import fr.inra.maiage.bibliome.alvisdb.ADBArgument;
import fr.inra.maiage.bibliome.alvisdb.ADBException;
import fr.inra.maiage.bibliome.alvisdb.ADBRelationType;
import fr.inra.maiage.bibliome.alvisdb.ConnectionManager;

public class LuceneUtils {
	public static class Fields {
		public static final String TYPE = "TYPE";
		public static final String ID = "ID";
		public static final String NAME = "NAME";
		public static final String SYNONYMS = "SYNONYMS";
		public static final String ANCESTORS = "ANCESTORS";
		public static final String CHILDREN = "CHILDREN";
		public static final String PATH = "PATH";
		
		public static enum ArgSide {
			LEFT("LEFT_ID", "LEFT_NAME", "LEFT_ANCESTORS", "LEFT_DOC", "LEFT_SEC", "LEFT_START", "LEFT_END") {
				@Override
				ADBArgument getArgument(ADBRelationType type) {
					return type.getLeft();
				}
			},
			
			RIGHT("RIGHT_ID", "RIGHT_NAME", "RIGHT_ANCESTORS", "RIGHT_DOC", "RIGHT_SEC", "RIGHT_START", "RIGHT_END") {
				@Override
				ADBArgument getArgument(ADBRelationType type) {
					return type.getRight();
				}
			};

			public final String ENTITY;
			@SuppressWarnings("hiding")
			public final String NAME;
			@SuppressWarnings("hiding")
			public final String ANCESTORS;
			public final String DOCUMENT;
			public final String SECTION;
			public final String START;
			public final String END;
			
			private ArgSide(String ENTITY, String NAME, String ANCESTORS, String DOCUMENT, String SECTION, String START, String END) {
				this.ENTITY = ENTITY;
				this.NAME = NAME;
				this.ANCESTORS = ANCESTORS;
				this.DOCUMENT = DOCUMENT;
				this.SECTION = SECTION;
				this.START = START;
				this.END = END;
			}
			
			abstract ADBArgument getArgument(ADBRelationType type);
		}
	}
	
	static IndexSearcher ensureIndex(ConnectionManager mngr, LuceneElement elt) throws ADBException {
		String indexPath = elt.getIndexPath();
		Closeable connection = mngr.getConnection(indexPath);
		if (connection != null) {
			return castIndexSearcher(connection);
		}
		return createIndexSearcher(mngr, indexPath);
	}
	
	private static IndexSearcher castIndexSearcher(Closeable connection) throws ADBException {
		try {
			return (IndexSearcher) connection;
		}
		catch (ClassCastException e) {
			throw new ADBException(e);
		} 
	}
	
	private static IndexSearcher createIndexSearcher(ConnectionManager mngr, String indexPath) throws ADBException {
		try (Directory dir = FSDirectory.open(new File(indexPath))) {
			IndexReader reader = IndexReader.open(dir);
			IndexSearcher result = new IndexSearcher(reader);
			mngr.addConnection(indexPath, result);
			return result;
		}
		catch (IOException e) {
			throw new ADBException(e);
		}
	}
}
