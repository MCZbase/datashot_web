package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.data.Users;
import edu.harvard.mcz.imagecapture.jsfclasses.util.JsfUtil;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal;
import edu.harvard.mcz.imagecapture.utility.AuthenticationUtility;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@ManagedBean
@SessionScoped
public class UsersController  implements Serializable {
	
	private static final long serialVersionUID = 6493301386438480771L;

	private final static Logger logger = Logger.getLogger(UsersController.class.getName());

    private Users current;
    private DataModel<Users> items = null;

    @EJB 
	private edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal usersFacade;

    private PaginationHelper pagination;
	private int selectedItemIndex;  // index including pages
	private int currentRowIndex;    // within page index
	
	// Filter criteria
	private String fullnameFilterCriterion = null;
	private String roleFilterCriterion = null;
	
    public UsersController() {
		super();
    }

    public Users getSelected() {
		logger.log(Level.INFO,"getSelected()" + this.toString());
        if (current == null) {
            current = new Users();
            selectedItemIndex = -1;
			currentRowIndex=-1;            
        }
		logger.log(Level.INFO,"getSelected() called, current = " + current.getUsername());
        return current;
    }
    
	public void setSelected(Users selected) {
		logger.log(Level.INFO,"setSelected()" + this.toString());
		logger.log(Level.INFO,"setSelected() called with selected = " + selected.getUsername());
		if (selected!=null) {
		    logger.log(Level.INFO,selected.getUsername());
			current=selected;
			currentRowIndex=-1;
			selectedItemIndex = -1;
			pagination = null;
		}
		logger.log(Level.INFO,"setSelected() current = " + current.getUsername());
		logger.log(Level.INFO,"setSelected() currentRowIndex = " + Integer.toString(currentRowIndex));
	}    

    private UsersFacadeLocal getFacade() {
        return usersFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(50) {
            	
				public Map<String, String> getFilterMap() {
					Map<String, String> filters = new HashMap<String, String>();
					if (fullnameFilterCriterion != null) {
						filters.put("fullname", fullnameFilterCriterion);
					}
					if (roleFilterCriterion != null) {
						filters.put("role", roleFilterCriterion);
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
                public DataModel<Users> createPageDataModel() {
                	String[] sortFields = { "fullname" };
                	Map<String, String> filters = getFilterMap();
                    return new ListDataModel<Users>(getFacade().findRangeQuery(
                    		new int[]{getPageFirstItem(), getPageFirstItem()+getPageSize()},
                    		sortFields,
                    		true,
                    		filters
                    		));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
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
		setFullnameFilterCriterion(null);	
		setRoleFilterCriterion(null);
	}

    public String prepareView() {
		logger.log(Level.INFO,"prepareView()" + this.toString());
        current = (Users)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
		currentRowIndex = getItems().getRowIndex();
        return "View?faces-redirect=true";
    }

    public String prepareCreate() {
		logger.log(Level.INFO,"prepareCreate()" + this.toString());
        current = new Users();
        selectedItemIndex = -1;
		currentRowIndex = -1;        
        return "Create?faces-redirect=true";
    }

    public String create() {
		logger.log(Level.INFO,"create()" + this.toString());
		boolean ok = false;
		String result = null;
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsersCreated"));
			ok = true;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
		if (ok) {
           result = prepareList();
		}
		return result;
    }

    public String prepareEdit(Users targetUser) {
		logger.log(Level.INFO,"prepareEdit()" + this.toString());
        if (targetUser!=null) { current=targetUser; } 
    //public String prepareEdit() {
        if (current!=null) { 
  		    logger.log(Level.INFO,"prepareEdit current = " +current.getUsername());
        } else {  
            logger.log(Level.INFO, "prepareEdit, current is null");
        }
        if (getItems()!=null && getItems().getRowData()!=null) { 
           current = (Users) getItems().getRowData();
		   logger.log(Level.INFO,"preepareEdit() current set to = " + current.getUsername());
           selectedItemIndex = getPagination().getPageFirstItem() + getItems().getRowIndex();
           currentRowIndex = getItems().getRowIndex();
        }
		logger.log(Level.INFO,"preepareEdit() current is " + current.getUsername());
		logger.log(Level.INFO,"preepareEdit() rowIndex= " + Integer.toString(getItems().getRowIndex()) );
        return "Edit?faces-redirect=true";
    }

    public String prepareEditMyPassword() {
		logger.log(Level.INFO,"prepareEditMyPassword()" + this.toString());
		String result = "/index?faces-redirect=true";
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (null != externalContext) {
			String remoteUser = externalContext.getRemoteUser();
			if (remoteUser != null) {
				current =  usersFacade.findByName(remoteUser);
                result = "/lepidoptera/users/SetNewPassword?faces-redirect=true";
			}
		}
		return result;
	}

    public String prepareEditPassword() {
		logger.log(Level.INFO,"prepareEditPassword()" + this.toString());
		logger.log(Level.INFO,"in prepareEditPassword");
        current = (Users)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        currentRowIndex = getItems().getRowIndex();
		logger.log(Level.INFO,"end prepareEditPassword");
        return "/lepidoptera/users/SetNewPassword?faces-redirect=true";
    }
    
    public String prepareEditPasswordCurrent() {
		logger.log(Level.INFO,"in prepareEditPasswordCurrent");
        return "/lepidoptera/users/SetNewPassword?faces-redirect=true";
    }    

    /** Update the user, but not the password. 
     * 
     * @return null 
     */
    public String update() {
		logger.log(Level.INFO,"update()" + this.toString());
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsersUpdated"));
            return null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Error: " + e.getMessage());
		    FacesContext.getCurrentInstance().validationFailed();
            return null;
        }
    }
    
    /**
     * Update the user, including the password. 
     * 
     * @return null
     */
    public String updatePassword() {
		logger.log(Level.INFO,"updatePassword()" + this.toString());
        try {
        	if (current.updateHashFromNewPassword()) { 
               getFacade().edit(current);
               JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsersUpdated"));
        	} else { 
               JsfUtil.addErrorMessage("Warning", "Password was empty, not saved");
        	}
            return null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Error: " + e.getMessage());
		    FacesContext.getCurrentInstance().validationFailed();
            return null;
        }
    }    

    public String destroy() {
		logger.log(Level.INFO,"destroy()" + this.toString());
        current = (Users)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        currentRowIndex = getItems().getRowIndex();
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
		logger.log(Level.INFO,"performDestroy()" + this.toString());
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsersDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
		logger.log(Level.INFO,"updateCurrentItem()" + this.toString());
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

    public DataModel<Users> getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    /**
	 * @return the fullnameFilterCriterion
	 */
	public String getFullnameFilterCriterion() {
		return fullnameFilterCriterion;
	}

	/**
	 * @param fullnameFilterCriterion the fullnameFilterCriterion to set
	 */
	public void setFullnameFilterCriterion(String fullnameFilterCriterion) {
		if (fullnameFilterCriterion!=null && fullnameFilterCriterion.length()>0) { 
			if (!fullnameFilterCriterion.startsWith("%")) { 
				fullnameFilterCriterion = "%" + fullnameFilterCriterion;
			}
			if (!fullnameFilterCriterion.endsWith("%")) { 
				fullnameFilterCriterion = fullnameFilterCriterion + "%"; 
			}			
		}
		this.fullnameFilterCriterion = fullnameFilterCriterion;
	}

	/**
	 * @return the roleFilterCriterion
	 */
	public String getRoleFilterCriterion() {
		return roleFilterCriterion;
	}

	/**
	 * @param roleFilterCriterion the roleFilterCriterion to set
	 */
	public void setRoleFilterCriterion(String roleFilterCriterion) {
		this.roleFilterCriterion = roleFilterCriterion;
	}

	private void recreateModel() {
		logger.log(Level.INFO,"recreateModel()" + this.toString());
        items = null;
		current = null;
    }

    public String next() {
        getPagination().nextPage();
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

    public boolean isHasNextRow() {
		logger.log(Level.INFO,"isHasNextrow()" + this.toString());
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
    
	/**Is there a previous row to edit? 
	 * 
	 * @return true if there is a previous row (on the same or previous page).
	 */
    public boolean isHasPreviousRow() {
		logger.log(Level.INFO,"isHasPreviousRow()" + this.toString());
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
	
    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(usersFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(usersFacade.findAll(), true);
    }

    @FacesConverter(forClass=Users.class)
    public static class UsersControllerConverter implements Converter {

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            UsersController controller = (UsersController)facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "usersController");
            return controller.usersFacade.find(getKey(value));
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
            if (object instanceof Users) {
                Users o = (Users) object;
                return getStringKey(o.getUserid());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: "+UsersController.class.getName());
            }
        }

    }

	public String getPasswordRules() {
		return AuthenticationUtility.getPasswordStrengthRules();
	}

}
