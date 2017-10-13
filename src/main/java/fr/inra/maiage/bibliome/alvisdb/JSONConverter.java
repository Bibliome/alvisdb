package fr.inra.maiage.bibliome.alvisdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class JSONConverter {
	public static final List<String> NONE = Collections.emptyList();
	public static final List<String> ALL_ENTITY_FIELDS = Arrays.asList("synonyms", "name", "path", "children");
	public static final List<String> MINIMAL_ENTITY_FIELDS = Arrays.asList("name");
	public static final List<String> ALL_ENTITY_TYPE_FIELDS = Arrays.asList("name", "root");

	public static JSONArray convert(ConnectionManager mngr, ADBEntity[] entities, List<String> fields, List<String> entityTypeFields, List<String> ancestorFields, List<String> childrenFields) throws JSONException, ADBException {
		JSONArray result = new JSONArray();
		for (ADBEntity entity : entities) {
			result.put(convert(mngr, entity, fields, entityTypeFields, ancestorFields, childrenFields));
		}
		return result;
	}

	public static JSONObject convert(ConnectionManager mngr, ADBEntity entity, List<String> fields, List<String> entityTypeFields, List<String> ancestorFields, List<String> childrenFields) throws JSONException, ADBException {
		JSONObject result = new JSONObject();
		result.put("id", entity.getId());
		ADBEntityType entityType = entity.getType();
		if (entityTypeFields.isEmpty()) {
			result.put("type", entityType.getId());
		}
		else {
			result.put("type", convert(mngr, entityType, entityTypeFields));
		}
		if (fields.contains("all")) {
			fields = ALL_ENTITY_FIELDS;
		}
		for (String f : fields) {
			switch (f) {
				case "id":
				case "type":
					break;
				case "synonyms":
					result.put("synonyms", Arrays.asList(entity.getSynonyms()));
					break;
				case "name":
					result.put("name", entity.getName());
					break;
				case "path":
					if (ancestorFields.isEmpty()) {
						result.put("path", Arrays.asList(entity.getPathIds()));
					}
					else {
						ADBEntity[] path = entity.getPath(mngr);
						JSONArray a = convert(mngr, path, ancestorFields, NONE, NONE, childrenFields);
						result.put("path", a);
					}
					break;
				case "children":
					if (childrenFields.isEmpty()) {
						result.put("children", Arrays.asList(entity.getChildrenIds()));
					}
					else {
						ADBEntity[] children = entity.getChildren(mngr);
						JSONArray a = convert(mngr, children, childrenFields, NONE, NONE, NONE);
						result.put("children", a);
					}
					break;
				default:
					result.put(f, "UNKOWN FIELD");
			}
		}
		return result;
	}

	public static JSONObject convert(ConnectionManager mngr, ADBEntityType entityType, List<String> fields) throws JSONException, ADBException {
		JSONObject result = new JSONObject();
		result.put("id", entityType.getId());
		if (fields.contains("all")) {
			fields = ALL_ENTITY_TYPE_FIELDS;
		}
		for (String f : fields) {
			switch (f) {
				case "id":
					break;
				case "name":
					result.put("name", entityType.getName());
					break;
				case "root":
					ADBEntity root = entityType.getRoot(mngr);
					result.put("root", root.getId());
					break;
			}
		}
		return result;
	}
	
	public static JSONObject convert(ADBRelationType relType) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("id", relType.getId());
		result.put("name", relType.getName());
		result.put("left", convert(relType.getLeft()));
		result.put("right", convert(relType.getRight()));
		return result;
	}

	private static JSONObject convert(ADBArgument arg) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("role", arg.getId());
		result.put("name", arg.getName());
		result.put("type", arg.getType().getId());
		return result;
	}
	
	public static JSONObject convert(ConnectionManager mngr, ADBRelation relation, boolean typeDetails, List<String> entityFields, List<String> entityTypeFields) throws JSONException, ADBException {
		JSONObject result = new JSONObject();
		result.put("id", relation.getId());
		result.put("name", relation.getName());
		ADBRelationType relType = relation.getType();
		if (typeDetails) {
			JSONObject rtj = convert(relType);
			result.put("type", rtj);
		}
		else {
			result.put("type", relType.getId());
		}
		JSONObject left = convert(mngr, relation.getLeft(), relType.getLeft().getId(), relation.getLeftAncestorIds(), entityFields, entityTypeFields);
		JSONObject right = convert(mngr, relation.getRight(), relType.getRight().getId(), relation.getRightAncestorIds(), entityFields, entityTypeFields);
		JSONArray args = new JSONArray();
		args.put(left);
		args.put(right);
		result.put("args", args);
		return result;
	}

	private static JSONObject convert(ConnectionManager mngr, ADBEntityOccurrence occ, String role, String[] ancestors, List<String> entityFields, List<String> entityTypeFields) throws JSONException, ADBException {
		JSONObject result = new JSONObject();
		ADBEntity entity = occ.getEntity(mngr);
		if (entityFields != null && !entityFields.isEmpty()) {
			JSONObject ej = convert(mngr, entity, entityFields, entityTypeFields, NONE, NONE);
			result.put("entity", ej);
		}
		else {
			result.put("entity", entity.getId());
		}
		result.put("role", role);
		if (ancestors != null) {
			JSONArray aj = new JSONArray();
			for (String a : ancestors) {
				aj.put(a);
			}
			result.put("ancestors",	aj);
		}
		result.put("doc", occ.getDocumentId());
		result.put("sec", occ.getSection());
		result.put("start", occ.getStart());
		result.put("end", occ.getEnd());
		return result;
	}

	public static JSONArray convert(ConnectionManager mngr, ADBRelation[] relations, boolean typeDetails, List<String> entityFields, List<String> entityTypeFields) throws JSONException, ADBException {
		JSONArray result = new JSONArray();
		for (ADBRelation relation : relations) {
			JSONObject rj = convert(mngr, relation, typeDetails, entityFields, entityTypeFields);
			result.put(rj);
		}
		return result;
	}
	
	
	
	
	
	public static JSONArray completion(ConnectionManager mngr, ADBEntityType entityType, String text, CompletionOperator op, int max, ADBRelationType relationType, String role, boolean useHierarchy) throws JSONException, ADBException {
		JSONArray result = new JSONArray();
		ADBEntity[] entities = entityType.completeEntities(mngr, text, op, Integer.MAX_VALUE);
		for (ADBEntity entity : entities) {
			if (result.length() >= max) {
				break;
			}
			if (relationType != null) {
				int nrel = relationType.countRelations(mngr, role, entity, useHierarchy);
				if (nrel == 0) {
					continue;
				}
			}
			JSONObject o = completion(entity, text, op);
			result.put(o);
		}
		return result;
	}
	
	public static JSONObject completion(ADBEntity entity, String text, CompletionOperator op) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("value", entity.getId());
		result.put("label", completionString(entity, text, op));
		return result;
	}

	private static String completionString(ADBEntity entity, String text, CompletionOperator op) {
		String name = entity.getName();
		if (op.match(name, text)) {
			return name;
		}
		for (String s : entity.getSynonyms()) {
			if (op.match(s, text)) {
				return s + " [" + name + "]";
			}
		}
		return "";
	}
	
	public static JSONObject expansionWait(ADBEntity entity) throws JSONException {
		String id = entity.getId();
		JSONObject result = new JSONObject();
		result.put("label", entity.getName());
		result.put("value", id);
		result.put("items", fakeChild(id));
		return result;
	}
	
	public static JSONArray expansionChildren(ConnectionManager mngr, ADBEntity entity, ADBRelationType relationType, String role, boolean useHierarchy) throws ADBException, JSONException {
		JSONArray result = new JSONArray();
		ADBEntity[] children = entity.getChildren(mngr);
		Arrays.sort(children, ADBElement.NameComparator.INSTANCE);
		for (ADBEntity child : children) {
			if (relationType != null) {
				int nrel = relationType.countRelations(mngr, role, child, useHierarchy);
				if (nrel == 0) {
					continue;
				}
			}
			JSONObject o = expansionWait(child);
			result.put(o);
		}
		return result;
	}
	
	private static JSONArray fakeChild(String id) throws JSONException {
		JSONArray result = new JSONArray();
		JSONObject fakeChild = new JSONObject();
		fakeChild.put("label", "Loading...");
		fakeChild.put("value", id);
		result.put(fakeChild);
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	public static class RelationQuery {
		private final ADBRelationType relationType;
		private final ADBEntity pivot;
		private final String pivotRole;
		private final boolean pivotUseHierarchy;
		private final boolean otherUseHierarchy;
		
		public RelationQuery(ADBRelationType relationType, ADBEntity pivot, String pivotRole, boolean pivotUseHierarchy, boolean otherUseHierarchy) {
			super();
			this.relationType = relationType;
			this.pivot = pivot;
			this.pivotRole = pivotRole;
			this.pivotUseHierarchy = pivotUseHierarchy;
			this.otherUseHierarchy = otherUseHierarchy;
		}

		public int count(ConnectionManager mngr, ADBEntity other) throws ADBException {
			return relationType.countRelations(mngr, pivotRole, pivot, pivotUseHierarchy, other, otherUseHierarchy);
		}
		
		public ADBRelation[] list(ConnectionManager mngr, ADBEntity other, int skip, int max, String sort, boolean descSort) throws ADBException {
			return relationType.listRelations(mngr, pivotRole, pivot, pivotUseHierarchy, other, otherUseHierarchy, skip, max, sort, descSort);
		}

		public ADBRelationType getRelationType() {
			return relationType;
		}

		public String getPivotRole() {
			return pivotRole;
		}

		public ADBEntity getPivot() {
			return pivot;
		}

		public boolean isPivotUseHierarchy() {
			return pivotUseHierarchy;
		}

		public boolean isOtherUseHierarchy() {
			return otherUseHierarchy;
		}
	}
	
	private static class EntityCount {
		private static final Comparator<EntityCount> COMPARATOR = new Comparator<EntityCount>() {
			@Override
			public int compare(EntityCount o1, EntityCount o2) {
				return Integer.compare(o2.count, o1.count);
			}
		};
		private final ADBEntity entity;
		private final int count;
		private final double value;
		
		private EntityCount(ADBEntity entity, int count, CountFunction fun) {
			super();
			this.entity = entity;
			this.count = count;
			this.value = fun.value(count);
		}

		private JSONObject toJSON(String parentid, int depth) throws JSONException {
			JSONObject result = new JSONObject();
			result.put("id", entity.getId());
			result.put("parentid", parentid);
			result.put("text", entity.getName());
			result.put("value", value);
			JSONObject data = new JSONObject();
			data.put("id", entity.getId());
			data.put("depth", depth);
			data.put("count", count);
			result.put("data", data);
			return result;
		}
	}
	
	private static enum CountFunction {
		COUNT {
			@Override
			double value(int count) {
				return count;
			}
		},
		
		SQRT {
			@Override
			double value(int count) {
				return Math.sqrt(count);
			}
		},
		
		LOG {
			@Override
			double value(int count) {
				return Math.log1p(count);
			}
		};
		
		abstract double value(int count);
		
		static CountFunction get(String s) {
			switch (s.toLowerCase()) {
				case "sqrt": return CountFunction.SQRT;
				case "log": return CountFunction.LOG;
				default: return CountFunction.COUNT;
			}
		}
	}

	private static JSONObject treeMap(ConnectionManager mngr, RelationQuery query, ADBEntity other, CountFunction fun, JSONArray items, int depth, int maxDepth, String parentid) throws ADBException, JSONException {
		double totalValue = 0;
		double maxValue = 0;
		for (EntityCount c : getDescendants(mngr, query, other, fun)) {
			JSONObject cj = c.toJSON(parentid, depth);
			items.put(cj);
			totalValue += c.value;
			maxValue = Math.max(maxValue, c.value);
			if (depth < maxDepth) {
				treeMap(mngr, query, c.entity, fun, items, depth + 1, maxDepth, c.entity.getId());
			}
		}
		if (items.length() == 0) {
			int n = query.count(mngr, other);
			EntityCount oc = new EntityCount(other, n, fun);
			JSONObject oj = oc.toJSON(parentid, depth);
			items.put(oj);
			totalValue = oc.value;
			maxValue = oc.value;
		}
		JSONObject result = new JSONObject();
		result.put("items", items);
		result.put("total", totalValue);
		result.put("max", maxValue);
		return result;
	}
	
	public static JSONObject treeMap(ConnectionManager mngr, RelationQuery query, ADBEntity other, String fun, int maxDepth) throws ADBException, JSONException {
		JSONArray items = new JSONArray();
		CountFunction cfun = CountFunction.get(fun);
		return treeMap(mngr, query, other, cfun, items, 1, maxDepth, "");
	}
	
	private static List<EntityCount> getDescendants(ConnectionManager mngr, RelationQuery query, ADBEntity other, CountFunction fun) throws ADBException {
		List<EntityCount> result = new ArrayList<EntityCount>();
		for (ADBEntity child : other.getChildren(mngr)) {
			int n = query.count(mngr, child);
			if (n > 0) {
				EntityCount c = new EntityCount(child, n, fun);
				result.add(c);
			}
		}
		if (result.size() == 1) {
			EntityCount c = result.get(0);
			List<EntityCount> childResult = getDescendants(mngr, query, c.entity, fun);
			if (!childResult.isEmpty()) {
				return childResult;
			}
		}
		Collections.sort(result, EntityCount.COMPARATOR);
		return result;
	}



	public static JSONObject table(ConnectionManager mngr, RelationQuery query, ADBEntity other, int page, int pageSize, String title, String sort, boolean descSort) throws ADBException, JSONException {
		JSONArray list = new JSONArray();
		AlvisDB adb = query.relationType.getAlvisDB();
		ADBRelation[] relations = query.list(mngr, other, page*pageSize, pageSize, sort, descSort);
		String pivotRole = query.pivotRole;
		String otherRole = query.relationType.getOther(pivotRole).getId();
		String key_doc = "doc";
		String key_sec = "section";
		String key_title = "title";
		String key_pivotId = pivotRole + "-id";
		String key_pivotName = pivotRole + "-name";
		String key_pivotForm = pivotRole + "-form";
		String key_otherId = otherRole + "-id";
		String key_otherName = otherRole + "-name";
		String key_otherForm = otherRole + "-form";

		for (ADBRelation rel : relations) {
			JSONObject rj = new JSONObject();
			ADBEntityOccurrence pivotOcc = rel.get(pivotRole);
			ADBEntityOccurrence otherOcc = rel.get(otherRole);
			String docId = pivotOcc.getDocumentId();
			String sec = pivotOcc.getSection();
			ADBDocument doc = adb.getDocument(mngr, docId);
			String docTitle = doc.getSection(title);
			rj.put(key_doc, docId);
			rj.put(key_sec, sec);
			rj.put(key_title, docTitle);
			ADBEntity pivotArg = pivotOcc.getEntity(mngr);
			ADBEntity otherArg = otherOcc.getEntity(mngr);
			rj.put(key_pivotId, pivotArg.getId());
			rj.put(key_pivotName, pivotArg.getName());
			rj.put(key_otherId, otherArg.getId());
			rj.put(key_otherName, otherArg.getName());
			rj.put(key_pivotForm, pivotOcc.getForm(mngr, adb));
			rj.put(key_otherForm, otherOcc.getForm(mngr, adb));
			list.put(rj);
		}
		JSONObject result = new JSONObject();
		int n = query.count(mngr, other);
		result.put("list", list);
		result.put("count", n);
		return result;
	}
}
