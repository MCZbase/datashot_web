/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.Specimen;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 * REST Web Service
 *
 * @author mole
 */
@Path("rss")
@Stateless
@DeclareRoles("Administrator")
@RolesAllowed("Administrator")
public class recent_specimens_feed {

	@Context
	private UriInfo context;

	@PersistenceContext(unitName = "bu_test_web_PU")
	private EntityManager em;

	//@ManagedProperty(value="http://localhost:8086/test/")
	private String baseURL;

	/** Creates a new instance of recent_specimens_feed */
	public recent_specimens_feed() {
	}

	/**
	 * Retrieves representation of an instance of edu.harvard.mcz.imagecapture.managedbeans.recent_specimens_feed
	 * @return an instance of java.lang.String which is an xml document containing an RSS feed.
	 */
	@GET
	@Produces("application/xml")
	public String getXml() {
		baseURL = "http://localhost:8086/ic/";
		//CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
	        Query q = em.createNamedQuery("Specimen.findAllOrderRecentlyCreated");
		List<Specimen> specimens = q.getResultList();
		StringBuffer result = new StringBuffer();
		DateFormat df = DateFormat.getDateInstance();
                String timestampNow = df.format(new Date());

		result.append("<?xml version=\"1.0\"?>"
			+ "<rss version=\"2.0\">"
			+ "<channel>"
			+ "<title>Recently Created MCZ Lepidoptera Specimen Records</title>"
			+ "<link>http://www.mcz.harvard.edu/</link>"
			+ "<description>The most recently created set of records in the MCZ Lepidoptera imaging project.</description>"
			+ "<lastBuildDate>" + timestampNow  + "</lastBuildDate>"
			+ "<docs>http://www.rssboard.org/rss-specification</docs>"
			+ "<pubDate>"+ timestampNow +"</pubDate>"
			);
		Iterator<Specimen> si = specimens.iterator();
		int delivered = 0;
		while (si.hasNext() & delivered < 16 ) {
			delivered++;
			Specimen s = si.next();
			String updatedBy = s.getLastUpdatedBy();
			if (updatedBy==null) { updatedBy = ""; }
			Date datecreated = s.getDateCreated();
			Date dateupdated = s.getDateLastUpdated();
			if (dateupdated==null) { dateupdated = datecreated; }

			result.append("<item>"
				+ "<title>" + s.getBarcode() + "</title>"
				+ "<description>" + s.getWorkFlowStatus() + " " + updatedBy + " " + s.getFamily() + ":" + s.getGenus() + " " + s.getSpecificEpithet() 
				+ " Created:" + datecreated
				+ " Updated:" + dateupdated
				+  "</description>"
				+ "<pubDate>" + dateupdated + "</pubDate>"
				+ "<link>" + baseURL + "/faces/index.xhtml?specimenId="+ s.getSpecimenId()+"</link>"
				+ "</item>");
		}
		result.append("</channel>"
			+ "</rss>");
		return result.toString();
	}

	/**
	 * PUT method for updating or creating an instance of recent_specimens_feed
	 * @param content representation for the resource
	 * @return an HTTP response with content of the updated or created resource.
	 */
	@PUT
	@Consumes("application/xml")
	public void putXml(String content) {
	}
}
