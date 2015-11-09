/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.Specimen;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author mole
 */
@ManagedBean(name="specimenDetailsBean")
@RequestScoped
@DeclareRoles("Administrator")
@RolesAllowed("Administrator")
public class specimenDetailsBean {

    @ManagedProperty(value="#{param.specimenId")
    private int specimenId;


    @ManagedProperty(value="#{specimenBean.find(specimenId)}")
    private Specimen specimen;


    /** Creates a new instance of specimenDetailsBean */
    public specimenDetailsBean() {
    }
	public Specimen getSpecimen() {
		return specimen;
	}

	public void setSpecimen(Specimen specimen) {
		this.specimen = specimen;
	}

	public int getSpecimenId() {
		return specimenId;
	}

	public void setSpecimenId(int specimenId) {
		this.specimenId = specimenId;
	}
}
