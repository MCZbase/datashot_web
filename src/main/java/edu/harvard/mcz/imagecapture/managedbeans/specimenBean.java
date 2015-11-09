/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.buisness.StatusCount;
import edu.harvard.mcz.imagecapture.data.Specimen;
import edu.harvard.mcz.imagecapture.ejb.SpecimenFacadeLocal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ComponentSystemEvent;

/**
 *
 * @author mole
 */
@ManagedBean(name="specimenBean")
@RequestScoped
@DeclareRoles("Administrator")
@RolesAllowed("Administrator")
public class specimenBean {
    @EJB
    private SpecimenFacadeLocal specimenFacade;

	private final static Logger logger = Logger.getLogger(specimenBean.class.getName());
	
    private Long selectedSpecimenId;
    private Specimen selectedSpecimen;


    /** Creates a new instance of specimenBean */
    public specimenBean() {
    	logger.log(Level.INFO,"new specimenBean");
    }

	public Long getSelectedSpecimenId() {
		return selectedSpecimenId;
	}

	public void setSelectedSpecimenId(Long selectedSpecimenId) {
		this.selectedSpecimenId = selectedSpecimenId;
	}

	public Specimen getSelectedSpecimen() {
		return selectedSpecimen;
	}

	public void setSelectedSpecimen(Specimen selectedSpecimen) {
		this.selectedSpecimen = selectedSpecimen;
	}



    public void remove(Specimen specimen) {
        specimenFacade.remove(specimen);
    }

    public List<Specimen> findRange(int[] range) {
        return specimenFacade.findRange(range);
    }

    public List<Specimen> findAll() {
        return specimenFacade.findAll();
    }

    public Specimen find(Object id) {
        return specimenFacade.find(id);
    }

    public void edit(Specimen specimen) {
    	logger.log(Level.INFO,"specimenBean.edit " + specimen.getBarcode());
        specimenFacade.edit(specimen);
    }

    public void create(Specimen specimen) {
    	logger.log(Level.INFO,"specimenBean.create " + specimen.getBarcode());
        specimenFacade.create(specimen);
    }

    public int count() {
        return specimenFacade.count();
    }

    public List<Specimen> findBySpecimenId(Long specimenId) {
	  return  specimenFacade.findBySpecimenId(specimenId);
    }

    public String showDetails(Specimen aSpecimen) {
	    this.selectedSpecimen = aSpecimen;
	    return "specimenDetails";
    }

    public String getDetails() {
	    return "DETAILS";
    }

    public void loadSelectedSpecimen(ComponentSystemEvent cse) {
	List<Specimen> result = findBySpecimenId(getSelectedSpecimenId());
	if (result.isEmpty()) { 
	     selectedSpecimen = null;
	} else {
	     selectedSpecimen = result.get(0);
	}
    }

	public String getCountByStatus(String status) {

		return "";
	}

    public List<StatusCount> getWorkflowStatusCounts() {
	     return specimenFacade.getWorkflowStatusCounts();
	}

}
