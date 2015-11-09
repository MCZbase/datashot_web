/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.Specimen;
import java.util.List;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author mole
 */
@Stateless
@LocalBean
@DeclareRoles("Administrator")
@RolesAllowed("Administrator")
public class SpecimenSessionBean {

	 @PersistenceContext(unitName = "bu_test_web_PU")
    private EntityManager em;



	public List<Specimen> retrieve() {
		Query query = em.createNamedQuery("Specimen.findAll");
		return query.getResultList();
	}

	public Specimen update(Specimen specimen) {
		em.persist(specimen);
		return specimen;
	}
    
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
 
}
