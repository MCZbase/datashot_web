/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.Specimen;

import java.io.Serializable;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.faces.bean.RequestScoped;
import javax.faces.annotation.ManagedProperty;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;

/**
 *
 * @author mole
 */
//@Named("specimenDetailsBean")
@ManagedBean
@RequestScoped
@DeclareRoles("Administrator")
@RolesAllowed("Administrator")
public class specimenDetailsBean  implements Serializable {

	private static final long serialVersionUID = 5531663810787706029L;

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
