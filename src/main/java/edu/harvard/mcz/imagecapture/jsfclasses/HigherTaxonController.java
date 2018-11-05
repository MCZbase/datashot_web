package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.data.HigherTaxon;
import edu.harvard.mcz.imagecapture.jsfclasses.util.JsfUtil;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.ejb.HigherTaxonFacadeLocal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Named;

@Named("higherTaxonController")
//@ManagedBean
@SessionScoped
public class HigherTaxonController implements Serializable {

	private static final long serialVersionUID = 7845868507003451156L;
	
	private HigherTaxon current;
    private DataModel items = null;
    @EJB private edu.harvard.mcz.imagecapture.ejb.HigherTaxonFacadeLocal ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public HigherTaxonController() {
		super();
    }

    public HigherTaxon getSelected() {
        if (current == null) {
            current = new HigherTaxon();
            selectedItemIndex = -1;
        }
        return current;
    }

    private HigherTaxonFacadeLocal getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem()+getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (HigherTaxon)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new HigherTaxon();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("HigherTaxonCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (HigherTaxon)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("HigherTaxonUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (HigherTaxon)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("HigherTaxonDeleted"));
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

    private void recreateModel() {
        items = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    @FacesConverter(forClass=HigherTaxon.class)
    public static class HigherTaxonControllerConverter implements Converter {

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            HigherTaxonController controller = (HigherTaxonController)facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "higherTaxonController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuffer sb = new StringBuffer();
            sb.append(value);
            return sb.toString();
        }

        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof HigherTaxon) {
                HigherTaxon o = (HigherTaxon) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: "+HigherTaxonController.class.getName());
            }
        }

    }

	/**Backing for rich:suggestionBox for autocompletion of family names.
	 *
	 *
	 * <h:inputText id="family_input" candidate="#{specimenControler.family}" />
	 * <rich:suggestionbox for="family_input" var="result"
     *    suggestionAction="#{capitalsBean.autocomplete}">
     *    <h:column>
     *       <h:outputText candidate="#{result}" />
     *    </h:column>
     * </rich:suggestionbox>

	 *
	 * @param suggest
	 * @return list of strings
	 */
	public List<String> autocompleteFamily(String suggest) {
		ArrayList<String> result = new ArrayList<String>();
		List<HigherTaxon> taxa = ejbFacade.findAll();
		Iterator<HigherTaxon> i = taxa.iterator();
		String last = "";
		while (i.hasNext()) {
			String candidate = i.next().getFamily();
			if (!last.equals(candidate)) {
			   if (candidate.toUpperCase().startsWith(suggest.toUpperCase())) {
			      result.add(candidate);
			   }
			   last = candidate;
			}
		}
        return (List<String>)result;
/*
                    <h:inputText id="family" candidate="#{specimenController.selected.family}" title="#{bundle.EditSpecimenTitle_family}" />
					<rich:suggestionbox for="family" var="result"
                           suggestionAction="#{higherTaxonController.autocompleteFamily}">
                            <h:column>
                               <h:outputText candidate="#{result}" />
                            </h:column>
					</rich:suggestionbox>

 */

	}
	public List<String> autocompleteSubfamily(String suggest) {
		ArrayList<String> result = new ArrayList<String>();
		List<HigherTaxon> taxa = ejbFacade.findAll();
		Iterator<HigherTaxon> i = taxa.iterator();
		String last = "";
		while (i.hasNext()) {
			String candidate = i.next().getSubfamily();
			if (!last.equals(candidate)) {
				if (candidate.toUpperCase().startsWith(suggest.toUpperCase())) {
			        result.add(candidate);
				}
			    last = candidate;
			}
		}
        return result;
	}

	public List<String> autocompleteTribe(String suggest) {
		ArrayList<String> result = new ArrayList<String>();
		List<HigherTaxon> taxa = ejbFacade.findAll();
		Iterator<HigherTaxon> i = taxa.iterator();
		String last = "";
		while (i.hasNext()) {
			String candidate = i.next().getTribe();
			if (!last.equals(candidate)) {
				if (candidate.toUpperCase().startsWith(suggest.toUpperCase())) {
			    result.add(candidate);
				}
				last = candidate;
			}
		}
        return result;
	}
}
