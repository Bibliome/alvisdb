package org.bibliome.alvisdb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AlvisDB implements ADBDocumentFactory {
	private final Map<String,ADBEntityType> entityTypes = new HashMap<String,ADBEntityType>();
	private final Map<String,ADBRelationType> relationTypes = new HashMap<String,ADBRelationType>();
	private ADBDocumentFactory documentFactory;
	
	public Collection<ADBEntityType> getEntityTypes() {
		Collection<ADBEntityType> entityTypes = this.entityTypes.values();
		return Collections.unmodifiableCollection(entityTypes);
	}
	
	public Collection<ADBRelationType> getRelationTypes() {
		Collection<ADBRelationType> relationTypes = this.relationTypes.values();
		return Collections.unmodifiableCollection(relationTypes);
	}
	
	public ADBEntityType getEntityType(String id) throws ADBException {
		return getEntityType(id, "");
	}
	
	private ADBEntityType getEntityType(String id, String msgComplement) throws ADBException {
		if (entityTypes.containsKey(id)) {
			return entityTypes.get(id);
		}
		throw new ADBException(msgComplement + "unknown entity type: " + id);
	}
	
	public ADBRelationType getRelationType(String id) throws ADBException {
		if (relationTypes.containsKey(id)) {
			return relationTypes.get(id);
		}
		throw new ADBException("unknown relation type: " + id);
	}
	
	public void addEntityType(ADBEntityType type) throws ADBException {
		if (type == null) {
			throw new NullPointerException();
		}
		if (!this.equals(type.getAlvisDB())) {
			throw new ADBException("database mismatch");
		}
		String id = type.getId();
		if (entityTypes.containsKey(id)) {
			throw new ADBException("duplicate type id: " + id);
		}
		entityTypes.put(id, type);
	}
	
	public void addRelationType(ADBRelationType type) throws ADBException {
		if (type == null) {
			throw new NullPointerException();
		}
		if (!this.equals(type.getAlvisDB())) {
			throw new ADBException("database mismatch");
		}
		String id = type.getId();
		if (relationTypes.containsKey(id)) {
			throw new ADBException("duplicate relation id: " + id);
		}
		checkArgument(type.getLeft(), id);
		checkArgument(type.getRight(), id);
		relationTypes.put(id, type);
	}
	
	private void checkArgument(ADBArgument arg, String relId) throws ADBException {
		ADBEntityType type = arg.getType();
		String id = type.getId();
		String msgComplement = "in relation " + relId + ", arg " + arg.getId() + ", ";
		ADBEntityType supposedType = getEntityType(id, msgComplement);
		if (!type.equals(supposedType)) {
			throw new ADBException(msgComplement + "unmatching entity types: " + id); 
		}
	}

	public void setDocumentFactory(ADBDocumentFactory documentFactory) {
		this.documentFactory = documentFactory;
	}

	@Override
	public ADBDocument getDocument(ConnectionManager mngr, String id) throws ADBException {
		if (documentFactory == null) {
			throw new ADBException("no document factory");
		}
		return documentFactory.getDocument(mngr, id);
	}
}
