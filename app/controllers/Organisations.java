package controllers;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.Organisation;
import models.Target;
import models.User;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import uk.bl.Const;
import uk.bl.api.Utils;
import views.html.organisations.list;
import views.html.organisations.organisationadmin;
import views.html.organisations.organisationedit;
import views.html.organisations.organisationsites;
import views.html.organisations.organisationview;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Manage organisations.
 */
@Security.Authenticated(Secured.class)
public class Organisations extends AbstractController {
  
    /**
     * Display the organisations.
     */

    public static Result index() {
    	Logger.info("DCollections.index()");
        return GO_HOME;
    }
    
    public static Result GO_HOME = redirect(
            routes.Organisations.list(0, "title", "asc", "")
        );

    /**
     * Searching
     */
    public static Result search() {
    	DynamicForm form = form().bindFromRequest();
    	String action = form.get("action");
    	String query = form.get(Const.QUERY);
		Logger.info("query: " + query);
		Logger.info("action: " + action);
    	
    	if (StringUtils.isBlank(query)) {
			Logger.info("Organisation name is empty. Please write name in search window.");
			flash("message", "Please enter a name in the search window");
	        return redirect(
	        		routes.Organisations.list(0, "title", "asc", "")
	        );
    	}

    	int pageNo = getQueryParamAsInt(Const.PAGE_NO, 0);
    	String sort = getQueryParam(Const.SORT_BY);
    	String order = getQueryParam(Const.ORDER);

    	if (StringUtils.isEmpty(action)) {
    		return badRequest("You must provide a valid action");
    	} else {
    		if (Const.ADDENTRY.equals(action)) {
        		return redirect(routes.Organisations.create(query));
    		} 
    		else if (Const.SEARCH.equals(action)) {
    	    	return redirect(routes.Organisations.list(pageNo, sort, order, query));
		    } else {
		      return badRequest("This action is not allowed");
		    }
    	}
    }

    /**
     * Add an organisation.
     */
    public static Result create(String title) {
//        Form<Organisation> organisationForm = form(Organisation.class);
//        return ok(
//            organisationedit.render(User.find.byId(request().username(), organisationForm))
//        );
    	
    	Organisation organisation = new Organisation();
    	organisation.title = title;
        organisation.nid = Target.createId();
        organisation.url = Const.ACT_URL + organisation.nid;
		Logger.info("add entry with url: " + organisation.url + ", and title: " + organisation.title);
        return ok(
                organisationedit.render(
                      organisation, User.find.byId(request().username())
                )
            );
    }
    
    /**
     * Display the organisation edit panel for this URL.
     */
    public static Result edit(String url) {
		Logger.info("organisation url: " + url);
		Organisation organisation = Organisation.findByUrl(url);
		Logger.info("organisation title: " + organisation.title + ", url: " + url);
        return ok(
                organisationedit.render(
                        Organisation.findByUrl(url), User.find.byId(request().username())
                )
            );
    }
    
    /**
     * filter for typeahead lookup
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result filterByJson(String name) {
        JsonNode jsonData = null;
        if (name != null) {
	        List<Organisation> organisations = Organisation.filterByName(name);
	        jsonData = Json.toJson(organisations);
        }
        return ok(jsonData);
    }

    /**
     * Display the paginated list of Organisations.
     *
     * @param page Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order Sort order (either asc or desc)
     * @param filter Filter applied on target urls
     */
    public static Result list(int pageNo, String sortBy, String order, String query) {
    	Logger.info("Organisations.list() " + query);
        return ok(
        	list.render(
        			"Organisations", 
        			User.find.byId(request().username()), 
        			query, 
        			Organisation.page(pageNo, 10, sortBy, order, query), 
        			sortBy, 
        			order)
        	);
    }

    /**
     * Get View based on url
     */
    public static Result view(String url) {
        return ok(
                organisationview.render(
                        Organisation.findByUrl(url), User.find.byId(request().username())
                )
            );
    }    
    
    public static Result sites(String url) {
        return ok(
                organisationsites.render(
                        Organisation.findByUrl(url), User.find.byId(request().username())
                )
            );
    }
    
    /**
     * Administer users
     * @param url
     * @return
     */
    public static Result admin(String url) {
        return ok(
                organisationadmin.render(
                        Organisation.findByUrl(url), User.find.byId(request().username())
                )
            );
    }

    /**
     * Rename a organisation.
     */
    public static Result rename(Long organisation) {
        return ok(
            Organisation.rename(
                organisation, 
                form().bindFromRequest().get("title")
            )
        );
    }
    
    /**
     * Delete a organisation.
     */
    public static Result delete(Long organisation) {
        Organisation.find.ref(organisation).delete();
        return ok();
    }

    /**
     * This method saves new object or changes on given Organisation in the same object
     * completed by revision comment. The "version" field in the Organisation object
     * contains the timestamp of the change. 
     * @return
     */
    public static Result save() {
    	Result res = null;
        String save = getFormParam(Const.SAVE);
        String delete = getFormParam(Const.DELETE);
//        Logger.info("save: " + save);
        if (save != null) {
        	Logger.info("save organisation nid: " + getFormParam(Const.NID) + ", url: " + getFormParam(Const.URL) + 
        			", title: " + getFormParam(Const.TITLE) + ", revision: " + getFormParam(Const.REVISION) + 
        			", abbreviation: " + getFormParam(Const.FIELD_ABBREVIATION));
        	Organisation organisation = null;
            boolean isExisting = true;
            try {
                try {
                	if (Organisation.existsByUrl(getFormParam(Const.URL))) {
                		organisation = Organisation.findByUrl(getFormParam(Const.URL));
                	}
                } catch (Exception e) {
                	Logger.info("is not existing exception");
                	isExisting = false;
                	organisation = new Organisation();
                	organisation.nid = Long.valueOf(getFormParam(Const.NID));
                	organisation.url = getFormParam(Const.URL);
                }
                if (organisation == null) {
                	Logger.info("is not existing");
                	isExisting = false;
                	organisation = new Organisation();
                	organisation.nid = Long.valueOf(getFormParam(Const.NID));
                	organisation.url = getFormParam(Const.URL);
                }
                
        	    if (getFormParam(Const.TITLE) != null) {
        	    	organisation.title = getFormParam(Const.TITLE);
        	    }
        	    if (getFormParam(Const.FIELD_ABBREVIATION) != null) {
        	    	organisation.field_abbreviation = getFormParam(Const.FIELD_ABBREVIATION);
        	    }
        	    if (getFormParam(Const.SUMMARY) != null) {
        	    	organisation.summary = getFormParam(Const.SUMMARY);
        	    }
        	    if (organisation.revision == null) {
        	    	organisation.revision = "";
        	    }
                if (getFormParam(Const.REVISION) != null) {
                	organisation.revision = organisation.revision.concat(", " + getFormParam(Const.REVISION));
                }
            } catch (Exception e) {
            	Logger.info("User not existing exception");
            }
            
        	if (!isExisting) {
            	boolean inDb = isInDb(organisation);
                if (!inDb) {
	               	Ebean.save(organisation);
	    	        Logger.info("save organisation: " + organisation.toString());
                } else {
	    	        Logger.info("Organisation title already exists in database: " + organisation.toString());
        	        return redirect(routes.Organisations.index()); 
                }
        	} else {
           		Logger.info("update organisation: " + organisation.toString());
               	Ebean.update(organisation);
        	}
	        res = redirect(routes.Organisations.index());
//	        res = redirect(routes.Organisations.view(organisation.url));
        } 
        if (delete != null) {
        	Organisation organisation = Organisation.findByUrl(getFormParam(Const.URL));
        	Ebean.delete(organisation);
	        res = redirect(routes.Organisations.index()); 
        }
        return res;
    }
	
    /**
     * This method checks if organisation object with given title already
     * exists in database.
     * @param newOrganisation
     * @return true if exists
     */
    public static boolean isInDb(Organisation newOrganisation) {
    	boolean res = true;
    	if (newOrganisation.title != null && newOrganisation.title.length() > 0) {
    		/**
    		 * Check if this organisation title already exists in database
    		 */
    		res = Organisation.existsByTitle(newOrganisation.title);
    	}
		Logger.info("isInDb() res: " + res);
        return res;
    }
    
    /**
     * This method ensures that no multiple organisations with the same name can be stored in 
     * database during the data import from the Drupal.
     * @return
     */
    public static List<Object> skipExistingObjects(List<Object> newOrganisations) {
    	List<Object> res = new ArrayList<Object>();
    	
    	/**
    	 * Iterate over all new organisations that supposed to be stored in database
    	 */
    	Iterator<Object> newItr = newOrganisations.iterator();
        while (newItr.hasNext()) {
        	Organisation newOrganisation = (Organisation) newItr.next();
        	boolean inDb = isInDb(newOrganisation);
       		if (!inDb) {
    			res.add(newOrganisation);
    		}
        }
        return res;
    }	   
    
    /**
     * This method checks if given user belongs to given organisation. This objects 
     * are linked by organisation URL.
     * @param user
     * @param organisation
     * @return
     */
    private static boolean isOrganisationLink(User user, Organisation organisation) {
    	boolean res = false;
		if (user.field_affiliation != null && organisation.url != null 
				&& user.field_affiliation.equals(organisation.url)) {
			res = true;
		} 
		return res;
	}
    
    /**
     * This method adds link to passed organisation in given User object if 
     * link does not already exists.
     * @param user
     * @param organisation
     */
    private static void addLink(User user, Organisation organisation) {
//		Logger.info("flag true add link: " + user.name);
    	if (!isOrganisationLink(user, organisation)) {
    		user.field_affiliation = organisation.url;
        	Ebean.update(user);
    	}
	}
    
    /**
     * This method removes link to passed organisation from given User object if 
     * link exists.
     * @param user
     * @param organisation
     */
    private static void removeLink(User user, Organisation organisation) {
//		Logger.info("flag false remove link: " + user.name);
    	if (isOrganisationLink(user, organisation)) {
    		Logger.info("remove link: " + user.name);
    		user.field_affiliation = "";
        	Ebean.update(user);
    	}
	}
    
    /**
     * This method implements administration for users associated with particular organisation.
     * @return
     */
    public static Result saveAdmin() {
    	Result res = null;
        String save = getFormParam(Const.SAVE);
        if (save != null) {
        	Organisation organisation = null;
            boolean isExisting = true;
            try {
               	organisation = Organisation.findByUrl(getFormParam(Const.URL));
               	
		        List<User> userList = User.findAll();
		        Iterator<User> userItr = userList.iterator();
		        while (userItr.hasNext()) {
		        	User user = userItr.next();
	                if (getFormParam(user.name) != null) {
//                		Logger.info("getFormParam(user.name): " + getFormParam(user.name) + " " + user.name);
		                boolean userFlag = Utils.getNormalizeBooleanString(getFormParam(user.name));
		                if (userFlag) {
		                	addLink(user, organisation); 
		                } else {
		                	removeLink(user, organisation); 
		                }
	                } else {
	                	removeLink(user, organisation); 	                	
	                }
		        }
            } catch (Exception e) {
            	Logger.info("User not existing exception");
            }
	        res = redirect(routes.Organisations.admin(organisation.url));
        } else {
        	res = ok();
        }
        return res;
    }
}
