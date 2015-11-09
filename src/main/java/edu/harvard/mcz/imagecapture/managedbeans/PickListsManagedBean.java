/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.PartAssociation;
import edu.harvard.mcz.imagecapture.data.Caste;
import edu.harvard.mcz.imagecapture.data.SpecimenPart;
import edu.harvard.mcz.imagecapture.ejb.FeaturesBeanLocal;
import edu.harvard.mcz.imagecapture.ejb.ImageFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.LifeStageBeanLocal;
import edu.harvard.mcz.imagecapture.ejb.LocationInCollectionBeanLocal;
import edu.harvard.mcz.imagecapture.ejb.NumberTypeBeanLocal;
import edu.harvard.mcz.imagecapture.ejb.SexBeanLocal;
import edu.harvard.mcz.imagecapture.ejb.TemplateFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.TypeStatusBeanLocal;
import edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.Dependent;

/**
 *
 * @author mole
 */
@Named(value="pickListsManagedBean")
@Dependent
public class PickListsManagedBean {

    @EJB
	private SexBeanLocal sexBean;
	@EJB
	private LifeStageBeanLocal lifeStageBean;
    @EJB
	private FeaturesBeanLocal featuresBean;
	@EJB
	private LocationInCollectionBeanLocal locationBean;
	@EJB
	private TypeStatusBeanLocal typeStatusBean;
	@EJB
	private NumberTypeBeanLocal numberTypeBean;
	@EJB
	private TemplateFacadeLocal templateBean;
    @EJB
	private UsersFacadeLocal usersBean;
    @EJB
    private ImageFacadeLocal imageBean ;
    

    /** Creates a new instance of PickListsManagedBean */
    public PickListsManagedBean() {
    }

	public List<String> getSexValues() {
		return sexBean.getSexValues();
	}

	public List<String> getLifeStageValues() {
		return lifeStageBean.getValues();
	}

	public List<String> getFeaturesValues() {
		return featuresBean.getValues();
	}

    public List<String> getLocationInCollectionValues()	{
			return locationBean.getValues();
	}

	public List<String> getTypeStatusValues() {
		return typeStatusBean.getValues();
	}

    public List<String> getNumberTypeValues() {
    	List<String> result = new ArrayList<String>();
    	result.add("");
    	result.add("Unknown");
    	result.add("Species Number");
    	result.add("Collector Number");
    	result.add("Collection Number");
    	result.add("MCZ Slide Number");
    	result.add("Genitalia Preparation");
    	result.add("DNA Sample Number");
    	result.add("Drawer Number");
    	result.add("Lycaenidae Morphology Ref.");
    	result.add("MCZ Butterfly Exhibit, 2000");
    	return result;
		// return numberTypeBean.getValues();
	}

	public List<String> getTemplateValues() {
		return templateBean.getTemplateNames();
	}
	
	public List<String> getUserRoleValues() {
		return usersBean.getUserRoleValues();
	}
	
	public List<String> getImagePaths() { 
		return imageBean.findUniquePaths();
	}

	/**
	 * Obtain a list of distinct image paths, excluding the blank path marker.
	 * 
	 * @return a list of strings comprising unique values for Image.path in the database.
	 */
	public List<String> getImagePathsNoBlank() { 
		return imageBean.findUniquePaths(false);
	}	
	
    public List<String> getAndGoToValues() {
		List<String> result = new ArrayList<String>();
		result.add("view");
		result.add("go next");
		result.add("go back");
		return result;
	}
    
    public List<String> getElevUnitsValues() { 
    	List<String> result = new ArrayList<String>();
    	result.add("");
    	result.add("?");
    	result.add("m");
    	result.add("ft");
    	return result;
    }
    
    public List<String> getLatDirValues() { 
    	List<String> result = new ArrayList<String>();
    	result.add("N");
    	result.add("S");
    	return result;
    }    
    public List<String> getLongDirValues() { 
    	List<String> result = new ArrayList<String>();
    	result.add("E");
    	result.add("W");
    	return result;
    }    
    
    public List<String> getOrigLatLongUnitsValues() { 
    	List<String> result = new ArrayList<String>();
    	result.add("decimal degrees");
    	result.add("deg. min. sec.");
    	result.add("degrees dec. minutes");
    	result.add("unknown");
    	return result;
    }    
    

    public List<String> getMaxErrorUnitValues() { 
    	List<String> result = new ArrayList<String>();
    	result.add("m");
    	result.add("ft");
    	result.add("km");
    	result.add("mi");
    	result.add("yd");
    	return result;
    }    
    
    public List<String> getGeorefMethodValues() {
		List<String> result = new ArrayList<String>();
		result.add("not recorded");
		result.add("unknown");
		result.add("GEOLocate");
		result.add("Google Earth");
		result.add("Gazeteer");
		result.add("GPS");
		result.add("MaNIS/HertNet/ORNIS Georeferencing Guidelines");
		return result;
	}    
    
    public List<String> getDatumValues() {
		List<String> result = new ArrayList<String>();
		result.add("not recorded");
		result.add("unknown");
		result.add("WGS84");
		result.add("NAD27");
		result.add("WGS 1972");
		result.add("North American 1983");
		result.add("North American 1928");
		result.add("North American 1929");
		result.add("Australian Geodetic 1966");
		result.add("Bogota Observatory");
		result.add("Corrego Alegre");
		result.add("Provisional South American 1956");
		result.add("PRP_M");
		result.add("POS");
		result.add("GRA");
		result.add("GDA94");
		result.add("Fundamental de Ocotepeque");
		result.add("AGD66");
		result.add("Clarke 1958");
		result.add("Japanese Geodetic Datum 2000");
		return result;
	}      
    
    public List<String> getPartNameValues() {
    	return Arrays.asList(SpecimenPart.PARTNAMES);
	}
    
    public List<String> getCountModifierValues() {
		List<String> result = new ArrayList<String>();
		result.add("");
		result.add("ca.");
		result.add(">");
		return result;
	}

    public List<String> getPreserveMethodValues() {
		List<String> result = new ArrayList<String>();
		result.add("pinned");
		result.add("pointed");
		result.add("carded");
		result.add("capsule");
		result.add("envelope");
		return result;
	}
    
    public List<String> getPartAttributeNameValues() {
		List<String> result = new ArrayList<String>();
		result.add("caste");
		result.add("scientific name");
		result.add("sex");
		result.add("life stage");
		result.add("associated taxon");
		return result;
	}
    
    
	public List<String> getNatureOfIdValues() {
		List<String> result = new ArrayList<String>();
		result.add("expert ID"); 
		result.add("type ID"); 
		result.add("legacy"); 
		result.add("ID based on molecular data");  
		result.add("ID to species group"); 
		result.add("erroneous citation"); 
		result.add("field ID"); 
		result.add("non-expert ID");  
		result.add("taxonomic revision"); 	
		return result;
	}	    
    
    public List<String> getCasteValues() { 
    	return Arrays.asList(Caste.getCasteValues());
    }
    
    public List<String> getAssociatedTaxonValues() { 
    	return Arrays.asList(PartAssociation.getPartAssociationValues());
    }
    
	/** To use with rich:autocomplete, given a suggestion, returns
	 * a list of matching OtherNumber.NumberType values.  Returns all
	 * existing values if suggestion is equal to a single space.
	 *
	 * @param suggest
	 * @return List<String> values beginning with suggest.
	 */
	public List<String> autocompleteTypeValues(String suggest) {
		List<String> candidates = numberTypeBean.getValues();
		if (suggest==null) { suggest = ""; }
		if (suggest.length() < 2) {
			return candidates;
		}
		List<String> results = new ArrayList<String>();
		Iterator<String> i = candidates.iterator();
		while (i.hasNext()) {
			String value = i.next();
			// case insensitve match
		    if (value.toUpperCase().startsWith(suggest.toUpperCase())) {
				results.add(value);
			}
		}
		return results;
	}

	public List<String> autocompleteFeaturesValues(String suggest) {
		List<String> candidates = featuresBean.getValues();
		if (suggest==null) { suggest = ""; }
		if (suggest.length() < 2) {
			return candidates;
		}
		List<String> results = new ArrayList<String>();
		Iterator<String> i = candidates.iterator();
		while (i.hasNext()) {
			String value = i.next();
			// case insensitve match
		    if (value.toUpperCase().startsWith(suggest.toUpperCase())) {
				results.add(value);
			}
		}
		return results;
	}

}
