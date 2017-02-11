package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.data.Collector;
import edu.harvard.mcz.imagecapture.data.Determination;
import edu.harvard.mcz.imagecapture.data.Image;
import edu.harvard.mcz.imagecapture.data.LatLong;
import edu.harvard.mcz.imagecapture.data.MCZbaseAuthAgentName;
import edu.harvard.mcz.imagecapture.data.MCZbaseGeogAuthRec;
import edu.harvard.mcz.imagecapture.data.OtherNumbers;
import edu.harvard.mcz.imagecapture.data.Specimen;
import edu.harvard.mcz.imagecapture.data.SpecimenPart;
import edu.harvard.mcz.imagecapture.data.SpecimenPartAttribute;
import edu.harvard.mcz.imagecapture.data.Users;
import edu.harvard.mcz.imagecapture.data.WorkFlowStatus;
import edu.harvard.mcz.imagecapture.ejb.HigherTaxonFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.LatLongFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.MCZbaseAuthAgentNameFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.MCZbaseGeogAuthRecFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.MetadataRetrieverSessionBeanLocal;
import edu.harvard.mcz.imagecapture.ejb.SpecimenPartAttributeFacadeLocal;
import edu.harvard.mcz.imagecapture.jsfclasses.util.JsfUtil;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.ejb.SpecimenFacadeLocal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.primefaces.event.RowEditEvent;

@ManagedBean(name = "specimenController")
@SessionScoped
public class SpecimenController {

	private final static Logger logger = Logger.getLogger(SpecimenController.class.getName());

	private Specimen current;
	private Specimen lastEditedSpecimen = null;
	private Specimen undoState = null;
	private DataModel<Specimen> items = null;
	@EJB
	private edu.harvard.mcz.imagecapture.ejb.SpecimenFacadeLocal specimenFacade;
	@EJB
	private MetadataRetrieverSessionBeanLocal metadataRetrieverEJBFacade;
	@EJB
	private edu.harvard.mcz.imagecapture.ejb.DeterminationFacadeLocal detEjbFacade;
	@EJB
	private edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal userBean;
	@EJB 
	private MCZbaseGeogAuthRecFacadeLocal higherGeogFacade; 
	@EJB
	private MCZbaseAuthAgentNameFacadeLocal agentNameFacade; 
	@EJB
	private HigherTaxonFacadeLocal higherTaxonFacade;
	@EJB
	private SpecimenPartAttributeFacadeLocal partAttributeFacade;
	@EJB 
	private LatLongFacadeLocal latLongFacade;
	
	
	private PaginationHelper pagination;
	private int selectedItemIndex;  // index including pages
	private int currentRowIndex;    // within page index
	private boolean sortByBarcode = false;
	private boolean sortByFamily = true;
	private boolean sortBySubfamily = true;
	private boolean sortByGenus = true;
	private boolean sortBySpecificEpithet = true;
	private boolean sortByCountry = true;  
	private boolean sortByPrimaryDivision = true;
	private boolean sortByWorkflowStatus = true;
	private boolean sortByFilename = true;  // image filenames.
	private boolean sortByHigherGeography = true; 
	// Filter criteria
	private String barcodeFilterCriterion = null;
	private String barcodesFilterCriterion = null;        
	private String genusFilterCriterion = null;
	private String specificepithetFilterCriterion = null;
	private String subspecificepithetFilterCriterion = null;
	private String familyFilterCriterion = null;
	private String collectionFilterCriterion = null;
	private String lastupdatedbyFilterCriterion = null;
	private String countryFilterCriterion = null;
	private String higherGeographyFilterCriterion = null;
	private String primarydivisionFilterCriterion = null;
	private String workflowstatusFilterCriterion = null;
	private String collectorFilterCriterion = null;
	private String pathFilterCriterion = null;
	/**
	 * temp_ attributes to support addition of new related
	 * objects from the SpecimenController.
	 */
	private String temp_NumberType = "";
	private String temp_Number;
	private String temp_genus;
	private String temp_specificEpithet;
	private String temp_subspecificEpithet;
	private String temp_infraspecificEpithet;
	private String temp_infraspecificRank;
	private String temp_authorship;
	private String temp_speciesNumber;
	private String temp_unNamedForm;
	private String temp_identifiedBy;
	private String temp_typeStatus;
	private String temp_verbatimText;
	private String temp_natureOfId;
	private String temp_dateIdentified;
	private String temp_remarks;
	private String temp_CollectorName;
	private Determination temp_CurrentEditIdentification;
	private SpecimenPartAttribute temp_CurrentPartAttribute;
	/**
	 * Value that determines what record is shown next when saving a record;
	 */
    private String andGoTo;



	public SpecimenController() {
		super();
		setDefaultSort();
		temp_CurrentEditIdentification = new Determination();
	}

	public Specimen getSelected() {
		if (current == null) {
			current = new Specimen();
		    currentRowIndex = -1;
			selectedItemIndex = -1;
		}
		return current;
	}

	public void setSelected(Specimen selected) {
		if (selected!=null) {
			current=selected;
			currentRowIndex=-1;
			selectedItemIndex = -1;
			pagination = null;
		}
	}

	/**
	 * Method to invoke from a JSF form to add a new OtherNumber to the current
	 * instance of a Specimen.  Use a form containing input fields bound to
	 * temp_NumberType and temp_Number, and an add button that triggers this
	 * method.
	 *
	 * @return null
	 */
	public String createNewOtherNumber() {
		OtherNumbers newOtherNumbers = new OtherNumbers();
		newOtherNumbers.setSpecimenId(current);
		newOtherNumbers.setNumberType(temp_NumberType);
		newOtherNumbers.setOtherNumber(temp_Number);
		current.getOtherNumbersCollection().add(newOtherNumbers);
		temp_NumberType = "";
		temp_Number = "";
		FacesContext.getCurrentInstance().renderResponse();
		return null;
	}
	
	/**
	 * Method to invoke from a JSF form to add a new Determination to the current
	 * instance of a Specimen.  Use a form containing input fields bound to
	 * temp_Genus and temp_Species, and an add button that triggers this
	 * method.  Include a table bound to the Specimen.determinationCollection
	 * showing other fields within determination to allow them to be added.
	 *
	 * @return null
	 */
	public String createNewDetermination() {
logger.log(Level.INFO, "SpecimenController.createNewDetermination() was invoked.");
		Determination newDetermination = new Determination();
		newDetermination.setSpecimenId(current);
		newDetermination.setGenus(temp_genus);
		newDetermination.setSpecificEpithet(temp_specificEpithet);
		newDetermination.setSubspecificEpithet(temp_subspecificEpithet);
		newDetermination.setInfraspecificEpithet(temp_infraspecificEpithet);
		newDetermination.setInfraspecificRank(temp_infraspecificRank);
		newDetermination.setAuthorship(temp_authorship);
		newDetermination.setUnNamedForm(temp_unNamedForm);
		newDetermination.setIdentifiedBy(temp_identifiedBy);
		newDetermination.setSpeciesNumber(temp_speciesNumber);
		newDetermination.setTypeStatus(temp_typeStatus);
		newDetermination.setVerbatimText(temp_verbatimText);
		newDetermination.setDateIdentified(temp_dateIdentified);
		newDetermination.setNatureOfId(temp_natureOfId);
		newDetermination.setRemarks(temp_remarks);
		detEjbFacade.create(newDetermination);
		current.getDeterminationCollection().add(newDetermination);
//TODO: ??? This log message is invoked on web page load ????		
		logger.log(Level.INFO, Long.toString(newDetermination.getDeterminationId()));

		temp_genus = "";
		temp_specificEpithet = "";
		temp_subspecificEpithet = "";
		temp_infraspecificEpithet = "";
		temp_infraspecificRank = "";
		temp_authorship = "";
		temp_unNamedForm = "";
		temp_identifiedBy = "";
		temp_speciesNumber = "";
		temp_typeStatus = "";
		temp_verbatimText = "";
		temp_natureOfId = "expert ID";
		temp_dateIdentified = "";
		temp_remarks = "";
		FacesContext.getCurrentInstance().renderResponse();
		return null;
	}

	/**
	 * Method to invoke from a JSF form to add a new Collector to the current
	 * instance of a Specimen.  Use a form containing an input field bound to
	 * temp_CollectorName, and an add button that triggers this method.
	 *
	 * @return null
	 */
	public String createNewCollector() {
		Collector newCollector = new Collector();
		newCollector.setSpecimenId(current);
		newCollector.setCollectorName(temp_CollectorName);
		current.getCollectorCollection().add(newCollector);
		temp_CollectorName = "";
		FacesContext.getCurrentInstance().renderResponse();
		return null;
	}
	
	public String deleteCollector(Collector collToRemove) { 
		current.getCollectorCollection().remove(collToRemove);
		FacesContext.getCurrentInstance().renderResponse();
		return null;
	}

	public String getDrawerNumberSize() {
		return Integer.toString(metadataRetrieverEJBFacade.getFieldLength(Specimen.class, "DrawerNumber"));
	}

	public String getTypeNumberSize() {
		return Integer.toString(metadataRetrieverEJBFacade.getFieldLength(Specimen.class, "TypeNumber"));
	}

	public String getCitedInPublicationSize() {
		return Integer.toString(metadataRetrieverEJBFacade.getFieldLength(Specimen.class, "CitedInPublication"));
	}

	public String getAuthorshipSize() {
		return Integer.toString(metadataRetrieverEJBFacade.getFieldLength(Specimen.class, "Authorship"));
	}

	public String getUnNamedFormSize() {
		return Integer.toString(metadataRetrieverEJBFacade.getFieldLength(Specimen.class, "UnNamedForm"));
	}

	public String getIdentifiedBySize() {
		return Integer.toString(metadataRetrieverEJBFacade.getFieldLength(Specimen.class, "IdentifiedBy"));
	}

	private SpecimenFacadeLocal getFacade() {
		return specimenFacade;
	}

	public PaginationHelper getPagination() {
		if (pagination == null) {
			pagination = new PaginationHelper(50) {

				public Map<String, String> getFilterMap() {
					Map<String, String> filters = new HashMap<String, String>();
					if (barcodeFilterCriterion != null) {
						filters.put("barcode", barcodeFilterCriterion);
					}
					if (barcodesFilterCriterion != null) {
						filters.put("magic_SpecimenBarcodeList", barcodesFilterCriterion);
					}                                        
					if (genusFilterCriterion != null) {
						filters.put("genus", genusFilterCriterion);
					}
					if (specificepithetFilterCriterion != null) {
						filters.put("specificEpithet", specificepithetFilterCriterion);
					}
					if (subspecificepithetFilterCriterion != null) {
						filters.put("subspecificEpithet", subspecificepithetFilterCriterion);
					}
					if (familyFilterCriterion != null) {
						filters.put("family", familyFilterCriterion);
					}
					if (countryFilterCriterion != null) {
						filters.put("country", countryFilterCriterion);
					}
					if (higherGeographyFilterCriterion != null) {
						filters.put("higherGeography", higherGeographyFilterCriterion);
					}					
					if (primarydivisionFilterCriterion != null) {
						filters.put("primaryDivison", primarydivisionFilterCriterion);
					}
					if (workflowstatusFilterCriterion != null) {
						filters.put("workFlowStatus", workflowstatusFilterCriterion);
					}
					if (lastupdatedbyFilterCriterion != null) {
						filters.put("lastUpdatedBy", lastupdatedbyFilterCriterion);
					}
					if (collectionFilterCriterion != null) {
						filters.put("collection", collectionFilterCriterion);
					}
					if (collectorFilterCriterion != null) {
						filters.put("collectorName", collectorFilterCriterion);
					}
					if (pathFilterCriterion != null) {
						filters.put("creatingPath", pathFilterCriterion);
					}					
					return filters;
				}

				@Override
				public int getItemsCount() {
					Map<String, String> filters = getFilterMap();
					if (filters.isEmpty()) {
						return getFacade().count();
					} else {
						return getFacade().countFiltered(filters);
					}
				}

				@Override
				public DataModel createPageDataModel() {
					DataModel result = null;
					int[] range = new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()};
					if (0 == 1) {
						result = new ListDataModel(getFacade().findRange(range));
					} else {
						ArrayList<String> sortFields = new ArrayList();
						if (isSortByBarcode()) {
							sortFields.add("barcode");
						}
						if (isSortByFilename()) {
							sortFields.add("creatingFilename");
						}
						if (isSortByFamily()) {
							sortFields.add("family");
						}
						if (isSortBySubfamily()) {
							sortFields.add("subfamily");
						}
						if (isSortByGenus()) {
							sortFields.add("genus");
						}
						if (isSortBySpecificEpithet()) {
							sortFields.add("specificEpithet");
						}
						if (isSortByCountry()) {
							sortFields.add("country");
						}
						if (isSortByPrimaryDivision()) {
							sortFields.add("primaryDivison");
						}
						if (isSortByWorkflowStatus()) {
							sortFields.add("workFlowStatus");
						}
						Map<String, String> filters = getFilterMap();
						boolean useAnd = true;
					    if (barcodesFilterCriterion != null && barcodesFilterCriterion != "") {
							useAnd = false;
						}
						result = new ListDataModel(
								getFacade().findRangeQueryAndOr(
								range, sortFields.toArray(new String[0]), true, filters,useAnd));
logger.log(Level.INFO, "Range:" + range[0] + "-" + range[1]);						
					}
					return result;
				}
			};
		}
		return pagination;
	}

	/** Clear all filter criteria except for collector filter criteria
	 * then prepare list.
	 *
	 * @return String URI of list page with redirect
	 */
    public String prepareCollectorListFromIndex() {
		genusFilterCriterion = null;
		specificepithetFilterCriterion = null;
		subspecificepithetFilterCriterion = null;
		familyFilterCriterion = null;
		collectionFilterCriterion = null;
		lastupdatedbyFilterCriterion = null;
		countryFilterCriterion = null;
		higherGeographyFilterCriterion = null;
		primarydivisionFilterCriterion = null;
		workflowstatusFilterCriterion = null;
		pathFilterCriterion = null;
		barcodesFilterCriterion = null;
		return prepareListFromIndex();
	}
	/** Clear all filter criteria except for country 
	 *  and primary division filter criteria
	 *  then prepare list.
	 *
	 * @return String URI of list page with redirect
	 */
    public String prepareCountryPrimaryListFromIndex() {
		genusFilterCriterion = null;
		specificepithetFilterCriterion = null;
		subspecificepithetFilterCriterion = null;
		familyFilterCriterion = null;
		collectionFilterCriterion = null;
		lastupdatedbyFilterCriterion = null;
		workflowstatusFilterCriterion = null;
		collectorFilterCriterion = null;
		pathFilterCriterion = null;
		barcodesFilterCriterion = null;
		return prepareListFromIndex();
	}

	/** Clear all filter criteria except for country filter criteria
	 * then prepare list.
	 *
	 * @return String URI of list page with redirect
	 */
    public String prepareCountryListFromIndex() {
		genusFilterCriterion = null;
		specificepithetFilterCriterion = null;
		subspecificepithetFilterCriterion = null;
		familyFilterCriterion = null;
		collectionFilterCriterion = null;
		lastupdatedbyFilterCriterion = null;
		primarydivisionFilterCriterion = null;
		workflowstatusFilterCriterion = null;
		collectorFilterCriterion = null;
		pathFilterCriterion = null;
		barcodesFilterCriterion = null;
		return prepareListFromIndex();
	}

    
    /** Remove any barcodes filter criterion then run search, prevents 
     * barcodes search and main index search from interfering with each other. 
      */
    public String prepareListFromIndexSearch() { 
        barcodesFilterCriterion = null;
    	return prepareListFromIndex();
    }
    
    /** Remove any non-barcodes filter criteria then run search, prevents 
     * barcodes search and main index search from interfering with each other. 
      */
    public String prepareListFromIndexBarcodes() { 
		genusFilterCriterion = null;
		specificepithetFilterCriterion = null;
		subspecificepithetFilterCriterion = null;
		familyFilterCriterion = null;
		collectionFilterCriterion = null;
		lastupdatedbyFilterCriterion = null;
		countryFilterCriterion = null;
		higherGeographyFilterCriterion = null;
		primarydivisionFilterCriterion = null;
		workflowstatusFilterCriterion = null;
		collectorFilterCriterion = null;
		pathFilterCriterion = null;    	
    	return prepareListFromIndex();
    }
    
    public String prepareListFromIndex() {
		pagination = null;
		recreateModel();
		return "/lepidoptera/specimen/List?faces-redirect=true";
	}

	public String prepareList() {
		pagination = null;
		recreateModel();
		return "List?faces-redirect=true";
	}

	public String prepareListAll() {
		pagination = null;
		items = null;
		resetFilters();
		getPagination().createPageDataModel();
		recreateModel();
		return "List?faces-redirect=true";
	}

	public void resetFilters() {
		genusFilterCriterion = null;
		specificepithetFilterCriterion = null;
		subspecificepithetFilterCriterion = null;
		familyFilterCriterion = null;
		collectionFilterCriterion = null;
		lastupdatedbyFilterCriterion = null;
		countryFilterCriterion = null;
		higherGeographyFilterCriterion = null;
		primarydivisionFilterCriterion = null;
		workflowstatusFilterCriterion = null;
		collectorFilterCriterion = null;
		pathFilterCriterion = null;
	}

	public String prepareView() {
		current = (Specimen) getItems().getRowData();
		currentRowIndex = getItems().getRowIndex();
		selectedItemIndex = getPagination().getPageFirstItem() + getItems().getRowIndex();
		return "View?faces-redirect=true";
	}

	public String prepareCreate() {
		current = new Specimen();
		currentRowIndex = -1;
		selectedItemIndex = -1;
		return "Create?faces-redirect=true";
	}

	public String create() {
		try {
			getFacade().create(current);
			JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("SpecimenCreated"));
			return prepareCreate();
		} catch (Exception e) {
			JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
			return null;
		}
	}

	public String prepareEditTest() {
		current = (Specimen) getItems().getRowData();
		
		// TODO: Does this work?
		if (current.getGeoreference()==null) { 
			LatLong georeference = new LatLong();
			current.setGeoreference(georeference);
			georeference.setSpecimenid(current);
			latLongFacade.create(georeference);
			specimenFacade.flush(current);
		}
				
		undoState = specimenFacade.cloneDetatch(current);
		currentRowIndex = getItems().getRowIndex();
		selectedItemIndex = getPagination().getPageFirstItem() + getItems().getRowIndex();
		return "Edit_test?faces-redirect=true";
	}	
	
	public String prepareEdit() {
		current = (Specimen) getItems().getRowData();
		
		// TODO: Does this work?
		if (current.getGeoreference()==null) { 
			LatLong georeference = new LatLong();
			current.setGeoreference(georeference);
			georeference.setSpecimenid(current);
			latLongFacade.create(georeference);
			specimenFacade.flush(current);
		}
		
		
		undoState = specimenFacade.cloneDetatch(current);
		currentRowIndex = getItems().getRowIndex();
		selectedItemIndex = getPagination().getPageFirstItem() + getItems().getRowIndex();
		return "Edit?faces-redirect=true";
	}

	/**Is there a next row to edit, will prepareEditNext() return a specimen.
	 * Checks if there is a next row in the current page or if there is a 
	 * next page.
	 *
	 * @return true if there is a next row.
	 */
    public boolean isHasNextRow() {
		boolean result = false;
		int rowIndex = currentRowIndex;
		logger.log(Level.INFO,"rowindex:" + rowIndex);
		try {
			getItems().setRowIndex(rowIndex + 1);
			int riplus1 = rowIndex+1;
		    logger.log(Level.INFO,"rowindex+1:" + riplus1 +  " isRowAvailable:" + getItems().isRowAvailable() + " isHasNextPage:" + getPagination().isHasNextPage());
			if (getItems().isRowAvailable()) {
				result = true;
			} else if (getPagination().isHasNextPage()) {
				result = true;
			}
		} catch (Exception e) {
			logger.log(Level.INFO, e.getMessage(), e);
			result = false;
		}
		// Stay at the current row.
		getItems().setRowIndex(currentRowIndex);
		return result;
	}

	/** Move to edit the next row in the list of items,
	 * traversing page boundaries if encountered.
	 *
	 * @return Edit?faces-redirect=true
	 */
	public String prepareEditNext() {
		// TODO: Stops at page boundary and doesn't move onto next page.

		int rowIndex = currentRowIndex;
		getItems().setRowIndex(rowIndex + 1);
		if (!getItems().isRowAvailable()) {
			if (getPagination().isHasNextPage()) { 
				logger.log(Level.INFO,"Moving to next page.");
				getPagination().nextPage();
				recreateModel();
				getItems().setRowIndex(0);
			} else { 
				logger.log(Level.INFO,"At last row.");
			   // if no row at next index, stay at current row.
			   getItems().setRowIndex(currentRowIndex);
			}
		} else { 
				logger.log(Level.INFO,"Moving to next row.");
		}
		current = (Specimen) getItems().getRowData();
		
		// TODO: Does this work?
try { 		
		if (current.getGeoreference()==null) { 
			LatLong georeference = new LatLong();
			current.setGeoreference(georeference);
			georeference.setSpecimenid(current);
logger.log(Level.INFO, "A: " + georeference.getLatLongId());
			latLongFacade.create(georeference);
			//latLongFacade.edit(georeference);
logger.log(Level.INFO, "C: " + georeference.getLatLongId());
			specimenFacade.flush(current);
logger.log(Level.INFO, "D: " + georeference.getLatLongId() + " " + georeference.toString());
logger.log(Level.INFO, "E: " + current.getGeoreference().toString()+ " " + georeference.toString());
		}
} catch (Exception e) { 
	logger.log(Level.SEVERE, e.getMessage(), e);
}
try {				
		undoState = specimenFacade.cloneDetatch(current);
} catch (Exception e) { 
	logger.log(Level.SEVERE, e.getMessage(), e);
}
		currentRowIndex = getItems().getRowIndex();
		selectedItemIndex = getPagination().getPageFirstItem() + getItems().getRowIndex();
		return "Edit?faces-redirect=true";
	}

	/**Is there a previous row to edit? 
	 * 
	 * @return true if there is a previous row (on the same or previous page).
	 */
    public boolean isHasPreviousRow() {
		boolean result = false;
		int rowIndex = currentRowIndex;
		logger.log(Level.INFO,"rowindex:" + rowIndex);
		try {
			getItems().setRowIndex(rowIndex - 1);
			if (getItems().isRowAvailable()) {
		        logger.log(Level.INFO,"Has a previous row");
				result = true;
			} else if (getPagination().isHasPreviousPage()) {
				result = true;
			}
		} catch (Exception e) {
			logger.log(Level.INFO, e.getMessage());
			result = false;
		}
		// Stay at the current row.
		getItems().setRowIndex(currentRowIndex);
		return result;
	}

	/** Move to edit the previous row in the list of records,
	 * traversing page boundaries if encountered.
	 *
	 * @return Edit?faces-redirect=true
	 */
	public String prepareEditPrevious() {
		int rowIndex = currentRowIndex;
		try {
			getItems().setRowIndex(rowIndex - 1);
			if (!getItems().isRowAvailable()) {
				if (pagination.isHasPreviousPage()) { 
					// move to previous page.
				    getPagination().previousPage();
				    recreateModel();
				} else { 
				     // if no row at next index, stay at current row.
				    getItems().setRowIndex(rowIndex);
				} 
			}
		} catch (Exception e) {
			// allready at first row.  Stay at current item
			getItems().setRowIndex(rowIndex);
		}
		current = (Specimen) getItems().getRowData();
		
		if (current.getGeoreference()==null) { 
			LatLong georeference = new LatLong();
			current.setGeoreference(georeference);
			georeference.setSpecimenid(current);
			latLongFacade.create(georeference);
			specimenFacade.flush(current);
		}
		
		undoState = specimenFacade.cloneDetatch(current);
		currentRowIndex = getItems().getRowIndex();
		selectedItemIndex = getPagination().getPageFirstItem() + getItems().getRowIndex();
		return "Edit?faces-redirect=true";
	}

	/**
	 * Paste into the current record values of the locality/collecting event
	 * fields from the most recently edited specimen.
	 * Stores current state in undo state.
	 * @see pasteFromUndo
	 */
	public void pasteFromPrevious() { 
		undoState = specimenFacade.cloneDetatch(current);
		if (this.lastEditedSpecimen!=null) { 
		    logger.log(Level.INFO,"664 undoState latlong specimenid" + undoState.getGeoreference().getSpecimenid().getSpecimenId());
		    logger.log(Level.INFO,"lastEdited latlong specimenid" + lastEditedSpecimen.getGeoreference().getSpecimenid().getSpecimenId());
		    logger.log(Level.INFO,"current latlong specimenid" + current.getGeoreference().getSpecimenid().getSpecimenId());
		    
			current.setVerbatimLocality(lastEditedSpecimen.getVerbatimLocality());
			current.setMinimum_elevation(lastEditedSpecimen.getMinimum_elevation());
			current.setMaximum_elevation(lastEditedSpecimen.getMaximum_elevation());
			current.setElev_units(lastEditedSpecimen.getElev_units());
			current.setHigherGeography(lastEditedSpecimen.getHigherGeography());
			current.setCountry(lastEditedSpecimen.getCountry());
			current.setPrimaryDivison(lastEditedSpecimen.getPrimaryDivison());
			current.setVerbatimCollector(lastEditedSpecimen.getVerbatimCollector());
			current.setVerbatimCollection(lastEditedSpecimen.getVerbatimCollection());
			current.setVerbatimNumbers(lastEditedSpecimen.getVerbatimNumbers());
			current.setVerbatimUnclassifiedText(lastEditedSpecimen.getVerbatimUnclassifiedText());
			current.setSpecificLocality(lastEditedSpecimen.getSpecificLocality());
			current.setDateCollected(lastEditedSpecimen.getDateCollected());
			current.setDateEmerged(lastEditedSpecimen.getDateEmerged());
			current.setDateNOS(lastEditedSpecimen.getDateNOS());
			current.setISODate(lastEditedSpecimen.getISODate());
			current.setDateCollectedIndicator(lastEditedSpecimen.getDateCollectedIndicator());
			current.setDateEmergedIndicator(lastEditedSpecimen.getDateEmergedIndicator());
			current.setCollection(lastEditedSpecimen.getCollection());
			current.setCollectingMethod(lastEditedSpecimen.getCollectingMethod());
			
			current.setGeoreferenceValues(lastEditedSpecimen.getGeoreference());
			logger.log(Level.INFO, "Georeference is current = " + Boolean.toString(latLongFacade.isManaged(current.getGeoreference())));
			//latLongFacade.edit(current.getGeoreference());
			//specimenFacade.flush(current);
			
		    logger.log(Level.INFO,"678 undoState latlong specimenid" + undoState.getGeoreference().getSpecimenid().getSpecimenId());
		    logger.log(Level.INFO,"lastEdited latlong specimenid" + lastEditedSpecimen.getGeoreference().getSpecimenid().getSpecimenId());
		    logger.log(Level.INFO,"current latlong specimenid" + current.getGeoreference().getSpecimenid().getSpecimenId());
			
		    specimenFacade.replaceCollectorCollection(current,lastEditedSpecimen.getCollectorCollection());
			specimenFacade.trackChange(current);
		}
	}
	
	/**
	 * Paste into the current record values of the locality/collecting event
	 * fields from the undo state, used to undo pasteFromPrevious()
	 * @see pasteFromPrevious
	 */
	public void pasteFromUndo() { 
		if (this.undoState!=null) { 
			current.setVerbatimLocality(undoState.getVerbatimLocality());
			current.setMinimum_elevation(undoState.getMinimum_elevation());
			current.setMaximum_elevation(undoState.getMaximum_elevation());
			current.setElev_units(undoState.getElev_units());
			current.setHigherGeography(undoState.getHigherGeography());
			current.setCountry(undoState.getCountry());
			current.setPrimaryDivison(undoState.getPrimaryDivison());
			current.setVerbatimCollector(undoState.getVerbatimCollector());
			current.setVerbatimCollection(undoState.getVerbatimCollection());
			current.setVerbatimNumbers(undoState.getVerbatimNumbers());
			current.setVerbatimUnclassifiedText(undoState.getVerbatimUnclassifiedText());			
			current.setSpecificLocality(undoState.getSpecificLocality());
			current.setDateCollected(undoState.getDateCollected());
			current.setDateEmerged(undoState.getDateEmerged());
			current.setDateNOS(undoState.getDateNOS());
			current.setISODate(undoState.getISODate());
			current.setDateCollectedIndicator(undoState.getDateCollectedIndicator());
			current.setDateEmergedIndicator(undoState.getDateEmergedIndicator());
			current.setCollection(undoState.getCollection());
			current.setCollectingMethod(undoState.getCollectingMethod());
			current.setGeoreferenceValues(undoState.getGeoreference());
			specimenFacade.replaceCollectorCollection(current,undoState.getCollectorCollection());
			specimenFacade.trackChange(current);
		}
	}	
	
	
	/**
	 * Paste into the current record values of the identification 
	 * fields from the most recently edited specimen.
	 * Stores current state in undo state.
	 * @see pasteTaxnoFromUndo
	 */
	public void pasteTaxonFromPrevious() { 
		undoState = specimenFacade.cloneDetatch(current);
		if (this.lastEditedSpecimen!=null) { 
            current.setFamily(lastEditedSpecimen.getFamily());
            current.setSubfamily(lastEditedSpecimen.getSubfamily());
            current.setTribe(lastEditedSpecimen.getTribe());
            current.setGenus(lastEditedSpecimen.getGenus());
            current.setSpecificEpithet(lastEditedSpecimen.getSpecificEpithet());
            current.setSubspecificEpithet(lastEditedSpecimen.getSubspecificEpithet());
            current.setInfraspecificEpithet(lastEditedSpecimen.getInfraspecificEpithet());
            current.setInfraspecificRank(lastEditedSpecimen.getInfraspecificRank());
            current.setAuthorship(lastEditedSpecimen.getAuthorship());
            specimenFacade.replaceDeterminationCollection(current, lastEditedSpecimen.getDeterminationCollection());
			
			specimenFacade.trackChange(current);
		}
	}
	
	/**
	 * Paste into the current record values of the identification
	 * fields from the undo state, used to undo pasteTaxonFromPrevious()
	 * @see pasteTaxonFromPrevious
	 */
	public void pasteTaxonFromUndo() { 
		if (this.undoState!=null) { 
            current.setFamily(undoState.getFamily());
            current.setSubfamily(undoState.getSubfamily());
            current.setTribe(undoState.getTribe());
            current.setGenus(undoState.getGenus());
            current.setSpecificEpithet(undoState.getSpecificEpithet());
            current.setSubspecificEpithet(undoState.getSubspecificEpithet());
            current.setInfraspecificEpithet(undoState.getInfraspecificEpithet());
            current.setInfraspecificRank(undoState.getInfraspecificRank());
            current.setAuthorship(undoState.getAuthorship());            
            specimenFacade.replaceDeterminationCollection(current, undoState.getDeterminationCollection());
			
			specimenFacade.trackChange(current);
		}
	}	
	
	public void pasteAllFromPrevious() { 
		logger.log(Level.INFO, "pasteAllFromPrevious start");
		undoState = specimenFacade.cloneDetatch(current);
		if (this.lastEditedSpecimen!=null) { 
			current.setVerbatimLocality(lastEditedSpecimen.getVerbatimLocality());
			current.setMinimum_elevation(lastEditedSpecimen.getMinimum_elevation());
			current.setMaximum_elevation(lastEditedSpecimen.getMaximum_elevation());
			current.setElev_units(lastEditedSpecimen.getElev_units());
			current.setHigherGeography(lastEditedSpecimen.getHigherGeography());
			current.setCountry(lastEditedSpecimen.getCountry());
			current.setPrimaryDivison(lastEditedSpecimen.getPrimaryDivison());
			current.setSpecificLocality(lastEditedSpecimen.getSpecificLocality());
			current.setVerbatimCollector(lastEditedSpecimen.getVerbatimCollector());
			current.setVerbatimCollection(lastEditedSpecimen.getVerbatimCollection());
			current.setVerbatimNumbers(lastEditedSpecimen.getVerbatimNumbers());
			current.setVerbatimUnclassifiedText(lastEditedSpecimen.getVerbatimUnclassifiedText());				
			current.setDateCollected(lastEditedSpecimen.getDateCollected());
			current.setDateEmerged(lastEditedSpecimen.getDateEmerged());
			current.setDateNOS(lastEditedSpecimen.getDateNOS());
			current.setISODate(lastEditedSpecimen.getISODate());
			current.setDateCollectedIndicator(lastEditedSpecimen.getDateCollectedIndicator());
			current.setDateEmergedIndicator(lastEditedSpecimen.getDateEmergedIndicator());
			current.setCollection(lastEditedSpecimen.getCollection());
			current.setCollectingMethod(lastEditedSpecimen.getCollectingMethod());
			current.setGeoreferenceValues(lastEditedSpecimen.getGeoreference());
			specimenFacade.replaceCollectorCollection(current,lastEditedSpecimen.getCollectorCollection());
			
			current.setAssociatedTaxon(lastEditedSpecimen.getAssociatedTaxon());
			current.setCitedInPublication(lastEditedSpecimen.getCitedInPublication());
			current.setDrawerNumber(lastEditedSpecimen.getDrawerNumber());
            current.setFeatures(lastEditedSpecimen.getFeatures());
            current.setHabitat(lastEditedSpecimen.getHabitat());
            current.setMicrohabitat(lastEditedSpecimen.getMicrohabitat());
            current.setInferences(lastEditedSpecimen.getInferences());
            current.setLifeStage(lastEditedSpecimen.getLifeStage());
            current.setLocationInCollection(lastEditedSpecimen.getLocationInCollection());
            specimenFacade.replaceOtherNumbersCollection(current, lastEditedSpecimen.getOtherNumbersCollection());
            current.setSex(lastEditedSpecimen.getSex());
            current.setSpecimenNotes(lastEditedSpecimen.getSpecimenNotes());
            current.setQuestions(lastEditedSpecimen.getQuestions());
            current.setValidDistributionFlag(lastEditedSpecimen.getValidDistributionFlag());
            
            current.setFamily(lastEditedSpecimen.getFamily());
            current.setSubfamily(lastEditedSpecimen.getSubfamily());
            current.setTribe(lastEditedSpecimen.getTribe());
            current.setGenus(lastEditedSpecimen.getGenus());
            current.setSpecificEpithet(lastEditedSpecimen.getSpecificEpithet());
            current.setSubspecificEpithet(lastEditedSpecimen.getSubspecificEpithet());
            current.setInfraspecificEpithet(lastEditedSpecimen.getInfraspecificEpithet());
            current.setInfraspecificRank(lastEditedSpecimen.getInfraspecificRank());
            current.setAuthorship(lastEditedSpecimen.getAuthorship());
            specimenFacade.replaceDeterminationCollection(current, lastEditedSpecimen.getDeterminationCollection());
            
			specimenFacade.trackChange(current);
		}
		logger.log(Level.INFO, "pasteAllFromPrevious done.");
	}
	
	public void pasteAllFromUndo() { 
		if (this.undoState!=null) { 
			current.setVerbatimLocality(undoState.getVerbatimLocality());
			current.setMinimum_elevation(undoState.getMinimum_elevation());
			current.setMaximum_elevation(undoState.getMaximum_elevation());
			current.setElev_units(undoState.getElev_units());
			current.setHigherGeography(undoState.getHigherGeography());
			current.setCountry(undoState.getCountry());
			current.setPrimaryDivison(undoState.getPrimaryDivison());
			current.setSpecificLocality(undoState.getSpecificLocality());
			current.setVerbatimCollector(undoState.getVerbatimCollector());
			current.setVerbatimCollection(undoState.getVerbatimCollection());
			current.setVerbatimNumbers(undoState.getVerbatimNumbers());
			current.setVerbatimUnclassifiedText(undoState.getVerbatimUnclassifiedText());				
			current.setDateCollected(undoState.getDateCollected());
			current.setDateEmerged(undoState.getDateEmerged());
			current.setDateNOS(undoState.getDateNOS());
			current.setISODate(undoState.getISODate());
			current.setDateCollectedIndicator(undoState.getDateCollectedIndicator());
			current.setDateEmergedIndicator(undoState.getDateEmergedIndicator());
			current.setCollection(undoState.getCollection());
			current.setCollectingMethod(undoState.getCollectingMethod());
			
			current.setGeoreferenceValues(undoState.getGeoreference());
			specimenFacade.replaceCollectorCollection(current,undoState.getCollectorCollection());
			
			current.setAssociatedTaxon(undoState.getAssociatedTaxon());
			current.setCitedInPublication(undoState.getCitedInPublication());
			specimenFacade.replaceDeterminationCollection(current, undoState.getDeterminationCollection());
			current.setDrawerNumber(undoState.getDrawerNumber());
            current.setFeatures(undoState.getFeatures());
            current.setHabitat(undoState.getHabitat());
            current.setMicrohabitat(undoState.getMicrohabitat());
            current.setInferences(undoState.getInferences());
            current.setLifeStage(undoState.getLifeStage());
            current.setLocationInCollection(undoState.getLocationInCollection());
            specimenFacade.replaceOtherNumbersCollection(current, undoState.getOtherNumbersCollection());
            current.setSex(undoState.getSex());
            current.setSpecimenNotes(undoState.getSpecimenNotes());
            current.setQuestions(undoState.getQuestions());
            current.setValidDistributionFlag(undoState.getValidDistributionFlag());			
			
            current.setFamily(undoState.getFamily());
            current.setSubfamily(undoState.getSubfamily());
            current.setTribe(undoState.getTribe());
            current.setGenus(undoState.getGenus());
            current.setSpecificEpithet(undoState.getSpecificEpithet());
            current.setSubspecificEpithet(undoState.getSubspecificEpithet());
            current.setInfraspecificEpithet(undoState.getInfraspecificEpithet());
            current.setInfraspecificRank(undoState.getInfraspecificRank());
            current.setAuthorship(undoState.getAuthorship());            
            specimenFacade.replaceDeterminationCollection(current, undoState.getDeterminationCollection());
            
			specimenFacade.trackChange(current);
		}
	}	
	
	public void doUpdate(Specimen aSpecimen) { 
		logger.log(Level.INFO,"SpecimenController.doUpdate" + aSpecimen.getBarcode());
		getFacade().edit(aSpecimen);
		//FacesContext context = FacesContext.getCurrentInstance();  
        //context.addMessage("doUpdate", new FacesMessage("Successful Save", "Saved " + aSpecimen.getBarcode() + " in state " + aSpecimen.getWorkFlowStatus()));  
	}
	
	public void doRowUpdate(RowEditEvent event) {
		Specimen aSpecimen = ((Specimen)event.getObject());
		logger.log(Level.INFO,"SpecimenController.doRowUpdate" + aSpecimen.getBarcode());
		getFacade().edit(aSpecimen);
		//FacesContext context = FacesContext.getCurrentInstance();  
        //context.addMessage("doUpdate", new FacesMessage("Successful Save", "Saved " + aSpecimen.getBarcode() + " in state " + aSpecimen.getWorkFlowStatus()));  
	}	
	
	public String loadForEdit() {
		try { 
	      	current = getFacade().find(current.getSpecimenId());
		    return "Edit?faces-redirect=true";
		} catch (Exception e) { 
		    logger.log(Level.WARNING,e.getMessage());
			JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
			JsfUtil.addErrorMessage(e, e.getMessage());
			return null;
		}
	}
	
// TODO: Throws exception - tries to add duplicate collector when edit->save->view->edit->save.	
	public String update() {
		logger.log(Level.INFO,"SpecimenController.update" + current.getBarcode());
        // TODO: Moving to next/previous only moves within the current page
		// and when on last specimen on page doesn't go to the next/previous page.
		try {
			if (current.getGeoreference()==null) { 
				LatLong georeference = new LatLong();
				georeference.setSpecimenid(current);
				latLongFacade.create(georeference);
				current.setGeoreference(georeference);
			}
			
// TODO: Throws exception here when there is at least one collector and workflow is edit->save->view->edit->save.	
// TODO: Got exception here on saving another number on save after fill from last (specimen barcode unique constraint violated).			
			getFacade().edit(current);
			
			lastEditedSpecimen = specimenFacade.cloneDetatch(current);
			JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("SpecimenUpdated"));
			if (this.andGoTo.equals("go next")) {
                return prepareEditNext();
			}
			if (this.andGoTo.equals("go back")) {
                return prepareEditPrevious();
			}
			return "View?faces-redirect=true";
		} catch (Exception e) {
			logger.log(Level.FINE,e.getMessage());
			JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
			if ((e.getMessage() !=null && e.getMessage().equals("Transaction aborted") && e.getCause()!=null && e.getCause().getMessage() !=null && e.getCause().getMessage().equals("Transaction marked for rollback"))) {
				if (e.getCause()!=null) { 
			       JsfUtil.addErrorMessage((Exception) e.getCause(), "(2) " +  e.getCause().getMessage());
				}
			}
			if (e.getCause()!=null) {
				if (e.getCause().getCause() !=null) { 
			        JsfUtil.addErrorMessage((Exception) e.getCause().getCause(), "(3) " +  e.getCause().getCause().getMessage());
				}
			}
			return null;
		}
	}
	
	public String updateAsOCR() {
		current.setWorkFlowStatus(WorkFlowStatus.STAGE_0);
		return update();
	}

	public String updateAsTaxonEntered() {
		current.setWorkFlowStatus(WorkFlowStatus.STAGE_1);
		return update();
	}

	public String updateAsVerbatim() {
		current.setWorkFlowStatus(WorkFlowStatus.STAGE_VERBATIM);
		return update();
	}	
	
	public String updateAsVerbatimClassified() {
		current.setWorkFlowStatus(WorkFlowStatus.STAGE_CLASSIFIED);
		return update();
	}	
	
	public String updateAsTextEntered() {
		current.setWorkFlowStatus(WorkFlowStatus.STAGE_2);
		return update();
	}

	public String updateAsSpecialistReviewed() {
		current.setWorkFlowStatus(WorkFlowStatus.STAGE_CLEAN);
		return update();
	}

	public String updateAsQCProblems() {
		current.setWorkFlowStatus(WorkFlowStatus.STAGE_QC_FAIL);
		return update();
	}

	public String updateAsQCReviewed() {
		current.setWorkFlowStatus(WorkFlowStatus.STAGE_QC_PASS);
		return update();
	}

	@RolesAllowed({Users.ROLE_ADMINISTRATOR})
	public String destroy() {
		current = (Specimen) getItems().getRowData();
		currentRowIndex = getItems().getRowIndex();
		selectedItemIndex = getPagination().getPageFirstItem() + getItems().getRowIndex();
		performDestroy();
		recreateModel();
		return "List?faces-redirect=true";
	}

	@RolesAllowed({Users.ROLE_ADMINISTRATOR})
	public String destroyAndView() {
		performDestroy();
		recreateModel();
		updateCurrentItem();
		if (selectedItemIndex >= 0) {
			return "View?faces-redirect=true";
		} else {
			// all items were removed - go back to list
			recreateModel();
			return "List?faces-redirect=true";
		}
	}

	@RolesAllowed({Users.ROLE_ADMINISTRATOR})
	private void performDestroy() {
		try {
			getFacade().remove(current);
			JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("SpecimenDeleted"));
		} catch (Exception e) {
			JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
		}
	}

	private void updateCurrentItem() {
		int count = getFacade().count();
		if (selectedItemIndex >= count) {
			// selected index cannot be bigger than number of items:
			selectedItemIndex = count - 1;
			// go to previous page if last page disappeared:
			if (getPagination().getPageFirstItem() >= count) {
				getPagination().previousPage();
			}
		}
		if (selectedItemIndex >= 0) {
			current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
		}
	}

	public DataModel<Specimen> getItems() {
		if (items == null) {
			items = getPagination().createPageDataModel();
		}
		return items;
	}

	private void recreateModel() {
		items = null;
		// pagination = null;
	}

    //TODO:  Alert if no next record (or page?)
	
	public String next() {
		getPagination().nextPage();
		recreateModel();
		return "List?faces-redirect=true";
	}

	public String previous() {
		getPagination().previousPage();
		recreateModel();
		return "List?faces-redirect=true";
	}
	
	public String last() {
		getPagination().lastPage();
		recreateModel();
		return "List?faces-redirect=true";
	}	
	
	public String first() {
		getPagination().firstPage();
		recreateModel();
		return "List?faces-redirect=true";
	}	

	/** Re-render the same list, but resorted using the current sortBy and filterBy criteria.
	 *
	 * @return faces navigation string, List?faces-redirect=true.
	 */
	public String sameReSort() {
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		//recreateModel();
		return "List?faces-redirect=true";
	}

	public String same() {
		recreateModel();
		return "List?faces-redirect=true";
	}

	public SelectItem[] getItemsAvailableSelectMany() {
		return JsfUtil.getSelectItems(specimenFacade.findAll(), false);
	}

	public SelectItem[] getItemsAvailableSelectOne() {
		return JsfUtil.getSelectItems(specimenFacade.findAll(), true);
	}

	@FacesConverter(forClass = Specimen.class)
	public static class SpecimenControllerConverter implements Converter {

		public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
			if (value == null || value.length() == 0) {
				return null;
			}
			SpecimenController controller = (SpecimenController) facesContext.getApplication().getELResolver().
					getValue(facesContext.getELContext(), null, "specimenController");
			return controller.specimenFacade.find(getKey(value));
		}

		java.lang.Long getKey(String value) {
			java.lang.Long key;
			key = Long.valueOf(value);
			return key;
		}

		String getStringKey(java.lang.Long value) {
			StringBuffer sb = new StringBuffer();
			sb.append(value);
			return sb.toString();
		}

		public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
			if (object == null) {
				return null;
			}
			if (object instanceof Specimen) {
				Specimen o = (Specimen) object;
				return getStringKey(o.getSpecimenId());
			} else {
				throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + SpecimenController.class.getName());
			}
		}
	}

	public boolean isSortByCountry() {
		return sortByCountry;
	}

	public void setSortByCountry(boolean sortByCountry) {
		this.sortByCountry = sortByCountry;
	}
	
	public boolean isSortByPrimaryDivision() {
		return sortByPrimaryDivision;
	}

	public void setSortByPrimaryDivision(boolean sortByPrimaryDivision) {
		this.sortByPrimaryDivision = sortByPrimaryDivision;
	}	
	
	public boolean isSortByHigherGeography() {
		return sortByHigherGeography;
	}

	public void setSortByHigherGeography(boolean sortByHigherGeography) {
		this.sortByHigherGeography = sortByHigherGeography;
	}	

	public boolean isSortByBarcode() {
		return sortByBarcode;
	}

	public void setSortByBarcode(boolean sortByBarcode) {
		this.sortByBarcode = sortByBarcode;
	}

	public boolean isSortByFamily() {
		return sortByFamily;
	}

	public void setSortByFamily(boolean sortByFamily) {
		this.sortByFamily = sortByFamily;
	}

	public boolean isSortByGenus() {
		return sortByGenus;
	}

	public void setSortByGenus(boolean sortByGenus) {
		this.sortByGenus = sortByGenus;
	}

	public boolean isSortBySpecificEpithet() {
		return sortBySpecificEpithet;
	}

	public void setSortBySpecificEpithet(boolean sortBySpecificEpithet) {
		this.sortBySpecificEpithet = sortBySpecificEpithet;
	}

	public boolean isSortBySubfamily() {
		return sortBySubfamily;
	}

	public void setSortBySubfamily(boolean sortBySubfamily) {
		this.sortBySubfamily = sortBySubfamily;
	}

	public boolean isSortByWorkflowStatus() {
		return sortByWorkflowStatus;
	}

	public void setSortByWorkflowStatus(boolean sortByWorkflowStatus) {
		this.sortByWorkflowStatus = sortByWorkflowStatus;
	}
	
	public boolean isSortByFilename() {
		return sortByFilename;
	}

	public void setSortByFilename(boolean sortByFilename) {
		this.sortByFilename = sortByFilename;
	}	

	public String getBarcodeFilterCriterion() {
		return barcodeFilterCriterion;
	}

	public void setBarcodeFilterCriterion(String barcodeFilterCriterion) {
		this.barcodeFilterCriterion = barcodeFilterCriterion;
	}

	public String getBarcodesFilterCriterion() {
		if (barcodesFilterCriterion!=null) {
		    return barcodesFilterCriterion.trim();
		} else {
			barcodesFilterCriterion = "";
			return barcodesFilterCriterion;
		}
	}

	public void setBarcodesFilterCriterion(String barcodesFilterCriterion) {
		if (barcodesFilterCriterion!=null) {
			this.barcodesFilterCriterion = barcodesFilterCriterion.trim();
		} else {
		    this.barcodesFilterCriterion = "";
		}
	}        

	public String getGenusFilterCriterion() {
		return genusFilterCriterion;
	}

	public void setGenusFilterCriterion(String genusFilterCriterion) {
		this.genusFilterCriterion = genusFilterCriterion;
	}

	public String getCollectionFilterCriterion() {
		return collectionFilterCriterion;
	}

	public void setCollectionFilterCriterion(String collectionFilterCriterion) {
		this.collectionFilterCriterion = collectionFilterCriterion;
	}

	public String getLastupdatedbyFilterCriterion() {
		return lastupdatedbyFilterCriterion;
	}

	public void setLastupdatedbyFilterCriterion(String lastupdatedbyFilterCriterion) {
		this.lastupdatedbyFilterCriterion = lastupdatedbyFilterCriterion;
	}

	public String getCountryFilterCriterion() {
		return countryFilterCriterion;
	}

	public void setCountryFilterCriterion(String countryFilterCriterion) {
		this.countryFilterCriterion = countryFilterCriterion;
	}
	
	public String getHigherGeographyFilterCriterion() {
		return higherGeographyFilterCriterion;
	}

	public void setHigherGeographyFilterCriterion(String higherGeographyFilterCriterion) {
		if (higherGeographyFilterCriterion!=null && higherGeographyFilterCriterion.trim().length()>0) { 
			if (!higherGeographyFilterCriterion.startsWith("%")) { 
				higherGeographyFilterCriterion = "%" + higherGeographyFilterCriterion;
			}
			if (!higherGeographyFilterCriterion.endsWith("%")) { 
				higherGeographyFilterCriterion = higherGeographyFilterCriterion + "%";
			}			
		}
		this.higherGeographyFilterCriterion = higherGeographyFilterCriterion;
	}	

	public String getFamilyFilterCriterion() {
		return familyFilterCriterion;
	}

	public void setFamilyFilterCriterion(String familyFilterCriterion) {
		this.familyFilterCriterion = familyFilterCriterion;
	}

	public String getPrimarydivisionFilterCriterion() {
		return primarydivisionFilterCriterion;
	}

	public void setPrimarydivisionFilterCriterion(String primarydivisionFilterCriterion) {
		this.primarydivisionFilterCriterion = primarydivisionFilterCriterion;
	}

	public String getSpecificepithetFilterCriterion() {
		return specificepithetFilterCriterion;
	}

	public void setSpecificepithetFilterCriterion(String specificepithetFilterCriterion) {
		this.specificepithetFilterCriterion = specificepithetFilterCriterion;
	}

	public String getSubspecificepithetFilterCriterion() {
		return subspecificepithetFilterCriterion;
	}

	public void setSubspecificepithetFilterCriterion(String subspecificepithetFilterCriterion) {
		this.subspecificepithetFilterCriterion = subspecificepithetFilterCriterion;
	}

	public String getCollectorFilterCriterion() {
		return collectorFilterCriterion;
	}

	public void setCollectorFilterCriterion(String collectorFilterCriterion) {
		this.collectorFilterCriterion = collectorFilterCriterion;
	}

	/**
	 * @return the pathFilterCriterion
	 */
	public String getPathFilterCriterion() {
		return pathFilterCriterion;
	}

	/**
	 * @param pathFilterCriterion the pathFilterCriterion to set
	 */
	public void setPathFilterCriterion(String pathFilterCriterion) {
		if (pathFilterCriterion==null) { 
			this.pathFilterCriterion = pathFilterCriterion;
		} else { 
			if (pathFilterCriterion.equals(Image.BLANKPATHFILTER)) { 
				this.pathFilterCriterion = ""; 
			} else { 
				this.pathFilterCriterion = pathFilterCriterion.trim();
			}
		}
	}

	public String worflowstatusAsOCR() {
		setSortOff();
		setWorkflowstatusFilterCriterion(WorkFlowStatus.STAGE_0);
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "/lepidoptera/specimen/List?faces-redirect=true";
	}

	public String worflowstatusAsTaxonEntered() {
		setSortOff();
		setWorkflowstatusFilterCriterion(WorkFlowStatus.STAGE_1);
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "/lepidoptera/specimen/List?faces-redirect=true";
	}

	public String worflowstatusAsTextEntered() {
		setSortOff();
		setWorkflowstatusFilterCriterion(WorkFlowStatus.STAGE_2);
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "/lepidoptera/specimen/List?faces-redirect=true";
	}

	public String worflowstatusAsQCFail() {
		setSortOff();
		setWorkflowstatusFilterCriterion(WorkFlowStatus.STAGE_QC_FAIL);
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "/lepidoptera/specimen/List?faces-redirect=true";
	}

	public String worflowstatusAsQCPass() {
		setSortOff();
		setWorkflowstatusFilterCriterion(WorkFlowStatus.STAGE_QC_PASS);
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "/lepidoptera/specimen/List?faces-redirect=true";
	}

	public String getWorkflowstatusFilterCriterion() {
		return workflowstatusFilterCriterion;
	}

	public void setWorkflowstatusFilterCriterion(String workflowstatusFilterCriterion) {
		this.workflowstatusFilterCriterion = workflowstatusFilterCriterion;
	}

	public String getTemp_Number() {
		return temp_Number;
	}

	public void setTemp_Number(String temp_Number) {
		this.temp_Number = temp_Number;
	}

	public String getTemp_NumberType() {
		if (temp_NumberType==null) { temp_NumberType = ""; } 
		return temp_NumberType;
	}

	public void setTemp_NumberType(String temp_NumberType) {
		this.temp_NumberType = temp_NumberType;
	}

	public String getTemp_genus() {
		return temp_genus;
	}

	public void setTemp_genus(String temp_Genus) {
		this.temp_genus = temp_Genus;
	}

	public String getTemp_specificEpithet() {
		return temp_specificEpithet;
	}

	public void setTemp_specificEpithet(String temp_specificEpithet) {
		this.temp_specificEpithet = temp_specificEpithet;
	}

	public String getTemp_authorship() {
		return temp_authorship;
	}

	public void setTemp_authorship(String temp_authorship) {
		this.temp_authorship = temp_authorship;
	}

	public String getTemp_identifiedBy() {
		return temp_identifiedBy;
	}

	public void setTemp_identifiedBy(String temp_identifiedBy) {
		this.temp_identifiedBy = temp_identifiedBy;
	}

	public String getTemp_infraspecificEpithet() {
		return temp_infraspecificEpithet;
	}

	public void setTemp_infraspecificEpithet(String temp_infraspecificEpithet) {
		this.temp_infraspecificEpithet = temp_infraspecificEpithet;
	}

	public String getTemp_infraspecificRank() {
		return temp_infraspecificRank;
	}

	public void setTemp_infraspecificRank(String temp_infraspecificRank) {
		this.temp_infraspecificRank = temp_infraspecificRank;
	}

	public String getTemp_speciesNumber() {
		return temp_speciesNumber;
	}

	public void setTemp_speciesNumber(String temp_speciesNumber) {
		this.temp_speciesNumber = temp_speciesNumber;
	}

	public String getTemp_subspecificEpithet() {
		return temp_subspecificEpithet;
	}

	public void setTemp_subspecificEpithet(String temp_subspecificEpithet) {
		this.temp_subspecificEpithet = temp_subspecificEpithet;
	}

	public String getTemp_typeStatus() {
		return temp_typeStatus;
	}

	public void setTemp_typeStatus(String temp_typeStatus) {
		this.temp_typeStatus = temp_typeStatus;
	}

	public String getTemp_unNamedForm() {
		return temp_unNamedForm;
	}

	public void setTemp_unNamedForm(String temp_unNamedForm) {
		this.temp_unNamedForm = temp_unNamedForm;
	}

	public String getTemp_verbatimText() {
		return temp_verbatimText;
	}

	public void setTemp_verbatimText(String temp_verbatimText) {
		this.temp_verbatimText = temp_verbatimText;
	}

	/**
	 * @return the temp_natureOfId
	 */
	public String getTemp_natureOfId() {
		if (temp_natureOfId==null) { 
			temp_natureOfId="expert ID";
		}
		return temp_natureOfId;
	}

	/**
	 * @param temp_natureOfId the temp_natureOfId to set
	 */
	public void setTemp_natureOfId(String temp_natureOfId) {
		this.temp_natureOfId = temp_natureOfId;
	}

	/**
	 * @return the temp_dateIdentified
	 */
	public String getTemp_dateIdentified() {
		return temp_dateIdentified;
	}

	/**
	 * @param temp_dateIdentified the temp_dateIdentified to set
	 */
	public void setTemp_dateIdentified(String temp_dateIdentified) {
		this.temp_dateIdentified = temp_dateIdentified;
	}

	/**
	 * @return the temp_remarks
	 */
	public String getTemp_remarks() {
		return temp_remarks;
	}

	/**
	 * @param temp_remarks the temp_remarks to set
	 */
	public void setTemp_remarks(String temp_remarks) {
		this.temp_remarks = temp_remarks;
	}

	public String getTemp_CollectorName() {
		return temp_CollectorName;
	}

	public void setTemp_CollectorName(String temp_CollectorName) {
		this.temp_CollectorName = temp_CollectorName;
	}

	public Determination getTemp_CurrentEditIdentification() {
		if (temp_CurrentEditIdentification == null) {
			// as the JSF forms invoke #{current.temp_CurrentEditIdentification.genus}
			// we need to make sure that a non-null value is returned.
			temp_CurrentEditIdentification = new Determination();
		}
		return temp_CurrentEditIdentification;
	}

	public void setTemp_CurrentEditIdentification(Determination temp_CurrentEditIdentification) {
		this.temp_CurrentEditIdentification = temp_CurrentEditIdentification;
	}

	public void updateCurrentEditIdentification() {
		if (current.getDeterminationCollection().contains(temp_CurrentEditIdentification)) {
			current.getDeterminationCollection().remove(temp_CurrentEditIdentification);
		}
		current.getDeterminationCollection().add(temp_CurrentEditIdentification);
	}	
	
	/**
	 * @return the temp_CurrentPartAttribute
	 */
	public SpecimenPartAttribute getTemp_CurrentPartAttribute() {
		return temp_CurrentPartAttribute;
	}

	/**
	 * @param temp_CurrentPartAttribute the temp_CurrentPartAttribute to set
	 */
	public void setTemp_CurrentPartAttribute(SpecimenPartAttribute temp_CurrentPartAttribute) {
		this.temp_CurrentPartAttribute = temp_CurrentPartAttribute;
	}

	public void updateCurrentPartAttribute() {
		Iterator<SpecimenPart> i = current.getPartCollection().iterator();
		while (i.hasNext()) {
			SpecimenPart sp = i.next();
		    if (sp.getAttributeCollection().contains(temp_CurrentPartAttribute)) {
			    sp.getAttributeCollection().remove(temp_CurrentPartAttribute);
		        sp.getAttributeCollection().add(temp_CurrentPartAttribute);
		    }
		}
	}
	public void removeFromCurrentPartAttribute() {
		Iterator<SpecimenPart> i = current.getPartCollection().iterator();
		while (i.hasNext()) {
			SpecimenPart sp = i.next();
		    if (sp.getAttributeCollection().contains(temp_CurrentPartAttribute)) {
			    sp.getAttributeCollection().remove(temp_CurrentPartAttribute);
			    partAttributeFacade.remove(temp_CurrentPartAttribute);
		    }
		}
	}	
	
	public void updateGeoreference() {
		// not needed?
	}	
	
	/**
	 * Stamps the MCZbase standard "[no specific locality data]"
	 * into specific locality for the current Specimen.
	 */
	public void noSpecificLocalityData() { 
		current.setSpecificLocality("[no specific locality data]");
	}

	/** Rebuild the page model and page items with a sort of the
	 * items on barcode.
	 *
	 * @return a faces navigation string, List?faces-redirect=true.
	 */
	public String sameReSortOnBarcode() {
		setSortOff();
		setSortByBarcode(true);
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "List?faces-redirect=true";
	}

	public String sameReSortOnSpecificEpithet() {
		setSortOff();
		setSortBySpecificEpithet(true);
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "List?faces-redirect=true";
	}

	/**  Sets the sortBy_ variables to their default values.
	 *
	 */
	private void setDefaultSort() {
		sortByBarcode = false;
		sortByFamily = true;
		sortBySubfamily = true;
		sortByGenus = true;
		sortBySpecificEpithet = true;
		sortByCountry = true;
		sortByHigherGeography = true;
		sortByWorkflowStatus = true;
		sortByFilename = true;
	}

	/** Sets all the sortBy_ variables to false, removing any
	 * sort criteria.
	 */
	private void setSortOff() {
		sortByBarcode = false;
		sortByFamily = false;
		sortBySubfamily = false;
		sortByGenus = false;
		sortBySpecificEpithet = false;
		sortByCountry = false;
		sortByHigherGeography = false;
		sortByWorkflowStatus = false;
		sortByFilename = false;
	}

	public List<String> autocompleteCollection(String suggest) {
		ArrayList<String> result = new ArrayList<String>();
		List<String> taxa = specimenFacade.getCollectionValues();
		Iterator<String> i = taxa.iterator();
		String last = "";
		while (i.hasNext()) {
			String candidate = i.next();
			if (!last.equals(candidate)) {
				if (candidate.startsWith(suggest)) {
					result.add(candidate);
				}
				last = candidate;
			}
		}
		return result;
	}

	public String getAndGoTo() {
		return andGoTo;
	}

	public void setAndGoTo(String andGoTo) {
        // TODO: Moving to next/previous only moves within the current page
		// and when on last specimen on page doesn't go to the next/previous page.

		this.andGoTo = andGoTo;
	}

	public String getIconForCollectorsInMCZbase() {
		String result = "upload_problem.png";
        if (specimenFacade.isCollectorsInMCZbase(current.getSpecimenId())) {
			result = "upload_ok.png";
		}
		return result;
	}

	public boolean isHasCollectors() {
		boolean result = false;
        if (current.getCollectorCollection().size()>0) {
			result = true;
		}
		return result;
	}
	
	public boolean isStateOCRSelected() { 
		return current.isStateOCR();
	}
	
	/**
	 * Do nothing.
	 * Support for p:panel collapsed="${specimenController.stateOCRSelected}"
	 * Requires a boolean with is() and set() methods.  
	 */
	public void setStateOCRSelected(boolean ignored) { 
		// Do nothing.
	}
	
	public void addCasteAttributeToSpecimenPart(SpecimenPart part) { 
		String username = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		Users user = userBean.findByName(username);		
		part.addCasteAttribute(user.getFullname());
	}

	
	public List<String> autocompleteHigherGeography(String suggest) {
		ArrayList<String> result = new ArrayList<String>();
		if (suggest!=null) {
			Map<String, String> filters = null;
			// Haven't saved yet to current object, so get the values from the form submission
			String country = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("mainForm:country"); 
			String primary = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("mainForm:primaryDivison"); 
			if (primary!=null && primary.length()>0) { 
			    filters = new HashMap<String, String>();
			    // NOTE: state_prov is the field name in the geographic authority file, not primaryDivision
			    filters.put("state_prov", primary);
			    logger.log(Level.INFO, primary);
			} else if ( country!=null && country.length()>0) {
			    filters = new HashMap<String, String>();
			    filters.put("country", country);
			    logger.log(Level.INFO, country);
			}
			if (!suggest.isEmpty()) { 
				if (filters==null) { 
			        filters = new HashMap<String, String>();
				} 
			    filters.put("higher_geog", "%" + suggest + "%");
			    logger.log(Level.INFO, current.getCountry());
			}
			List<MCZbaseGeogAuthRec> geographies = higherGeogFacade.findHigherGeographies(filters);
			//TODO: Switch picklist to using geography objects instead of strings
			Iterator<MCZbaseGeogAuthRec> i = geographies.iterator();
			String last = "";
			while (i.hasNext()) {
				String candidate = i.next().getHigher_geog();
				if (!last.equals(candidate)) {
					if (candidate!=null) { 
						//if (candidate.toUpperCase().contains(suggest.toUpperCase())) {
							result.add(candidate);
						//}
						last = candidate;
					}
				}
			}
		}
        return result;
	}	
	
	public List<String> autocompleteAgent(String suggest) {
		ArrayList<String> result = new ArrayList<String>();
		if (suggest!=null) {
			Map<String, String> filters = null;
			if (!suggest.isEmpty()) { 
				if (filters==null) { 
			        filters = new HashMap<String, String>();
				} 
			    filters.put("agent_name", "%" + suggest + "%");
			}
			List<MCZbaseAuthAgentName> agentNames = agentNameFacade.findAgents(filters, 50);
			Iterator<MCZbaseAuthAgentName> i = agentNames.iterator();
			String last = "";
			while (i.hasNext()) {
				String candidate = i.next().getAgent_name();
				if (!last.equals(candidate)) {
					if (candidate!=null) { 
						result.add(candidate);
						last = candidate;
					}
				}
			}
		}
        return result;
	}	
	
	/**
	 * Used to determine if part attributes should be shown on UI.  Use with
	 * rendered attribute to turn on or off UI elements related to part attributes.
	 * 
	 * @return true if the current specimen record has a higher taxon that
	 * uses part attributes (e.g. castes), otherwise return false.  
	 * 
	 */
	public boolean isTaxonWithPartAttributes() { 
		return higherTaxonFacade.isFamilyWithCastes(current.getFamily());
	}

	public boolean isPasteAll() { 
		return isTaxonWithPartAttributes();
	}
	
	public boolean isShowDateEmerged() { 
		return !isTaxonWithPartAttributes();
	}
	
	public String iconForState(String state) { 
		String result = null;
		if (state.equals(current.getWorkFlowStatus())) { 
			result = "ui-icon-pin-s";
		}
		return result;
	}
	
}