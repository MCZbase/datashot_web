package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.data.Label;
import edu.harvard.mcz.imagecapture.ejb.LabelFacadeLocal;
import edu.harvard.mcz.imagecapture.jsfclasses.util.JsfUtil;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Named;

//@Named("labelController")
@ManagedBean
@SessionScoped
public class LabelController  implements Serializable {
	
	private static final long serialVersionUID = 4048819082332430644L;

	private final static Logger logger = Logger.getLogger(LabelController.class.getName());

    private Label current;
    private DataModel items = null;
    @EJB 
	private LabelFacadeLocal ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

	private String searchCriterion;

    public LabelController() {
    }

	public String getSearchCriterion() {
		return searchCriterion;
	}

	public void setSearchCriterion(String searchCriterion) {
		this.searchCriterion = searchCriterion;
	}



    public Label getSelected() {
        if (current == null) {
            current = new Label();
            selectedItemIndex = -1;
        }
        return current;
    }

    private LabelFacadeLocal getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

				public Map<String, String> getFilterMap() {
					Map<String, String> filters = new HashMap<String, String>();
					if (searchCriterion != null) {
						filters.put("verbatimtext", "%" + searchCriterion +"%");
						filters.put("interpretation", "%" + searchCriterion +"%");
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
					Map<String, String> filters = getFilterMap();
					int[] range = new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()};
					ArrayList<String> sortFields = new ArrayList<String>();
					sortFields.add("verbatimtext");
					DataModel	result = new ListDataModel(
								getFacade().findRangeQueryAndOr(
								range, sortFields.toArray(new String[0]), true, filters, false));
					logger.log(Level.INFO, Integer.toString(result.getRowCount()));
                    return result;
					//new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem()+getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List?faces-redirect=true";
    }

    public String prepareView() {
        current = (Label)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View?faces-redirect=true";
    }

    public String prepareCreate() {
        current = new Label();
        selectedItemIndex = -1;
        return "Create?faces-redirect=true";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LabelCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Label)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit?faces-redirect=true";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LabelUpdated"));
            return "View?faces-redirect=true";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Label)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "List?faces-redirect=true";
    }

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

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("LabelDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count-1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex+1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

	/** Re-render the same list, but resorted using the current sortBy and filterBy criteria.
	 *
	 * @return faces navigation string, List?faces-redirect=true.
	 */
	public String sameReSort() {
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "List?faces-redirect=true";
	}

	/** Re-render the same list, but resorted using the current sortBy and filterBy criteria.
	 * Intended for Ajax rather than page reloads.
	 *
	 * @return an empty string.
	 */
	public String sameReSortAjax() {
		logger.log(Level.INFO, "LabelController.sameReSortAjax()");
		logger.log(Level.INFO, getSearchCriterion());
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "";
	}


    private void recreateModel() {
        items = null;
    }

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

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    @FacesConverter(forClass=Label.class)
    public static class LabelControllerConverter implements Converter {

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            LabelController controller = (LabelController)facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "labelController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
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
            if (object instanceof Label) {
                Label o = (Label) object;
                return getStringKey(o.getLabelid());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: "+LabelController.class.getName());
            }
        }

    }

}
