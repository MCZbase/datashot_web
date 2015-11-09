/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
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

@Path("generic")
public class GenericResource {
    @Context
    private UriInfo context;

    /** Creates a new instance of GenericResource */
    public GenericResource() {
    }

	/**
	 * Retrieves representation of an instance of edu.harvard.mcz.imagecapture.managedbeans.GenericResource
	 * @return an instance of java.lang.String
	 */
	@GET
        @Produces("text/html")
	public String getHtml() {
		 return "<html><body><h1>Hello, World!</body></h1></html>";
	}

	/**
	 * PUT method for updating or creating an instance of GenericResource
	 * @param content representation for the resource
	 * @return an HTTP response with content of the updated or created resource.
	 */
	@PUT
        @Consumes("text/html")
	public void putHtml(String content) {
	}
}
