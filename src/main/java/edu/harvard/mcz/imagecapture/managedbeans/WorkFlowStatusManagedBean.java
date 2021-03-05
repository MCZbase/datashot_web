/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.Users;
import edu.harvard.mcz.imagecapture.ejb.UsersFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.WorkflowStatusBeanLocal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author mole
 */
//@Named(value = "workFlowStatusManagedBean")
@ManagedBean
@Dependent
public class WorkFlowStatusManagedBean  implements Serializable{

	private static final long serialVersionUID = -7464143789579531541L;
	
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
