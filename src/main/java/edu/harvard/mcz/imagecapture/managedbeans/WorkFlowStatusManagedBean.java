/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.Users;
import edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.WorkflowStatusBeanLocal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author mole
 */
@Named(value = "workFlowStatusManagedBean")
@Dependent
public class WorkFlowStatusManagedBean {

	@EJB
	WorkflowStatusBeanLocal workFlowStatusBean;
	@EJB
	UsersFacadeLocal usersFacade;

	/** Creates a new instance of WorkFlowStatusManagedBean */
	public WorkFlowStatusManagedBean() {
	}


	public List<String> getAllWorkflowStatusValuesWithBlank() {
	   ArrayList<String> valuesList = (ArrayList<String>) getAllWorkflowStatusValues();
	   valuesList.add(0, "");
	   return valuesList;
	}
	
	public List<String> getAllWorkflowStatusValuesWithBlankAndNot() {
		   ArrayList<String> valuesList = (ArrayList<String>) getAllWorkflowStatusValues();
		   Iterator<String> i = ((ArrayList<String>) getAllWorkflowStatusValues()).iterator();
		   while (i.hasNext()) { 
			   valuesList.add("!" + i.next());
		   }
		   valuesList.add(0, "");
		   return valuesList;
		}	

	public List<String> getAllWorkflowStatusValues() {
		ArrayList<String> valuesList = new ArrayList<String>();
		String values[] = workFlowStatusBean.getWorkFlowStatusValues();
		if (values != null) {
			for (int x = 0; x < values.length; x++) {
				valuesList.add(values[x]);
			}
		}
		return valuesList;
	}

	public List<String> getWorkFlowStatusValues() {
		ArrayList<String> valuesList = new ArrayList<String>();
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		if (null != externalContext) {
			String remoteUser = externalContext.getRemoteUser();
			Users user = usersFacade.findByName(remoteUser);
			if (user != null) {
				String values[] = workFlowStatusBean.getWorkFlowStatusValuesForUser(user);
				if (values != null) {
					for (int x = 0; x < values.length; x++) {
						valuesList.add(values[x]);
					}
				}
			}
		}
		return valuesList;
	}
}
