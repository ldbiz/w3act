package uk.bl.db;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import models.Collection;
import models.ContactPerson;
import models.Flag;
import models.Instance;
import models.MailTemplate;
import models.Organisation;
import models.Permission;
import models.Role;
import models.Subject;
import models.Tag;
import models.Target;
import models.Taxonomy;
import models.TaxonomyType;
import models.User;

import com.avaje.ebean.Ebean;

import play.Logger;
import play.libs.Yaml;
import uk.bl.Const;
import uk.bl.api.JsonUtils;
import uk.bl.api.PasswordHash;
import uk.bl.api.Utils;

public enum DataImport {

	INSTANCE;

	public void insert() {
        try {

			if (Ebean.find(User.class).findRowCount() == 0) {
	        	this.importPermissions();
	        	this.importRoles();
	        	this.importJsonOrganisations();
	        	this.importOrganisations();
	        	this.importCurators();
	            this.importAccounts();
	        }
			if (Ebean.find(MailTemplate.class).findRowCount() == 0) {
	        	this.importMailTemplates();
			}
			if (Ebean.find(ContactPerson.class).findRowCount() == 0) {
	        	this.importContactPersons();
			}
			if (Ebean.find(TaxonomyType.class).findRowCount() == 0) {
				this.importJsonTaxonomyVocabularies();
			}
			if (Ebean.find(Taxonomy.class).findRowCount() == 0) {
				this.importJsonTaxonomies();
				this.importTaxonomies();
	        	this.importTags();
	        	this.importFlags();
			}
			if (Ebean.find(Target.class).findRowCount() == 0) {
	        	this.importTargets();
			}
			if (Ebean.find(Instance.class).findRowCount() == 0) {
				this.importInstances();
			}
	        	
//				// aggregate url data from drupal and store JSON content in a file
//		        List<Object> allUrls = JsonUtils.getDrupalData(Const.NodeType.URL);
//				// store urls in DB
//                Ebean.save(allUrls);
//                Logger.debug("targets successfully loaded");

                
                
                ////                List<Target> targetList = (List<Target>) Target.find.all();
////                Iterator<Target> targetItr = targetList.iterator();
////                while (targetItr.hasNext()) {
////                	Target target = targetItr.next();
//////                    Logger.debug("Target test object: " + target.toString());
////					if (target.field_subject == null
////							|| target.field_subject.length() == 0) {
////						target.field_subject = Const.NONE;
////						Ebean.update(target);
////					}
////                }
//                Logger.debug("load organisations ...");
//				// aggregate organisations data from drupal and store JSON content in a file
//		        List<Object> allOrganisations = JsonUtils.getDrupalData(Const.NodeType.ORGANISATION);
//		        List<Object> allSingleOrganisations = Organisations.skipExistingObjects(allOrganisations);
//				// store organisations in DB
//                Ebean.save(allSingleOrganisations);
//                JsonUtils.normalizeOrganisationUrlInUser();
//                Logger.debug("organisations successfully loaded");
//                Logger.debug("load taxonomies ...");
//                // aggregate original taxonomies from drupal extracting information from aggregated data
//		        List<Object> allTaxonomies = JsonUtils.INSTANCE.extractDrupalData(Const.NodeType.TAXONOMY);
////		        List<Taxonomy> cleanedTaxonomies = cleanUpTaxonomies(allTaxonomies);
//				// store taxonomies in DB
//                Ebean.save(allTaxonomies);
////                Ebean.save(cleanedTaxonomies);
//                Logger.debug("taxonomies successfully loaded");
//                // due to merging of different original object models the resulting 
//                // collection set is evaluated from particular taxonomy type
//                Logger.debug("load collections ..."); 
//		        List<Object> allCollections = JsonUtils.readCollectionsFromTaxonomies();
//				// store collections in DB
//                Ebean.save(allCollections);
//                Logger.debug("collections successfully loaded");
			
//                Logger.debug("load instances");
//				// aggregate instances data from drupal and store JSON content in a file
			
			
//		        List<Object> allInstances = JsonUtils.getDrupalData(Const.NodeType.INSTANCE);
//		        Logger.debug("Number of instances: " + allInstances.size());
//				// store instances in DB
//                Ebean.save(allInstances);
//                Logger.debug("instances successfully loaded");
//                JsonUtils.mapInstancesToTargets();
//                Logger.debug("map instances to targets");
			//JsonUtils.getDomainForTargets();
//                Logger.debug("Target domains extracted");
//          normalizeUrls();
//                // Create association between Creator and Organisation
//	            List<User> creatorList = (List<User>) User.find.all();
//	            Iterator<User> creatorItr = creatorList.iterator();
//	            while (creatorItr.hasNext()) {
//	              	User creator = creatorItr.next();
////                    Logger.debug("Test creator test object: " + creator.toString());
//                    creator.updateOrganisation(); // NO NEED AS WE ARE USING ORM AND ALREADY IMPORTED
//                    // Create association between User and Role
////                	creator.role_to_user = Role.convertUrlsToObjects(creator.roles);
//        			Ebean.update(creator);
//	            }                
//                // Create associations for Target
//	            List<Target> targetList = (List<Target>) Target.find.all();
//	            Iterator<Target> targetItr = targetList.iterator();
//	            while (targetItr.hasNext()) {
//	            	Target target = targetItr.next();
////                    Logger.debug("Test target object: " + target.toString());
//	            	// Create association between Target and Organisation
//	            	target.updateOrganisation();
//                    // Create association between Target and DCollection
//                	target.collectionToTarget = Collection.convertUrlsToObjects(target.fieldCollectionCategories);
//                    // Create association between Target and Subject (Taxonomy)
//                	target.subject = Taxonomy.convertUrlsToObjects(target.fieldSubject);
//                    // Create association between Target and License (Taxonomy)
//                	target.licenseToTarget = Taxonomy.convertUrlsToObjects(target.fieldLicense);
//                    // Create association between Target and Flag
//                	target.flagToTarget = Flag.convertUrlsToObjects(target.flags);
//                    // Create association between Target and Tag
//                	target.tagToTarget = Tag.convertUrlsToObjects(target.tags);
//        			Ebean.update(target);
//	            }
//                // Create associations for Instance
//	            List<Instance> instanceList = (List<Instance>) Instance.find.all();
//	            Iterator<Instance> instanceItr = instanceList.iterator();
//	            while (instanceItr.hasNext()) {
//	            	Instance instance = instanceItr.next();
//	                // Create association between Instance and Organisation
//                    instance.updateOrganisation();
//                    // Create association between Instance and DCollection
//                	instance.collectionToInstance = Collection.convertUrlsToObjects(instance.fieldCollectionCategories);
//                    // Create association between Instance and Subject (Taxonomy)
//                	instance.subjectToInstance = Taxonomy.convertUrlsToObjects(instance.fieldSubject); 		
//                    // Create association between Instance and Flag
//         instance.flagToInstance = Flag.convertUrlsToObjects(instance.flags);
//                    // Create association between Instance and Tag
//         instance.tagToInstance = Tag.convertUrlsToObjects(instance.tags);
//        			Ebean.update(instance);
//	            }
//                // Create association between Permission and Role			
			// TODO: KL WHY WE NEED TO DO THIS?
//	            List<Permission> permissionList = (List<Permission>) Permission.find.all();
//	            Iterator<Permission> permissionItr = permissionList.iterator();
//	            while (permissionItr.hasNext()) {
//	            	Permission permission = permissionItr.next();
////                    Logger.debug("Test permission test object: " + permission.toString());
//                    permission.updateRole();
//        			Ebean.update(permission);
//	            }
                Logger.debug("+++ Data import completed +++");
	        } catch (Exception e) {
            	e.printStackTrace();
            }
	}
	
	private void importPermissions() {
		@SuppressWarnings("unchecked")
		Map<String,List<Permission>> allPermissions = (Map<String,List<Permission>>)Yaml.load("Accounts.yml");
		List<Permission> permissions = allPermissions.get(Const.PERMISSIONS);
		for (Permission permission : permissions) {
			permission.save();
		}
//		Ebean.save(permissions);
	}
	
	private void importRoles() {
		@SuppressWarnings("unchecked")
		Map<String,List<Role>> allRoles = (Map<String,List<Role>>)Yaml.load("Accounts.yml");
		List<Role> roles = allRoles.get(Const.ROLES);
		for (Role role : roles) {
			role.save();
		}
//		Ebean.save(roles);
	}
	
	private void importAccounts() {
		@SuppressWarnings("unchecked")
		Map<String,List<User>> accounts = (Map<String,List<User>>)Yaml.load("Accounts.yml");
		List<User> users = accounts.get(Const.USERS);
		try {
			for (User user : users) {
				user.password = PasswordHash.createHash(user.password);
				user.createdAt = new Date();
			}
			Ebean.save(users);
//			Logger.debug("hash password: " + user.password);
		} catch (NoSuchAlgorithmException e) {
			Logger.debug("initial password creation - no algorithm error: " + e);
		} catch (InvalidKeySpecException e) {
			Logger.debug("initial password creation - key specification error: " + e);
		}
        Logger.debug("Loaded Permissions, Roles and Users");
	}
	
	private void importJsonTaxonomyVocabularies() {
		JsonUtils.INSTANCE.convertTaxonomyVocabulary();
        Logger.debug("Loaded Json Taxonomies Vocabularies");
	}

	private void importJsonTaxonomies() {
		JsonUtils.INSTANCE.convertTaxonomies();
        Logger.debug("Loaded Json Taxonomies");
	}
	
	private void importTaxonomies() {
		@SuppressWarnings("unchecked")
		Map<String,List<Taxonomy>> allTaxonomies = (Map<String,List<Taxonomy>>)Yaml.load("taxonomies.yml");
		List<Taxonomy> taxonomies = allTaxonomies.get(Const.TAXONOMIES);
		TaxonomyType tv = null;
		for (Taxonomy taxonomy : taxonomies) {
			
			// see if they are already stored?
			Taxonomy lookup = Taxonomy.findByNameAndType(taxonomy.name, taxonomy.ttype);
			if (lookup == null) {
				tv = TaxonomyType.findByMachineName(taxonomy.ttype);
				Logger.debug("ttype: " + taxonomy.ttype + " - " + tv);
				taxonomy.setTaxonomyType(tv);
				taxonomy.url = Const.ACT_URL + Utils.INSTANCE.createId();
				if (StringUtils.isNotEmpty(taxonomy.parentName)) {
					Taxonomy parent = Taxonomy.findByNameAndType(taxonomy.parentName, taxonomy.ttype);
					Logger.debug("Parent found: " + parent);
					if (parent != null) {
						if (taxonomy instanceof Collection) {
							((Collection)taxonomy).parent = (Collection)parent;
						}
						else if (taxonomy instanceof Subject) {
							((Subject)taxonomy).parent = (Subject)parent;
						}
					}
				}
				taxonomy.save();
			}
		}
        Logger.debug("Loaded Taxonomies");
	}
	
	private void importTags() { 
		@SuppressWarnings("unchecked")
		Map<String,List<Tag>> allTags = (Map<String,List<Tag>>)Yaml.load("tags.yml");
		List<Tag> tags = allTags.get(Const.TAGS);
		for (Tag tag : tags) {
			tag.url = Const.ACT_URL + Utils.INSTANCE.createId();
			tag.save();
		}
        Logger.debug("Loaded Tags");
	}
	
	private void importFlags() {
		@SuppressWarnings("unchecked")
		Map<String,List<Flag>> allFlags = (Map<String,List<Flag>>)Yaml.load("flags.yml");
		List<Flag> flags = allFlags.get(Const.FLAGS);
		for (Flag flag : flags) {
			flag.url = Const.ACT_URL + Utils.INSTANCE.createId();
			flag.save();
		}
        Logger.debug("Loaded Flags");
	}

	private void importMailTemplates() {
		@SuppressWarnings("unchecked")
		Map<String,List<MailTemplate>> allTemplates = (Map<String,List<MailTemplate>>)Yaml.load("mail-templates.yml");
		List<MailTemplate> mailTemplates = allTemplates.get(Const.MAILTEMPLATES);
		for (MailTemplate mailTemplate : mailTemplates) {
			mailTemplate.url = Const.ACT_URL + Utils.INSTANCE.createId();
			mailTemplate.save();
		}
        Logger.debug("Loaded MailTemplates");
	}
	
	private void importContactPersons() {
		@SuppressWarnings("unchecked")
		Map<String,List<ContactPerson>> allContactPersons = (Map<String,List<ContactPerson>>)Yaml.load("contact-persons.yml");
		List<ContactPerson> contactPersons = allContactPersons.get(Const.CONTACTPERSONS);
		for (ContactPerson contactPerson : contactPersons) {
			contactPerson.url = Const.ACT_URL + Utils.INSTANCE.createId();
			contactPerson.save();
		}
        Logger.debug("Loaded ContactPersons");
	}
	
	private void importOrganisations() {
		@SuppressWarnings("unchecked")
		Map<String,List<Organisation>> allOrganisations = (Map<String,List<Organisation>>)Yaml.load("organisations.yml");
		List<Organisation> organisations = allOrganisations.get(Const.ORGANISATIONS);
		for (Organisation organisation : organisations) {
			organisation.url = Const.ACT_URL + Utils.INSTANCE.createId();
			organisation.save();
		}
        Logger.debug("Loaded Organisations");
	}

	private void importJsonOrganisations() {
		JsonUtils.INSTANCE.convertOrganisations();
        Logger.debug("Loaded Json Organisations");
	}
	
	private void importCurators() {
		JsonUtils.INSTANCE.convertCurators();
        Logger.debug("Loaded Curators");
	}
	
	private void importTargets() {
		// store urls in DB
        JsonUtils.INSTANCE.convertTargets();
        Logger.debug("Loaded URLs");
	}
	
	private void importInstances() {
        JsonUtils.INSTANCE.convertInstances();;
        Logger.debug("Loaded Instances");
	}

//    /**
//	 * normalize URL if there is "_" e.g. in taxonomy_term
//	 */
//	public void normalizeUrls() {
//        List<Target> targets = Target.findAll();
//        Iterator<Target> itr = targets.iterator();
//        while (itr.hasNext()) {
//        	Target target = itr.next();
////			if (target.fieldCollectionCategories != null && target.fieldCollectionCategories.contains("_")) {
////				target.fieldCollectionCategories = target.fieldCollectionCategories.replace("_", "/");
////			}
////			if (target.fieldLicense != null && target.fieldLicense.contains("_")) {
////				target.fieldLicense = target.fieldLicense.replace("_", "/");
////			}
//            Ebean.update(target);
//		}
//	}

//    /**
//     * This method removes from taxonomy list old subject taxonomies.
//     * @param taxonomyList
//     * @return
//     */
//    public List<Taxonomy> cleanUpTaxonomies(List<Object> taxonomyList) {
//    	List<Taxonomy> res = new ArrayList<Taxonomy>();
//        Iterator<Object> taxonomyItr = taxonomyList.iterator();
//        while (taxonomyItr.hasNext()) {
//        	Taxonomy taxonomy = (Taxonomy) taxonomyItr.next();
//        	if (!(taxonomy.ttype.equals(Const.SUBJECT) && (taxonomy.parent == null || taxonomy.parent.length() == 0)) 
//        			&& !(taxonomy.ttype.equals(Const.SUBSUBJECT) && taxonomy.parent.contains(Const.ACT_URL))) {
//        		res.add(taxonomy);
//        	}
//        }
//        return res;
//    }
    
	public static void main(String[] args) {
		Logger.debug("start");
		new play.core.StaticApplication(new java.io.File("."));
		DataImport.INSTANCE.insert();
		Logger.debug("finished");
	}
}
