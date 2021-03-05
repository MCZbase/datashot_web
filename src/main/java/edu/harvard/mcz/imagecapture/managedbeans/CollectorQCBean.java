/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.ejb.CollectorFacadeLocal;
import edu.harvard.mcz.imagecapture.interfaces.CountValueChangeListener;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.utility.CountValue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.faces.bean.SessionScoped;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.inject.Named;

/**
 *
 * @author mole
 */
//@Named("collectorQCBean")
@ManagedBean
@SessionScoped
public class CollectorQCBean implements CountValueChangeListener, Serializable {

	private static final long serialVersionUID = -4910426720692522588L;

	private final static Logger logger = Logger.getLogger(CollectorQCBean.class.getName());

	@EJB
	private CollectorFacadeLocal collectorFacade;

	private PaginationHelper pagination;
    private DataModel<CountValue> collectorFrequencyItems = null;

    private boolean sortByFrequency;
	private String collectorFilterCriterion;

    /** Creates a new instance of CollectorQCBean */
    public CollectorQCBean() {
        super();
		setDefaultSort();
		logger.log(Level.INFO, "Sort by frequency (constructor): " + sortByFrequency);
    }


	public List<CountValue> getCollectorFrequency() {
		return collectorFacade.getCollectorValues(this.sortByFrequency, collectorFilterCriterion);
	}

	public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(50) {

                @Override
                public int getItemsCount() {
                    return collectorFacade.getCollectorValuesCount();
                }

                @Override
                public DataModel createPageDataModel() {
		            logger.log(Level.INFO, "Creating page data model");
					boolean useFrequency = false;
					if (sortByFrequency) {
						useFrequency = true;
					}
		            logger.log(Level.INFO, "Sort by frequency (create page data model): " + useFrequency);
                    return new ListDataModel(collectorFacade.getCollectorValuesRange(getPageFirstItem(), getPageFirstItem()+getPageSize(), useFrequency, collectorFilterCriterion));
                }
            };
        }
        return pagination;
    }

	private void recreateModel() {
		if (collectorFrequencyItems != null) {
			Iterator<CountValue> i = collectorFrequencyItems.iterator();
			while (i.hasNext()) {
				i.next().unregisterListener(this);
			}
		}
		collectorFrequencyItems = null;
	}

	public String next() {
		getPagination().nextPage();
		recreateModel();
		return "QCList?faces-redirect=true";
	}

	public String previous() {
		getPagination().previousPage();
		recreateModel();
		return "QCList?faces-redirect=true";
	}

	/** Re-render the same list, but resorted using the current sortBy and filterBy criteria.
	 *
	 * @return faces navigation string, List?faces-redirect=true.
	 */
	public String sameReSort() {
		logger.log(Level.INFO, "Sort by frequency 1: " + sortByFrequency);
		pagination = null;
		recreateModel();
		logger.log(Level.INFO, "Sort by frequency 2: " + sortByFrequency);
		getPagination().createPageDataModel();
		logger.log(Level.INFO, "Sort by frequency 3: " + sortByFrequency);
		return "QCList?faces-redirect=true";
	}

	private void setDefaultSort() {
		this.sortByFrequency = false;
	}

	public DataModel<CountValue> getCollectorFrequencyItems() {
		if (collectorFrequencyItems == null) {
			collectorFrequencyItems = getPagination().createPageDataModel();
			Iterator<CountValue> i =collectorFrequencyItems.iterator();
			while (i.hasNext()) {
				i.next().registerListener(this);
			}

		}
		return collectorFrequencyItems;
	}

	public String prepareSaveChange() {
		return "QCList&faces-redirect=true";
	}

	public String prepareListAll() {
		pagination = null;
		recreateModel();
		resetFilters();
		getPagination().createPageDataModel();
		return "QCList?faces-redirect=true";
	}

	public String getCollectorFilterCriterion() {
		return collectorFilterCriterion;
	}

	public void setCollectorFilterCriterion(String collectorFilterCriterion) {
		this.collectorFilterCriterion = collectorFilterCriterion;
	}

	public boolean isSortByFrequency() {
		return sortByFrequency;
	}

	public void setSortByFrequency(boolean sortByFrequency) {
		this.sortByFrequency = sortByFrequency;
	}

    private void resetFilters() {
		sortByFrequency = false;
		collectorFilterCriterion = "";
	}

	public void notify(String oldValue, String newValue) {
		logger.log(Level.INFO, "Changing" + oldValue + " to " + newValue);
        if (!oldValue.equals(newValue))  {
             collectorFacade.updateAll(oldValue, newValue);
		}
	}
	
}
