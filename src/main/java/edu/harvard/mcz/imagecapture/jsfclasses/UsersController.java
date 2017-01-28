package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.data.Users;
import edu.harvard.mcz.imagecapture.jsfclasses.util.JsfUtil;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.managedbeans.LoginBean;
import edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal;
import edu.harvard.mcz.imagecapture.utility.AuthenticationUtility;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

@ManagedBean (name="usersController")
@SessionScoped
public class UsersController {
	
	private final static Logger logger = Logger.getLogger(UsersController.class.getName());

    private Users current;
    private DataModel items = null;

    @EJB 
	private edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal usersFacade;

    private PaginationHelper pagination;
    private int selectedItemIndex;

    public UsersController() {
		super();
    }

    public Users getSelected() {
        if (current == null) {
            current = new Users();
            selectedItemIndex = -1;
        }
        return current;
    }

    private UsersFacadeLocal getFacade() {
        return usersFacade;
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
        return "List?faces-redirect=true";
    }

    public String prepareView() {
        current = (Users)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View?faces-redirect=true";
    }

    public String prepareCreate() {
        current = new Users();
        selectedItemIndex = -1;
        return "Create?faces-redirect=true";
    }

    public String create() {
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

    public String prepareEdit() {
        current = (Users)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit?faces-redirect=true";
    }

    public String prepareEditMyPassword() {
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
		logger.log(Level.INFO,"in prepareEditPassword");
        current = (Users)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
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
        current = (Users)getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("UsersDeleted"));
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
		current = null;
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
