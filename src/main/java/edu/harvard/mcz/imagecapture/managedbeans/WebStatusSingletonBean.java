package edu.harvard.mcz.imagecapture.managedbeans;

import java.io.Serializable;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Singleton;
import jakarta.faces.bean.ManagedBean;
import jakarta.inject.Named;

/**
 *
 * @author Paul J. Morris
 */
@Singleton
//@Named("WebStatusSingletonBean")
@ManagedBean
@DeclareRoles("Administrator")
@RolesAllowed("Administrator")
public class WebStatusSingletonBean   implements Serializable {

	private static final long serialVersionUID = 1265428887335277695L;
	
	/**
     * The number of users currently logged in.
     */
    private int loggedInUserCount;

    /**
     * Construct the default instance of WebStatusSingletonBean.
     */
    public WebStatusSingletonBean() { 
        this.loggedInUserCount = 0;
    }

    /**
     * Get the value of loggedInUserCount
     *
     * @return the value of loggedInUserCount
     */
    @RolesAllowed("Administrator")
    public int getLoggedInUserCount() {
        return loggedInUserCount;
    }

    /**
     * Set the value of loggedInUserCount
     *
     * @param loggedInUserCount new value of loggedInUserCount
     */
    @RolesAllowed("Administrator")
    public void setLoggedInUserCount(int loggedInUserCount) {
        this.loggedInUserCount = loggedInUserCount;
    }

    /**
     * Increase the number of logged in Users by one.
     */
    @RolesAllowed("Administrator")
    public void incrementLoggedInUserCount() {
        System.out.println("Before incrementing user count:" + this.loggedInUserCount);
        this.loggedInUserCount++;
        System.out.println("After incrementing user count:" + this.loggedInUserCount);
    }

    /**
     * Decrease the number of logged in Users by one.
     */
    @RolesAllowed("Administrator")
    public void decrementLoggedInUserCount() {
        this.loggedInUserCount--;
    }

 
}
