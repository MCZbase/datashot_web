/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.Specimen;
import java.util.List;
import javax.ejb.EJB;


/**
 *
 * @author mole
 */
public class SpecimenManagedBean {
	@EJB
	private SpecimenSessionBean specimenSessionBean;

	private Specimen specimen;

    /** Creates a new instance of SpecimenManagedBean */
    public SpecimenManagedBean() {
    }

    public List<Specimen> getSpecimens() {
	  return  specimenSessionBean.retrieve();
    }

    /**
     * Returns the selected Specimen object
     * @return
     */
    public Specimen getDetails()
    {
        //Can either do this for simplicity or fetch the details again from the
        //database using the ID
        return specimen;
    }

    /**
     * Action handler - user selects a specimen record from the list
     * (data table) to view/edit
     * @param specimen
     * @return
     */
    public String showDetails(Specimen specimen)
    {
        this.specimen = specimen;
        return "specimenDetails";
    }

    /**
     * Action handler - update the changes in the specimen object to the
     * database
     * @return
     */
    public String update()
    {
        specimen = specimenSessionBean.update(specimen);
        return "SAVED";
    }

    /**
     * Action handler - goes to the Customer listing page
     * @return
     */
    public String list()
    {
        return "LIST";
    }


}
