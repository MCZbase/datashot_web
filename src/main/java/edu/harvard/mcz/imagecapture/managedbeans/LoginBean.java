/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.Users;
import edu.harvard.mcz.imagecapture.jsfclasses.ChatController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.faces.bean.SessionScoped;
import javax.faces.annotation.ManagedProperty;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author mole
 */
//@Named("loginBean")
@ManagedBean
@SessionScoped
public class LoginBean  implements Serializable{

	private static final long serialVersionUID = 3117579405513401728L;
	
	@EJB
	edu.harvard.mcz.imagecapture.managedbeans.WebStatusSingletonBean status;
	@EJB
	private edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal usersFacade;
	@ManagedProperty(value="#{chatController}")
	private ChatController chatController;

	private String state;
	private boolean loggedIn;
	private String username;

    /**
     * Supporting managed property, injecting chatController managed bean.
     * 
     * @return
     */
	public ChatController getChatController()
    {
        return chatController;
    }
    /**
     * Supporting managed property, injecting chatController managed bean.
     * 
     * @param specimenController
     */
    public void setChatController(ChatController chatController)
    {
        this.chatController = chatController;
    }	 
    
	public boolean isLoggedIn() {
		return loggedIn;
	}

	private void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		String retval = null;
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (null != externalContext) {
			String remoteUser = externalContext.getRemoteUser();
			if (remoteUser != null) {
				retval = remoteUser;
				setUsername(remoteUser);
				setLoggedIn(true);
			} else {
				retval = "";
				username = null;
				setLoggedIn(false);
			}
		} else {
			retval = "Unable to obtain Faces ExternalContext and hence the remote user details.";
		}
		return retval;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	private String password;

	/** Creates a new instance of loginBean */
	public LoginBean() {
		state = "Logged out";
		this.setLoggedIn(false);
	}


	/*
	public String doLogout(){
	// Not working within JSF.  Need a different solution.
	ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
	String contextPath = context.getRequestContextPath();
	HttpSession activeSession = (HttpSession) context.getSession(false);
	if (activeSession != null) {
	// activeSession.invalidate();
	}
	//return contextPath + "/login.xhtml?faces-redirect=true";
	return contextPath + "/login.xhtml";
	}
	 *
	 */
	public String logout() {
		Logger.getLogger(LoginBean.class.getName()).log(Level.INFO, "Called loginBean.logout()");
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		try {
			chatController.registerLogout();
			//request.logout();
			HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
			session.invalidate();
		//} catch (ServletException ex) {
		} catch (Exception ex) {
			Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
		}
		return "/loginfailure?faces-redirect=true";
	}

	public String getState() {
		String retval = null;
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (null != externalContext) {
			String remoteUser = externalContext.getRemoteUser();
			if (remoteUser != null) {
				retval = "Logged in [" + remoteUser + "]";
				setUsername(remoteUser);
				setLoggedIn(true);
				String username = usersFacade.findByName(remoteUser).getFullname();
			} else {
				retval = "Logged out";
				username = null;
				setLoggedIn(false);
			}
		} else {
			retval = "Unable to obtain Faces ExternalContext and hence the remote user details.";
		}
		state = retval;
		return retval;
	}

	private void setState(String state) {
		this.state = state;
	}

	@RolesAllowed({"Administrator", Users.ROLE_CHIEF_EDITOR, Users.ROLE_EDITOR})
	public int getLoggedInUserCount() {
		return status.getLoggedInUserCount();
	}

	/**
	 * Workaround for prime faces p:panel collapsed tag requiring a settable attribute.
	 * 
	 * @return value of isUserAbleToEditImages
	 */
	public boolean isUserAdministratorWorkaround() {
		return isUserAdministrator();
	}
	
	/**
	 * Workaround for prime faces p:panel collapsed tag requiring a settable attribute.
	 * 
	 * @param ignored
	 */
	public void setUserAdministratorWorkaround(boolean ignored) {
		// do nothing, workaround for p:panel collapsed requiring a settable attribute
	}		
	
	public boolean isUserAdministrator() {
		return FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_ADMINISTRATOR);
	}

	/** Is the user in the role of an editor, not higher or lower.
	 *
	 * @return true if user is either a chief editor or editor, false for any other role.
	 */
	public boolean isUserExactlyAnyEditor() {
		boolean result = false;
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_CHIEF_EDITOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_EDITOR)) {
			result = true;
		}
		return result;
	}

	/**
	 *
	 * @return true only if the user is in role chief editor.
	 */
	public boolean isUserExactlyChiefEditor() {
		return FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_CHIEF_EDITOR);
	}

	/**
	 *
	 * @return true only if the user is in the role data entry, false for any other role.
	 */
	public boolean isUserExactlyDataEntry() {
		return FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_DATAENTRY);
	}

	/**
	 * Workaround for prime faces p:panel collapsed tag requiring a setable attribute.
	 * 
	 * @return inverse of the value of isUserAbleToEditImages
	 */
	public boolean isUserUnAbleToEditImagesWorkaround() {
		return !isUserAbleToEditImages();
	}
	
	/**
	 * Workaround for prime faces p:panel collapsed tag requiring a setable attribute.
	 * 
	 * @param ignored
	 */
	public void setUserUnAbleToEditImagesWorkaround(boolean ignored) {
		// do nothing, workaround for p:panel collapsed requiring a settable atribute
	}	
	
	/**
	 * 
	 * @return true if user is in a role that is able to edit images.
	 */
	public boolean isUserAbleToEditImages() {
		boolean result = false;
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_ADMINISTRATOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_CHIEF_EDITOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_FULL)) {
			result = true;
		}
		//Workaround, "Full Access" instead of "Full access"
		//TODO: Fix case.
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("Full Access")) {
			result = true;
		}
		return result;
	}

	/**
	 * Workaround for prime faces p:panel collapsed tag requiring a setable attribute.
	 * 
	 * @return inverse of value of isUserChiefEditor
	 */
	public boolean isUserNotChiefEditorWorkaround() {
		return !isUserChiefEditor();
	}
	
	/**
	 * Workaround for prime faces p:panel collapsed tag requiring a setable attribute.
	 * 
	 * @param ignored
	 */
	public void setUserNotChiefEditorWorkaround(boolean ignored) {
		// do nothing, workaround for p:panel collapsed requiring a settable atribute
	}	
	
	/**
	 *
	 * @return true if the user is at least a chief editor
	 */
	public boolean isUserChiefEditor() {
		boolean result = false;
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_ADMINISTRATOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_CHIEF_EDITOR)) {
			result = true;
		}
		return result;
	}

	/**
	 *
	 * @return true if the user is at least an editor.
	 */
	public boolean isUserEditor() {
		boolean result = false;
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_ADMINISTRATOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_CHIEF_EDITOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_EDITOR)) {
			result = true;
		}
		return result;
	}

	public boolean isUserFullAccess() {
		boolean result = false;
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_ADMINISTRATOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_CHIEF_EDITOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_EDITOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_FULL)) {
			result = true;
		}
		//Workaround, "Full Access" instead of "Full access"
		//TODO: Fix case.
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("Full Access")) {
			result = true;
		}
		return result;
	}

	public boolean isUserNotDataEntry() { 
	   return !isUserDataEntry();
    }
	
	/**
	 *
	 * @return true if user is at least role data entry.
	 */
	public boolean isUserDataEntry() {
		boolean result = false;
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_ADMINISTRATOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_CHIEF_EDITOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_EDITOR)) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_FULL)) {
			result = true;
		}
		//Workaround, "Full Access" instead of "Full access"
		//TODO: Fix case.
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole("Full Access")) {
			result = true;
		}
		if (FacesContext.getCurrentInstance().getExternalContext().isUserInRole(Users.ROLE_DATAENTRY)) {
			result = true;
		}

		return result;
	}

}
