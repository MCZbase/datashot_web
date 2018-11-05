package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.data.Determination;
import edu.harvard.mcz.imagecapture.jsfclasses.util.JsfUtil;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.ejb.DeterminationFacadeLocal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.faces.bean.SessionScoped;
import javax.faces.annotation.ManagedProperty;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Named;

//@Named("determinationController")
@ManagedBean
@SessionScoped
public class DeterminationController implements Serializable {
	private static final long serialVersionUID = -6235127539980043405L;

    private Determination current;
    private DataModel<Determination> items = null;
	private PaginationHelper pagination;
    private int selectedItemIndex;

	@EJB
	private edu.harvard.mcz.imagecapture.ejb.DeterminationFacadeLocal ejbFacade;

	@ManagedProperty(value="#{specimenController}")
	private edu.harvard.mcz.imagecapture.jsfclasses.SpecimenController specimenController;

    private boolean limitByBlankGenus = false;
	private boolean limitByAbbrevGenus = false;
	private String filterGenus;

    public DeterminationController() {
		super();
    }

	/**
	 * Required setter method for ManagedProperty
	 *
	 * @param specimenController
	 */
	public void setSpecimenController(SpecimenController specimenController) {
		this.specimenController = specimenController;
	}

    public Determination getSelected() {
        if (current == null) {
            current = new Determination();
            selectedItemIndex = -1;
        }
        return current;
    }

    private DeterminationFacadeLocal getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(50) {

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
						ArrayList<String> sortFields = new ArrayList<String>();
						if (isSortByBarcode()) {
							sortFields.add("barcode");
						}
						if (isSortByGenus()) {
							sortFields.add("genus");
						}
						if (isSortBySpecificEpithet()) {
							sortFields.add("specificEpithet");
						}
						Map<String, String> filters = getFilterMap();
						boolean useAnd = true;
					    if (barcodesFilterCriterion != null && barcodesFilterCriterion != "") {
							useAnd = false;
						}
						result = new ListDataModel(
								getFacade().findRangeQueryAndOr(
								range, sortFields.toArray(new String[0]), true, filters,useAnd));
					}
					return result;
                }

				public Map<String, String> getFilterMap() {
					Map<String, String> filters = new HashMap<String, String>();
					if (filterGenus!=null) {
						filters.put("genus", filterGenus);
					}
					if (limitByAbbrevGenus==true) {
						filters.put("genus", "%.%");
					}
					if (limitByBlankGenus==true) {
						filters.put("genus", "null");
					}
					return filters;
				}

            };
        }
        return pagination;
    }

	private String barcodeFilterCriterion = null;
	private String barcodesFilterCriterion = null;
	private boolean sortByBarcode = false;
	private boolean sortByGenus = true;
	private boolean sortBySpecificEpithet = true;

	public String getBarcodeFilterCriterion() {
		return barcodeFilterCriterion;
	}

	public void setBarcodeFilterCriterion(String barcodeFilterCriterion) {
		this.barcodeFilterCriterion = barcodeFilterCriterion;
	}

	public boolean isSortByBarcode() {
		return sortByBarcode;
	}

	public void setSortByBarcode(boolean sortByBarcode) {
		this.sortByBarcode = sortByBarcode;
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

	public void resetFilters() {
   		limitByBlankGenus = false;
		limitByAbbrevGenus = false;
		filterGenus = null;
	}

    public String prepareList() {
		pagination = null;
        recreateModel();
        return "ListProblems?faces-redirect=true";
    }

	public String prepareListAll() {
		pagination = null;
		items = null;
		resetFilters();
		getPagination().createPageDataModel();
		recreateModel();
		return "ListProblems?faces-redirect=true";
	}

    public String prepareView() {
        current = (Determination)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View?faces-redirect=true";
    }

    public String prepareCreate() {
        current = new Determination();
        selectedItemIndex = -1;
        return "Create?faces-redirect=true";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DeterminationCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Determination)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit?faces-redirect=true";
    }

	/**
	 * Get the specimen for the current determination and display it in edit mode.
	 *
	 * @return "/lepidoptera/specimen/Edit?faces-redirect=true"
	 */
    public String prepareEditSpecimen() {
		try {
            current = (Determination)getItems().getRowData();
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
		} catch (Exception e) {
			// expected if not coming off of a list of items.
		}
		specimenController.setSelected(current.getSpecimenId());
        return "/lepidoptera/specimen/Edit?faces-redirect=true";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DeterminationUpdated"));
            return "View?faces-redirect=true";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Determination)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "ListProblems?faces-redirect=true";
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
            return "ListProblems?faces-redirect=true";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("DeterminationDeleted"));
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
        return "ListProblems?faces-redirect=true";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "ListProblems?faces-redirect=true";
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
		return "ListProblems?faces-redirect=true";
	}

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    @FacesConverter(forClass=Determination.class)
    public static class DeterminationControllerConverter implements Converter {

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DeterminationController controller = (DeterminationController)facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "determinationController");
            return controller.ejbFacade.find(getKey(value));
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
            if (object instanceof Determination) {
                Determination o = (Determination) object;
                return getStringKey(o.getDeterminationId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: "+DeterminationController.class.getName());
            }
        }

    }

	public boolean isLimitByAbbrevGenus() {
		return limitByAbbrevGenus;
	}

	public void setLimitByAbbrevGenus(boolean limitByAbbrevGenus) {
		this.limitByAbbrevGenus = limitByAbbrevGenus;
	}

	public boolean isLimitByBlankGenus() {
		return limitByBlankGenus;
	}

	public void setLimitByBlankGenus(boolean limitByBlankGenus) {
		this.limitByBlankGenus = limitByBlankGenus;
	}

	public String getFilterGenus() {
		return filterGenus;
	}

	public void setFilterGenus(String filterGenus) {
		this.filterGenus = filterGenus;
	}

}
