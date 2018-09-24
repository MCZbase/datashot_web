/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.ejb.SpecimenFacadeLocal;
import edu.harvard.mcz.imagecapture.interfaces.CountValueValueChangeListener;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.utility.CountValueValue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Named;

/**
 *
 * @author mole
 */
//@Named("countryStateQCBean")
@ManagedBean
@SessionScoped
public class CountryStateQCBean implements CountValueValueChangeListener, Serializable {

	private static final long serialVersionUID = -4090938819856849875L;

	private final static Logger logger = Logger.getLogger(CountryStateQCBean.class.getName());

	@EJB
	private SpecimenFacadeLocal specimenFacade;

	private PaginationHelper pagination;
    private DataModel<CountValueValue> countryStateFrequencyItems = null;

    private boolean sortByFrequency;
	private String countryFilterCriterion;
	private String primaryFilterCriterion;  // filter condition on primaryDivison [sic]

    /** Creates a new instance of CollectorQCBean */
    public CountryStateQCBean() {
        super();
		setDefaultSort();
		logger.log(Level.INFO, "Sort by frequency (constructor): " + sortByFrequency);
    }


	public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(50) {

                @Override
                public int getItemsCount() {
                    return specimenFacade.getCountryStateValuesCount();
                }

                @Override
                public DataModel createPageDataModel() {
		            logger.log(Level.INFO, "Creating page data model");
					boolean useFrequency = false;
					if (sortByFrequency) {
						useFrequency = true;
					}
		            logger.log(Level.INFO, "Sort by frequency (create page data model): " + useFrequency);
                    return new ListDataModel(specimenFacade.getCountryStateValuesRange(getPageFirstItem(), getPageFirstItem()+getPageSize(), useFrequency, primaryFilterCriterion, countryFilterCriterion));
                }
            };
        }
        return pagination;
    }

	private void recreateModel() {
		if (countryStateFrequencyItems != null) {
			Iterator<CountValueValue> i = countryStateFrequencyItems.iterator();
			while (i.hasNext()) {
				i.next().unregisterListener(this);
			}
		}
		countryStateFrequencyItems = null;
	}

	public String next() {
		getPagination().nextPage();
		recreateModel();
		return "CountryStateQCList?faces-redirect=true";
	}

	public String previous() {
		getPagination().previousPage();
		recreateModel();
		return "CountryStateQCList?faces-redirect=true";
	}

	/** Re-render the same list, but resorted using the current sortBy and filterBy criteria.
	 *
	 * @return faces navigation string, List?faces-redirect=true.
	 */
	public String sameReSort() {
		pagination = null;
		recreateModel();
		getPagination().createPageDataModel();
		return "CountryStateQCList?faces-redirect=true";
	}

	private void setDefaultSort() {
		this.sortByFrequency = false;
	}

	public DataModel<CountValueValue> getCountryStateFrequencyItems() {
		if (countryStateFrequencyItems == null) {
			countryStateFrequencyItems = getPagination().createPageDataModel();
			Iterator<CountValueValue> i =countryStateFrequencyItems.iterator();
			while (i.hasNext()) {
				i.next().registerListener(this);
			}

		}
		return countryStateFrequencyItems;
	}

	public String prepareSaveChange() {
		return "CountryStateQCList&faces-redirect=true";
	}

	public String prepareListAll() {
		pagination = null;
		recreateModel();
		resetFilters();
		getPagination().createPageDataModel();
		return "CountryStateQCList?faces-redirect=true";
	}

	public String getCountryFilterCriterion() {
		return countryFilterCriterion;
	}

	public void setCountryFilterCriterion(String countryFilterCriterion) {
		this.countryFilterCriterion = countryFilterCriterion;
	}

	public String getPrimaryFilterCriterion() {
		return primaryFilterCriterion;
	}

	public void setPrimaryFilterCriterion(String primaryFilterCriterion) {
		this.primaryFilterCriterion = primaryFilterCriterion;
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
		primaryFilterCriterion = "";
	}

	// TODO: May need explicit inclusion of country
	public void notify(String oldValue, String newValue, String country) {
		logger.log(Level.INFO, "Changing" + oldValue + " to " + newValue + " where country = " + country );
        if (!oldValue.equals(newValue))  {
             int changed = specimenFacade.updateAllStates(country, oldValue, newValue);
			 logger.log(Level.INFO, "Changed: " + changed);
		}
	}

    public void changeAction(ValueChangeEvent event) {
		logger.log(Level.INFO, "Event fired");
		//((UIDataTable)event.getComponent().getParent().getParent()).getRowData();
	}

}
