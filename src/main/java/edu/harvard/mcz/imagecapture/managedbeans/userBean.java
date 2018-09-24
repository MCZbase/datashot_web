/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.Users;
import edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal;

import java.io.Serializable;
import java.util.List;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;

/**
 *
 * @author mole
 */
//@Named("userBean")
@ManagedBean
@RequestScoped
@DeclareRoles("Administrator")
@RolesAllowed("Administrator")
public class userBean  implements Serializable{ 
	
	private static final long serialVersionUID = 7850573718338503505L;
	
	@EJB
    private UsersFacadeLocal usersFacade;

    /** Creates a new instance of userBean */
    public userBean() {
    }

    public void remove(Users users) {
        usersFacade.remove(users);
    }

    public List<Users> findRange(int[] range) {
        return usersFacade.findRange(range);
    }

    public List<Users> findAll() {
        return usersFacade.findAll();
    }

    public Users find(Object id) {
        return usersFacade.find(id);
    }

    public void edit(Users users) {
        usersFacade.edit(users);
    }

    public void create(Users users) {
        usersFacade.create(users);
    }

    public int count() {
        return usersFacade.count();
    }

    public Users findByUsername(String username) {
        return usersFacade.findByName(username);
    }

    

}
