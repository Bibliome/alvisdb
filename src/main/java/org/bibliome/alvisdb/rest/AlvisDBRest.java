package org.bibliome.alvisdb.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bibliome.alvisdb.ADBArgument;
import org.bibliome.alvisdb.ADBDocument;
import org.bibliome.alvisdb.ADBEntity;
import org.bibliome.alvisdb.ADBEntityOccurrence;
import org.bibliome.alvisdb.ADBEntityType;
import org.bibliome.alvisdb.ADBException;
import org.bibliome.alvisdb.ADBRelation;
import org.bibliome.alvisdb.ADBRelationType;
import org.bibliome.alvisdb.AlvisDB;
import org.bibliome.alvisdb.AlvisDBBuilder;
import org.bibliome.alvisdb.CompletionOperator;
import org.bibliome.alvisdb.ConnectionManager;
import org.bibliome.alvisdb.JSONConverter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Path("/obt")
public class AlvisDBRest {
	public static final String CONFIG_PARAM = "alvisdb.config";
	
	private final AlvisDB adb;

	public AlvisDBRest(@Context ServletContext servletContext) throws ADBException {
		String configPath = servletContext.getInitParameter(CONFIG_PARAM);
		AlvisDBBuilder builder = new AlvisDBBuilder();
		//builder.buildFromDOM("/home/rbossy/code/alvisdb/test/alvisdb.xml");
		builder.buildFromDOM(configPath);
		adb = builder.getAlvisDB();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/entities")
	public String getEntities(@QueryParam("fields") List<String> fields) {
		try(ConnectionManager mngr = new ConnectionManager()) {
			Collection<ADBEntityType> types = adb.getEntityTypes();
			JSONArray result = new JSONArray();
			for (ADBEntityType et : types) {
				JSONObject json = JSONConverter.convert(mngr, et, fields);
				result.put(json);
			}
			return result.toString(4);
		}
		catch (JSONException|ADBException|IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/relations")
	public String getRelations() {
		try {
			Collection<ADBRelationType> types = adb.getRelationTypes();
			JSONArray result = new JSONArray();
			for (ADBRelationType rt : types) {
				JSONObject json = JSONConverter.convert(rt);
				result.put(json);
			}
			return result.toString(4);
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("entity/{type}/{id}")
	public String getEntity(
			@PathParam("type") String type,
			@PathParam("id") String id,
			@QueryParam("fields") List<String> fields,
			@QueryParam("typefields") List<String> entityTypeFields,
			@QueryParam("pathfields") List<String> ancestorFields,
			@QueryParam("childfields") List<String> childrenFields
			) {
		try (ConnectionManager mngr = new ConnectionManager()) {
			ADBEntityType et = adb.getEntityType(type);
			ADBEntity e = et.getEntity(mngr, id);
			JSONObject result = JSONConverter.convert(mngr, e, fields, entityTypeFields, ancestorFields, childrenFields);
			return result.toString(4);
		}
		catch (ADBException|JSONException|IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("entity/{type}")
	public String getRoot(
			@PathParam("type") String type,
			@QueryParam("fields") List<String> fields,
			@QueryParam("typefields") List<String> entityTypeFields,
			@QueryParam("pathfields") List<String> ancestorFields,
			@QueryParam("childfields") List<String> childrenFields
			) {
		try (ConnectionManager mngr = new ConnectionManager()) {
			ADBEntityType et = adb.getEntityType(type);
			ADBEntity e = et.getRoot(mngr);
			JSONObject result = JSONConverter.convert(mngr, e, fields, entityTypeFields, ancestorFields, childrenFields);
			return result.toString(4);
		}
		catch (ADBException|JSONException|IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("browse")
	public InputStream browse() {
		Class<?> klass = getClass();
		return klass.getResourceAsStream("browse.html");
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("browse2")
	public InputStream browse2() {
		Class<?> klass = getClass();
		return klass.getResourceAsStream("browse2.html");
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("complete/{type}/{text}")
	public String completeEntity(
			@PathParam("type") String type,
			@PathParam("text") String text,
			@DefaultValue("false") @QueryParam("contains") boolean contains,
			@DefaultValue("10") @QueryParam("max") int max,
			@QueryParam("relation") String rel,
			@QueryParam("role") String role
			) {
		try (ConnectionManager mngr = new ConnectionManager()) {
			ADBEntityType et = adb.getEntityType(type);
			CompletionOperator op = contains ? CompletionOperator.CONTAINS : CompletionOperator.STARTS_WITH;
			JSONArray json;
			if (rel != null && !rel.isEmpty() && role != null && !role.isEmpty()) {
				ADBRelationType rt = adb.getRelationType(rel);
				json = JSONConverter.completion(mngr, et, text, op, max, rt, role, true);
			}
			else {
				json = JSONConverter.completion(mngr, et, text, op, max, null, null, false);
			}
			return json.toString(4);
		}
		catch (ADBException|IOException|JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("root/{type}")
	public String root(
			@PathParam("type") String type
			) {
		try (ConnectionManager mngr = new ConnectionManager()) {
			ADBEntityType et = adb.getEntityType(type);
			ADBEntity root = et.getRoot(mngr);
			JSONObject json = JSONConverter.expansionWait(root);
			JSONArray a = new JSONArray();
			a.put(json);
			return a.toString(4);
		}
		catch (ADBException|JSONException|IOException e) {
			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("children/{type}/{id}")
	public String children(
			@PathParam("type") String type,
			@PathParam("id") String id,
			@QueryParam("relation") String rel,
			@QueryParam("role") String role
			) {
		try (ConnectionManager mngr = new ConnectionManager()) {
			ADBEntityType et = adb.getEntityType(type);
			ADBEntity entity = et.getEntity(mngr, id);
			JSONArray json;
			if (rel != null && !rel.isEmpty() && role != null && !role.isEmpty()) {
				ADBRelationType rt = adb.getRelationType(rel);
				json = JSONConverter.expansionChildren(mngr, entity, rt, role, true);
			}
			else {
				json = JSONConverter.expansionChildren(mngr, entity, null, null, false);
			}
			return json.toString(4);
		}
		catch (ADBException|JSONException|IOException e) {
			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("treemap/{rel}/{role}/{id}")
	public String taxonLocalization(
			@PathParam("rel") String rel,
			@PathParam("role") String role,
			@PathParam("id") String id,
			@QueryParam("oth") String otherId,
			@DefaultValue("count") @QueryParam("fun") String fun,
			@DefaultValue("1") @QueryParam("depth") int depth
			) {
		try (ConnectionManager mngr = new ConnectionManager()) {
			ADBRelationType relationType = adb.getRelationType(rel);
			
			ADBArgument pivotArg = relationType.getArgument(role);
			ADBEntityType pivotType = pivotArg.getType();
			ADBEntity pivot = pivotType.getEntity(mngr, id);
			
			ADBArgument otherArg = relationType.getOther(role);
			ADBEntityType otherType = otherArg.getType();
			ADBEntity other;
			if (otherId == null || otherId.isEmpty()) {
				other = otherType.getRoot(mngr);
			}
			else {
				other = otherType.getEntity(mngr, otherId);
			}
			JSONConverter.RelationQuery query = new JSONConverter.RelationQuery(relationType, pivot, role, true, true);
			JSONObject json = JSONConverter.treeMap(mngr, query, other, fun, depth);
			return json.toString(4);
		}
		catch (IOException|ADBException|JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("table/{rel}/{role}/{id}")
	public String taxonLocalization(
			@PathParam("rel") String rel,
			@PathParam("role") String role,
			@PathParam("id") String id,
			@QueryParam("oth") String otherId,
			@DefaultValue("0") @QueryParam("pagenum") int page,
			@DefaultValue("10") @QueryParam("pagesize") int pageSize,
			@DefaultValue("title") @QueryParam("title") String title,
			@QueryParam("sortdatafield") String sort,
			@QueryParam("sortorder") String sortOrder
			) {
		try (ConnectionManager mngr = new ConnectionManager()) {
			ADBRelationType relationType = adb.getRelationType(rel);
			
			ADBArgument pivotArg = relationType.getArgument(role);
			ADBEntityType pivotType = pivotArg.getType();
			ADBEntity pivot = pivotType.getEntity(mngr, id);
			
			ADBArgument otherArg = relationType.getOther(role);
			ADBEntityType otherType = otherArg.getType();
			ADBEntity other;
			if (otherId == null || otherId.isEmpty()) {
				other = otherType.getRoot(mngr);
			}
			else {
				other = otherType.getEntity(mngr, otherId);
			}
			JSONConverter.RelationQuery query = new JSONConverter.RelationQuery(relationType, pivot, role, true, true);
			JSONObject json = JSONConverter.table(mngr, query, other, page, pageSize, title, sort, "desc".equals(sortOrder));
			return json.toString(4);
		}
		catch (IOException|ADBException|JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces("text/csv")
	@Path("export/{rel}/{role}/{id}")
	public Response taxonLocalization(
			@PathParam("rel") String rel,
			@PathParam("role") String role,
			@PathParam("id") String id,
			@QueryParam("oth") String otherId,
			@DefaultValue("title") @QueryParam("title") String title,
			@DefaultValue("false") @QueryParam("othpath") Boolean othpath
			) {
		try (ConnectionManager mngr = new ConnectionManager()) {
			ADBRelationType relationType = adb.getRelationType(rel);
			
			ADBArgument pivotArg = relationType.getArgument(role);
			ADBEntityType pivotType = pivotArg.getType();
			ADBEntity pivot = pivotType.getEntity(mngr, id);
			
			ADBArgument otherArg = relationType.getOther(role);
			ADBEntityType otherType = otherArg.getType();
			ADBEntity other;
			if (otherId == null || otherId.isEmpty()) {
				other = otherType.getRoot(mngr);
			}
			else {
				other = otherType.getEntity(mngr, otherId);
			}
			StringBuilder result = new StringBuilder();
			result.append("PMID\tTitle\tURL\tTaxon ID\tTaxon Name\tTaxon Form\tHabitat ID\tHabitat Name\tHabitat Form");
			if (othpath) {
				result.append("\tHabitat Path\n");
			}
			else {
				result.append('\n');
			}
			ADBRelation[] relations = relationType.listRelations(mngr, role, pivot, true, other, true, 0, Integer.MAX_VALUE, null, false);
			for (ADBRelation arel : relations) {
				try {
					ADBEntityOccurrence taxonOcc = arel.getLeft();
					ADBEntityOccurrence habitatOcc = arel.getRight();
					String docId = taxonOcc.getDocumentId();
					ADBDocument doc = adb.getDocument(mngr, docId);
					String docTitle = doc.getSection(title);
					ADBEntity taxonArg = taxonOcc.getEntity(mngr);
					ADBEntity habitatArg = habitatOcc.getEntity(mngr);
					result.append(docId);
					result.append('\t');
					result.append(docTitle);
					result.append('\t');
					result.append("http://www.ncbi.nlm.nih.gov/pubmed/");
					result.append(docId);
					result.append('\t');
					result.append(taxonArg.getId());
					result.append('\t');
					result.append(taxonArg.getName());
					result.append('\t');
					result.append(taxonOcc.getForm(mngr, adb));
					result.append('\t');
					result.append(habitatArg.getId());
					result.append('\t');
					result.append(habitatArg.getName());
					result.append('\t');
					result.append(habitatOcc.getForm(mngr, adb));
					if (othpath) {
						ADBEntity[] path = habitatArg.getPath(mngr);
						for (ADBEntity anc : path) {
							result.append('\t');
							result.append(anc.getId());
							result.append(" (");
							result.append(anc.getName());
							result.append(')');
						}
					}
				}
				catch (RuntimeException e) {
					// XXX bug workaround
					e.printStackTrace();
				}
				result.append('\n');
			}
			return Response.ok(result.toString()).header("Content-Disposition", "attachment; filename="+pivot.getName().replace(' ', '_')+"__"+other.getName().replace(' ', '_')+".csv").build();
		}
		catch (IOException|ADBException e) {
			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces("text/javascript")
	@Path("lib/{jssource}")
	public InputStream jslib(@PathParam("jssource") String jssource) {
		Class<?> klass = getClass();
		return klass.getResourceAsStream(jssource);
	}

	@GET
	@Produces("text/css")
	@Path("css/{css}")
	public InputStream css(@PathParam("css") String css) {
		Class<?> klass = getClass();
		return klass.getResourceAsStream(css);
	}

	@GET
	@Produces("image/*")
	@Path("css/images/{image}")
	public InputStream image(@PathParam("image") String image) {
		Class<?> klass = getClass();
		return klass.getResourceAsStream(image);
	}
}
