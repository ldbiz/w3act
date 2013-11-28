package uk.bl.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import models.DCollection;
import models.Organisation;
import models.Target;
import models.Taxonomy;
import models.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.*;
import com.ning.http.client.Body;

import play.libs.Json;
import uk.bl.Const;
import uk.bl.Const.NodeType;
import uk.bl.Const.TaxonomyType;
import models.*;
import play.Logger;

/**
 * JSON object management.
 */
public class JsonUtils {

	/**
	 * This method extracts page number from the JSON in order to evaluate first and last page numbers.
	 * @param node
	 * @param field
	 * @return page number as int
	 */
	private static int getPageNumber(JsonNode node, String field) {
		String page = getStringItem(node, field);
		Logger.info("page url: " + page);
		int idxPage = page.indexOf(Const.PAGE_IN_URL) + Const.PAGE_IN_URL.length();
		return Integer.parseInt(page.substring(idxPage));
	}
	
	/**
	 * This method downloads remote data using HTTP request and retrieves contentfor passed type.
	 * @param urlStr
	 * @param type
	 * @return list of objects
	 */
	private static String downloadData(String urlStr, NodeType type) {
	    String res = "";
	    if (urlStr != null && urlStr.length() > 0) {
			// aggregate data from drupal and store JSON content in a file
			HttpBasicAuth.downloadFileWithAuth(urlStr, Const.AUTH_USER, Const.AUTH_PASSWORD, type.toString().toLowerCase() + Const.OUT_FILE_PATH);
			// read file and store content in String
			res = JsonUtils.readJsonFromFile(type.toString().toLowerCase() + Const.OUT_FILE_PATH);
	    }
        return res;
	}

	/**
     * This method retrieves JSON data from Drupal for particular domain object type (e.g. Target, Collection...)
     * @param type
     * @return a list of retrieved objects
     */
    public static List<Object> getDrupalData(NodeType type) {
	    List<Object> res = new ArrayList<Object>();
	    try {
		    String urlStr = Const.URL_STR + type.toString().toLowerCase();
			// aggregate data from drupal and store JSON content in a file
			HttpBasicAuth.downloadFileWithAuth(urlStr, Const.AUTH_USER, Const.AUTH_PASSWORD, type.toString().toLowerCase() + Const.OUT_FILE_PATH);
			// read file and store content in String
			String content = JsonUtils.readJsonFromFile(type.toString().toLowerCase() + Const.OUT_FILE_PATH);
			// extract page information
			JsonNode mainNode = Json.parse(content);	
			if(mainNode != null) {
				int firstPage = getPageNumber(mainNode, Const.FIRST_PAGE);
				int lastPage = getPageNumber(mainNode, Const.LAST_PAGE);
				Logger.info("pages: " + firstPage + ", " + lastPage);
				// aggregate data from drupal for all pages 
				for (int i = firstPage; i <= lastPage; i++) {
//					if (i == 1) {
//						break; // if necessary for faster testing take only the first page
//					}
					String pageContent = downloadData(urlStr + "&" + Const.PAGE_IN_URL + String.valueOf(i), type);
					List<Object> pageList = JsonUtils.parseJson(pageContent, type);
					res.addAll(pageList);
				}
			}
    	} catch (Exception e) {
			Logger.info("data aggregation error: " + e);
		}
    	Logger.info("list size: " + res.size());
//    	int idx = 0;
//		Iterator<Object> itr = res.iterator();
//		while (itr.hasNext()) {
//			Object obj = itr.next();
//			Logger.info("res getDrupalData: " + obj.toString() + ", idx: " + idx);
//			idx++;
//		}
    	int idx = 0;
		Iterator<Object> itr = res.iterator();
		while (itr.hasNext()) {
			if (type.equals(NodeType.URL)) {
				Target obj = (Target) itr.next();
				if (obj.vid > 0) {
					obj.edit_url = Const.WCT_URL + obj.vid;
				}
			}
			if (type.equals(NodeType.ORGANISATION)) {
				Organisation obj = (Organisation) itr.next();
				if (obj.vid > 0) {
					obj.edit_url = Const.WCT_URL + obj.vid;
				}
			}
		}
		return res;
    }
    
	/**
     * This method retrieves collections. Due to merging of different original object models the resulting 
	 * collection set is evaluated from particular taxonomy type.
     * @return a list of retrieved collections
     */
    public static List<Object> readCollectionsFromTaxonomies() {
	    List<Object> res = new ArrayList<Object>();
		List<Taxonomy> taxonomyList = Taxonomy.findListByType(TaxonomyType.COLLECTION.toString().toLowerCase());
		Iterator<Taxonomy> itr = taxonomyList.iterator();
		while (itr.hasNext()) {
			Taxonomy taxonomy = itr.next();
			if (taxonomy.name != null && taxonomy.name.length() > 0) {
				DCollection collection = new DCollection();
				collection.nid         = taxonomy.tid;
				collection.author      = taxonomy.field_owner;
				collection.summary     = taxonomy.description;
				collection.title       = taxonomy.name;
				collection.feed_nid    = taxonomy.feed_nid;
				collection.url         = taxonomy.url;
			    collection.weight      = taxonomy.weight;
			    collection.node_count  = taxonomy.node_count;
			    collection.vocabulary  = taxonomy.vocabulary;
			    collection.parent      = taxonomy.parent;
			    collection.parents_all = taxonomy.parents_all;
				res.add(collection);
			}
		}
		return res;
    }
    
    /**
     * This method aggregates object list from JSON data for particular domain object type.
     * @param urlStr
     * @param type
     * @param taxonomy_type The type of taxonomy
     * @param res
     */
    private static void aggregateObjectList(String urlStr, NodeType type, TaxonomyType taxonomy_type, List<Object> res) {
	    Logger.info("extract data for: " + urlStr + " type: " + type);
		String content = downloadData(urlStr, type);
		JsonNode mainNode = Json.parse(content);	
		if(mainNode != null) {
			List<Object> pageList = JsonUtils.parseJsonExt(content, type, taxonomy_type);
			res.addAll(pageList);
		}
    }

    /**
     * This method executes JSON URL request for particular object.
     * @param url The current URL
     * @param urlList The list of aggregated URLs (to avoid duplicates)
     * @param type The object type
     * @param taxonomy_type The type of taxonomy
     * @param res Resulting list
     */
    public static void executeUrlRequest(String url, List<String> urlList, NodeType type, TaxonomyType taxonomy_type, List<Object> res) {
    	url = getWebarchiveCreatorUrl(url, type);
		String urlStr = url + Const.JSON;
		if (!urlList.contains(urlStr)) {
    		urlList.add(urlStr);
			aggregateObjectList(urlStr, type, taxonomy_type, res);
		}
    }
    
    /**
     * This method prepares URLs for JSON URL request.
     * @param fieldName Contains one or many URLs separated by comma
     * @param urlList The list of aggregated URLs (to avoid duplicates)
     * @param type The object type
     * @param taxonomy_type The type of taxonomy
     * @param res Resulting list
     */
    public static void readListFromString(String fieldName, List<String> urlList, NodeType type, TaxonomyType taxonomy_type, List<Object> res) {
//		Logger.info("fieldName: " + fieldName);
    	if (fieldName != null && fieldName.length() > 0) {
    		if (fieldName.contains(Const.COMMA)) {
    			List<String> resList = Arrays.asList(fieldName.split(Const.COMMA));
    			Iterator<String> itr = resList.iterator();
    			while (itr.hasNext()) {
        			executeUrlRequest(itr.next(), urlList, type, taxonomy_type, res);
    			}
    		} else {
    			executeUrlRequest(fieldName, urlList, type, taxonomy_type, res);
    		}
    	}
    }
    
    /**
     * This method retrieves secondary JSON data from Drupal for 
     * particular domain object type (e.g. User, Taxonomy ...).
     * The URL to the secondary JSON data is included in previosly 
     * aggregated main domain objects (e.g. link to User is contained in Target).
     * @param type
     * @return a list of retrieved objects
     */
    public static List<Object> extractDrupalData(NodeType type) {
	    List<Object> res = new ArrayList<Object>();
	    try {
	    	List<String> urlList = new ArrayList<String>();
	    	List<Target> targets = Target.findAll();
	    	Iterator<Target> itr = targets.iterator();
	    	while (itr.hasNext()) {
	    		Target target = itr.next();
			    String urlStr = "";
			    if (type.equals(NodeType.USER)) {
			    	readListFromString(target.author, urlList, type, null, res);
			    }
			    if (type.equals(NodeType.TAXONOMY)) {
			    	readListFromString(target.field_collection_categories, urlList, type, TaxonomyType.COLLECTION, res);
			    	readListFromString(target.field_suggested_collections, urlList, type, TaxonomyType.COLLECTION, res);
			    	readListFromString(target.field_license, urlList, type, TaxonomyType.LICENSE, res);
			    	readListFromString(target.field_subject, urlList, type, TaxonomyType.SUBJECT, res);
			    }
			    if (type.equals(NodeType.TAXONOMY_VOCABULARY)) {
			    	List<Taxonomy> taxonomies = Taxonomy.findAll();
			    	Iterator<Taxonomy> taxonomyItr = taxonomies.iterator();
			    	while (taxonomyItr.hasNext()) {
			    		Taxonomy taxonomy = (Taxonomy) taxonomyItr.next();
				    	readListFromString(taxonomy.vocabulary, urlList, type, null, res);
			    	}
			    }
	    	}
    	} catch (Exception e) {
			Logger.info("data aggregation error: " + e);
		}
    	Logger.info("list size: " + res.size());
    	int idx = 0;
		Iterator<Object> itr = res.iterator();
		while (itr.hasNext()) {
			Object obj = itr.next();
			Logger.info("res getDrupalData: " + obj.toString() + ", idx: " + idx);
			idx++;
		}
		return res;
    }
    
    /**
     * This method extracts multiple items for JSON path
     * @param node
     * @param path
     * @return list of String items
     */
    public static List<String> getStringItems(JsonNode node, String path) {
		List<String> res = new ArrayList<String>();
		JsonNode resNode = getElement(node, path);
//		Logger.info("getStringItems path: " + path + ", resNode: " + resNode);
		if (resNode != null) {
			Iterator<JsonNode> it = resNode.iterator();
			while (it.hasNext()) {
				String fieldName = "";
				JsonNode subNode = it.next();	
				if (subNode.has(Const.URI)) {
					fieldName = Const.URI;
				}
				if (subNode.has(Const.URL)) {
					fieldName = Const.URL;
				}
				String item = subNode.findPath(fieldName).textValue();
				if(item != null) {
					res.add(item);
				}
//				Logger.info("subNode: " + subNode + ", path: " + path + ", fieldName: " + fieldName + ", item: " + item + ", res: " + res.size());
			}
		}
//		Logger.info("getStringItems res: " + res);
		return res;
    }

    /**
     * This method returns list objects from JSON node as a String
     * @param resNode
     * @param path
     * @return list as a String
     */
    public static String getStringList(JsonNode resNode, String path) {
		String res = "";
//		Logger.info("getStringList path: " + path + ", resNode: " + resNode);
		if (resNode != null) {
			Iterator<JsonNode> it = resNode.iterator();
			while (it.hasNext()) {
				String fieldName = "";
				JsonNode subNode = it.next();	
//				Logger.info("subNode: " + subNode);
				if (subNode.has(Const.URI)) {
					fieldName = Const.URI;
				}
				if (subNode.has(Const.URL)) {
					fieldName = Const.URL;
				}
				String item = subNode.findPath(fieldName).textValue();
				if(item != null) {
					if (res.length() > 0) {
						res = res + "," + item;
					} else {
						res = item;
					}
				}
//				Logger.info("list subNode: " + subNode + ", path: " + path + ", fieldName: " + fieldName + ", item: " + item + ", res: " + res);
			}
		}
//		Logger.info("getStringList res: " + res);
		return res;
    }

    /**
     * This method checks if given URL is a webarchive url from original Drupal ACT.
     * If yes it is converted to the new url using NID at the end of the url e.g. "act-<NID>"
     * If it is an edit URL (ends with "/edit") it is converted to the WCT URL e.g. "wct-<VID>"
     * @param url
     * @return identifier URL
     */
    public static String checkArchiveUrl(String url) {
//    	Logger.info("checkArchiveUrl() url: " + url);
    	String res = url;
		if(url != null) {
			if(url.contains(Const.WEBARCHIVE_LINK)) {
				if(url.contains(Const.EDIT_LINK)) {
					String root = url.replace(Const.EDIT_LINK, "");
					res = Const.WCT_URL + root.substring(root.lastIndexOf("/") + 1);
				} else {
					res = Const.ACT_URL + url.substring(url.lastIndexOf("/") + 1);
				}
			}
		}
//		if (!url.equals(res)) {
//			Logger.info("checkArchiveUrl() res: " + res);
//		}
		return res;
    }
        
    /**
     * This method converts the given W3ACT user URL in a webarchive URL using NID at the end of the url e.g. "act-<NID>"
     * @param url The W3ACT identifier
     * @return the Webarchive identifier URL
     * @param type The node type
     * @return
     */
    public static String getWebarchiveCreatorUrl(String url, NodeType type) {
    	String res = url;
		if(url != null) {
			if(url.contains(Const.ACT_URL)) {
				String entryType = "user";
				if (type.equals(NodeType.TAXONOMY)) {
					entryType = "taxonomy_term";
				}
				if (type.equals(NodeType.TAXONOMY_VOCABULARY)) {
					entryType = "taxonomy_vocabulary";
				}
				res = "http://" + Const.WEBARCHIVE_LINK + "/act/" + entryType + "/" + 
						url.substring(url.lastIndexOf(Const.ACT_URL) + Const.ACT_URL.length());
			}
		}
		return res;
    }
        
    /**
     * This method returns object from JSON sub node as a String
     * @param resNode
     * @param path
     * @return list as a String
     */
    public static String getStringFromSubNode(JsonNode resNode, String path) {
		String res = "";
//		Logger.info("getStringList path: " + path + ", resNode: " + resNode);
		if (resNode != null) {
			String item = resNode.findPath(path).textValue();
			res = normalizeArchiveUrl(item);
		}
//		Logger.info("getStringFromSubNode res: " + res);
		return res;
    }

    /**
     * This method extracts one item for JSON field
     * @param node
     * @param field
     * @return String item
     */
    public static String getStringItem(JsonNode node, String fieldName) {
		String res = "";
		JsonNode resNode = getElement(node, fieldName);
		if (resNode != null) {
			res = resNode.textValue();
		}
//		Logger.info("getStringItem field name: " + fieldName + ", res: " + res);
		return res;
    }
    
    /**
     * This method evaluates element from the root node associated with passed field name.
     * @param node
     * @param fieldName
     * @return sub node
     */
    public static JsonNode getElement(JsonNode node, String fieldName) {
		JsonNode res = null;
		Iterator<Map.Entry<String, JsonNode>> elt = node.fields();
		while (elt.hasNext()) {
			Map.Entry<String, JsonNode> element = elt.next(); 
			if (element.getKey().equals(fieldName)) {
				res = element.getValue();
				break;
			}
		}
		return res;
    }
    
	/**
	 * This method extracts JSON nodes and passes them to parser
	 * @param content
	 * @param type
	 * @return object list for particular domain object type
	 */
	public static List<Object> parseJson(String content, NodeType type) {
	    List<Object> res = new ArrayList<Object>();
		JsonNode json = Json.parse(content);
		if(json != null) {
			JsonNode rootNode = json.path(Const.LIST_NODE);
			Iterator<JsonNode> ite = rootNode.iterator();
			Logger.info("rootNode elements count is: " + rootNode.size());

			while (ite.hasNext()) {
				JsonNode node = ite.next();
				Object obj = null;
				if (type.equals(Const.NodeType.URL)) {					
					obj = new Target();
				} 
				if (type.equals(Const.NodeType.COLLECTION)) {
					obj = new DCollection();
				}
				if (type.equals(Const.NodeType.ORGANISATION)) {
					obj = new Organisation();
				}
				parseJsonNode(node, obj);
				res.add(obj);
			}
		} else {
			Logger.info("json is null");
		}			  
		return res;
	}
	
	/**
	 * This method extracts JSON node without root node and passes them to parser
	 * @param content
	 * @param type
     * @param taxonomy_type The type of taxonomy
     * @return object list for particular domain object type
	 */
	public static List<Object> parseJsonExt(String content, NodeType type, TaxonomyType taxonomy_type) {
	    List<Object> res = new ArrayList<Object>();
		JsonNode node = Json.parse(content);
		if(node != null) {
			Object obj = null;
			if (type.equals(Const.NodeType.USER)) {					
				obj = new User();
			} 
			if (type.equals(Const.NodeType.TAXONOMY)) {
				obj = new Taxonomy();
			}
			if (type.equals(Const.NodeType.TAXONOMY_VOCABULARY)) {
				obj = new TaxonomyVocabulary();
			}
			parseJsonNode(node, obj);
			if (type.equals(Const.NodeType.TAXONOMY)) {
				((Taxonomy) obj).type = taxonomy_type.toString().toLowerCase();
//				Logger.info("taxonomy type: " + taxonomy_type.toString().toLowerCase());
			}
			boolean isNew = true;
			if (type.equals(Const.NodeType.USER)) {	
			    User newUser = (User) obj;
			    if (newUser.email == null || newUser.email.length() == 0) {
			    	newUser.email = newUser.name.toLowerCase() + "@bl.uk";
			    }
			    User existingUser = User.findByName(newUser.name);
			    if (existingUser != null && existingUser.name.length() > 0) {
			    	isNew = false;
			    	existingUser.field_affiliation = newUser.field_affiliation;
			    	existingUser.uid = newUser.uid;
			    	existingUser.url = newUser.url;
			    	existingUser.edit_url = newUser.edit_url;
			    	existingUser.last_access = newUser.last_access;
			    	existingUser.last_login = newUser.last_login;
			    	existingUser.created = newUser.created;
			    	existingUser.status = newUser.status;
			    	existingUser.language = newUser.language;
			    	existingUser.feed_nid = newUser.feed_nid;
			    	existingUser.update();
			    }
			}
			if (isNew) {
				res.add(obj);
			}
		} else {
			Logger.info("json is null");
		}			  
		return res;
	}
	
	/**
	 * This method parses String value from JSON and converts it in associated field type of the object.
	 * @param f
	 * @param node
	 * @param obj
	 */
	public static void parseJsonString(Field f, JsonNode node, Object obj) {
		try {
			String jsonField = getStringItem(node, f.getName());
			jsonField = normalizeArchiveUrl(jsonField);
			if (f.getType().equals(String.class)) {
				if (jsonField == null || jsonField.length() == 0) {
					jsonField = "";
				}
				f.set(obj, jsonField);
			}
			if (f.getType().equals(Long.class)) {
				if (jsonField == null || jsonField.length() == 0) {
					jsonField = "0";
				}
				Long jsonFieldLong = new Long(Long.parseLong(jsonField, 10));
				f.set(obj, jsonFieldLong);
			}
			if (f.getType().equals(Boolean.class)) {
				if (jsonField == null || jsonField.length() == 0) {
					jsonField = "false";
				}
				if (jsonField.equals("Yes") 
						|| jsonField.equals("yes") 
						|| jsonField.equals("True") 
						|| jsonField.equals("Y") 
						|| jsonField.equals("y")) {
					jsonField = "true";
				}
				Boolean jsonFieldBoolean = new Boolean(Boolean.parseBoolean(jsonField));
				f.set(obj, jsonFieldBoolean);
			}
		} catch (IllegalArgumentException e) {
			Logger.info("parseJsonString IllegalArgument error: " + e + ", f: " + f); 
		} catch (IllegalAccessException e) {
			Logger.info("parseJsonString IllegalAccess error: " + e + ", f: " + f); 
		} catch (Exception e) {
			Logger.info("parseJsonString error: " + e + ", f: " + f); 
		}
	}
	
	/**
	 * This method checks if node is a sub node and processes it if that assumption is true
	 * @param f
	 * @param node
	 * @param obj
	 * @return check result
	 */
	private static boolean checkSubNode(Field f, JsonNode node, Object obj) {
		boolean res = false;
	    if (Const.subNodeMap.containsKey(f.getName())) {
			res = true;
			JsonNode resNode = getElement(node, f.getName());
			String jsonField = getStringFromSubNode(resNode, Const.subNodeMap.get(f.getName()));
//			Logger.info("resNode: " + resNode + ", jsonField: " + jsonField);
			if (f.getType().equals(String.class)) {
				if (jsonField == null || jsonField.length() == 0) {
					jsonField = "";
				}
				try {
					f.set(obj, jsonField);
				} catch (Exception e) {
					Logger.info("checkSubNode: " + e); 
				}
			}
		}
        return res;
	}

	/**
	 * This method converts archiving URLs into W3ACT URLs.
	 * @param str A single URL or multiple URLs separated by comma
	 * @return W3ACT URLs as a string
	 */
	public static String normalizeArchiveUrl(String str) {
		String res = "";
    	if (str != null && str.length() > 0) {
    		if (str.contains(Const.COMMA)) {
    			List<String> resList = Arrays.asList(str.split(Const.COMMA));
    			int idx = 0;
    			Iterator<String> itr = resList.iterator();
    			while (itr.hasNext()) {
        			String urlItem = checkArchiveUrl(itr.next());
        			if (idx == 0) {
        			   res = urlItem;
        			} else {
        			   res = res + Const.COMMA + " " + urlItem;
        			}
        			idx++;
    			}
    		} else {
    			res = checkArchiveUrl(str);
    		}
    	}
    	return res;
	}
	
	/**
	 * This method parses JSON node and extracts fields
	 * @param node
	 * @param obj
	 */
	public static void parseJsonNode(JsonNode node, Object obj) {
		Field[] fields = obj.getClass().getFields();
//		if (obj.getClass().toString().contains("Taxonomy")) {
//			Logger.info("Taxonomy node: " + node.toString());
//		}
		for (Field f : fields) {
			try {
			    if (Const.targetMap.containsKey(f.getName()) || Const.collectionMap.containsKey(f.getName())) {
					JsonNode resNode = getElement(node, f.getName());
					String jsonField = getStringList(resNode, f.getName());
					if (!f.getName().equals(Const.targetMap.get("field_url"))) {
						jsonField = normalizeArchiveUrl(jsonField);
					}
//						Logger.info("resNode: " + resNode + ", jsonField: " + jsonField);
					if (f.getType().equals(String.class)) {
						if (jsonField == null || jsonField.length() == 0) {
							jsonField = "";
						}
						f.set(obj, jsonField);
					}
			    } else {
					if (f.getName().equals(Const.VALUE) // body elements
							|| f.getName().equals(Const.SUMMARY) 
							|| f.getName().equals(Const.FORMAT)) {
						JsonNode resNode = getElement(node, Const.BODY);
						parseJsonString(f, resNode, obj);
					} else {
						if (!checkSubNode(f, node, obj)) {
							parseJsonString(f, node, obj);
						}
					}
				}
			} catch (IllegalArgumentException e) {
				Logger.info("parseJsonNode IllegalArgument error: " + e + ", f: " + f); 
			} catch (IllegalAccessException e) {
				Logger.info("parseJsonNode IllegalAccess error: " + e + ", f: " + f); 
			} catch (Exception e) {
				Logger.info("parseJsonNode error: " + e + ", f: " + f); 
			}
		}
	}

	/**
	 * This method reads JSON content from a file for given path.
	 * @param outFilePath
	 * @return JSON as a String
	 */
	public static String readJsonFromFile(String outFilePath) {
	    String res = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(outFilePath));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line);
					sb.append('\n');
					line = br.readLine();
				}
				res = sb.toString();
			} finally {
				br.close();
			}
		} catch (FileNotFoundException e) {
			Logger.info("JSON content file not found: " + e.getMessage());
		} catch (IOException e) {
			Logger.info("document path error: " + e.getMessage());
    	} catch (Exception e) {
			Logger.info("error: " + e);
		}
		return res;
	}

}

