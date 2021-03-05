/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.ejb.SpecimenFacadeLocal;
import edu.harvard.mcz.imagecapture.interfaces.CountValueChangeListener;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.utility.CountValue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.context.FacesContext;
import jakarta.ejb.EJB;
import jakarta.faces.bean.SessionScoped;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.inject.Named;

/**
 *
 * @author mole
 */
//@Named("countryQCBean")
@ManagedBean
@SessionScoped
public class CountryQCBean implements CountValueChangeListener, Serializable{

	private static final long serialVersionUID = -4748745400731692063L;

	private final static Logger logger = Logger.getLogger(CountryQCBean.class.getName());

	@EJB
	private SpecimenFacadeLocal specimenFacade;

	private PaginationHelper pagination;
    private DataModel<CountValue> countryFrequencyItems = null;

    private boolean sortByFrequency;
	private String countryFilterCriterion;

    /** Creates a new instance of CollectorQCBean */
    public CountryQCBean() {
        super();
		setDefaultSort();
		logger.log(Level.INFO, "Sort by frequency (constructor): " + sortByFrequency);
    }


	public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(50) {

                @Override
                public int getItemsCount() {
                    return specimenFacade.getCountryValuesCount();
                }

                @Override
                public DataModel createPageDataModel() {
		            logger.log(Level.INFO, "Creating page data model");
					boolean useFrequency = false;
					if (sortByFrequency) {
						useFrequency = true;
					}
		            logger.log(Level.INFO, "Sort by frequency (create page data model): " + useFrequency);
                    return new ListDataModel(specimenFacade.getCountryValuesRange(getPageFirstItem(), getPageFirstItem()+getPageSize(), useFrequency, countryFilterCriterion));
                }
            };
        }
        return pagination;
    }

	private void recreateModel() {
		if (countryFrequencyItems != null) {
			Iterator<CountValue> i = countryFrequencyItems.iterator();
			while (i.hasNext()) {
				i.next().unregisterListener(this);
			}
		}
		countryFrequencyItems = null;
	}

	public String next() {
		getPagination().nextPage();
		recreateModel();
		return "CountryQCList?faces-redirect=true";
	}

	public String previous() {
		getPagination().previousPage();
		recreateModel();
		return "CountryQCList?faces-redirect=true";
	}

	/** Re-render the same list, but resorted using the current sortBy and filterBy criteria.
	 *
	 * @return faces navigation string, List?faces-redirect=true.
	 */
	public String sameReSort() {
		pagination = null;
		recreateModel();
		getPagination().createPageDataModel();
		return "CountryQCList?faces-redirect=true";
	}

	private void setDefaultSort() {
		this.sortByFrequency = false;
	}

	public DataModel<CountValue> getCountryFrequencyItems() {
		if (countryFrequencyItems == null) {
			countryFrequencyItems = getPagination().createPageDataModel();
			Iterator<CountValue> i =countryFrequencyItems.iterator();
			while (i.hasNext()) {
				i.next().registerListener(this);
			}

		}
		return countryFrequencyItems;
	}

	public String prepareSaveChange() {
		return "CountryQCList&faces-redirect=true";
	}

	public String prepareListAll() {
		pagination = null;
		recreateModel();
		resetFilters();
		getPagination().createPageDataModel();
		return "CountryQCList?faces-redirect=true";
	}

	public String getCountryFilterCriterion() {
		return countryFilterCriterion;
	}

	public void setCountryFilterCriterion(String countryFilterCriterion) {
		this.countryFilterCriterion = countryFilterCriterion;
	}

	public boolean isSortByFrequency() {
		return sortByFrequency;
	}

	public void setSortByFrequency(boolean sortByFrequency) {
		this.sortByFrequency = sortByFrequency;
	}

    private void resetFilters() {
		sortByFrequency = false;
		countryFilterCriterion = "";
	}

	public void notify(String oldValue, String newValue) {
		logger.log(Level.INFO, "Changing" + oldValue + " to " + newValue);
        if (!oldValue.equals(newValue))  {
             int changed = specimenFacade.updateAllCountries(oldValue, newValue);
			 logger.log(Level.INFO, "Changed: " + changed);
			 
			 FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Changed", Integer.toString(changed));  
		     FacesContext.getCurrentInstance().addMessage(null, msg);  
		}
	}

}
