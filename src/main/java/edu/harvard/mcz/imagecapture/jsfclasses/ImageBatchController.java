/**
 * ImageBatchController.java
 * edu.harvard.mcz.imagecapture.jsfclasses
 * Copyright © 2015 President and Fellows of Harvard College
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of Version 2 of the GNU General Public License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author: Paul J. Morris
 */
package edu.harvard.mcz.imagecapture.jsfclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * @author mole
 *
 */
//@Named("imageBatchController")
@ManagedBean
@ViewScoped
public class ImageBatchController implements Serializable {
	
	private static final long serialVersionUID = -5913826561791637038L;

	private final static Logger logger = Logger.getLogger(ImageBatchController.class.getName());
	
	@EJB 
	private edu.harvard.mcz.imagecapture.ejb.ImageFacadeLocal ejbFacade;
	
	private boolean listVisible = false;
	private boolean deleteOk = false;
	
	private String pathFilterCriterion;
	private String result;
	
	public ImageBatchController() { 
		init();
	}
	
	public void init() { 
		listVisible = false;
		deleteOk = false;
		result = "";
	}
	
	public void performList() { 
		logger.log(Level.INFO, pathFilterCriterion);
		listVisible = true;
		List otherImages = ejbFacade.findBatchWithOtherImages(pathFilterCriterion);
		if (otherImages==null || otherImages.size()==0) { 
			deleteOk = true;
		} else { 
			deleteOk = false;
		}
		if (getProblemList().size()>0) { 
			deleteOk = false;
		}
		logger.log(Level.INFO,Boolean.toString(deleteOk));
		if (deleteOk) {
			result = "";
		} else { 
			result = "Unable to delete batch, problems exist.  You can only delete a batch of images if none of the specimen records have been edited and if no specimen is also linked to a record outside of the batch.";
		}
	}
	
	public List<String> getCandidateList() {
		List<String> retval = new ArrayList<String>();
		if (pathFilterCriterion!=null) { 
		   retval =  ejbFacade.findBatch(pathFilterCriterion);
		} else {
			retval.add("files on path");
		}
		return retval;
	}
	
	public List<String> getProblemList() { 
		List<String> retval = new ArrayList<String>();
		if (pathFilterCriterion!=null) { 
		   List<String>problemlist =  ejbFacade.findBatchWithOtherImages(pathFilterCriterion);
		   Iterator<String> i = problemlist.iterator();
		   while (i.hasNext()) { 
			   String problem = i.next();
			   retval.add(problem + " Specimen also linked to image in another batch.");
		   }
		   retval.addAll(ejbFacade.findBatchFilesPostTaxon(pathFilterCriterion));
		}
		retval = highlightProblems(retval);
		return retval;
	}
	
	public void performDelete() { 
		if (deleteOk) { 
		   result = ejbFacade.deleteBatch(pathFilterCriterion);
		   deleteOk = false;
		   listVisible = false;
		}
	}
	
	public static List<String> highlightProblems(List<String> problems) {
		List<String> result = new ArrayList<String>();
		if (problems!=null) {
		    Iterator<String> i = problems.iterator();
		    while (i.hasNext()) { 
		    	String problem = i.next();
		    	result.add(problem.replaceAll("(Text Entered|QC Problems|QC Reviewed|Specialist Reviewed)","** $1 **  Specimen record has been edited."));
		    }
		}
		return result;
	}

	/**
	 * @return the pathFilterCriterion
	 */
	public String getPathFilterCriterion() {
		return pathFilterCriterion;
	}

	/**
	 * @param pathFilterCriterion the pathFilterCriterion to set
	 */
	public void setPathFilterCriterion(String pathFilterCriterion) {
		this.pathFilterCriterion = pathFilterCriterion;
	}

	/**
	 * @return the listVisible
	 */
	public boolean isListVisible() {
		return listVisible;
	}

	/**
	 * @param listVisible the listVisible to set
	 */
	public void setListVisible(boolean listVisible) {
		this.listVisible = listVisible;
	}

	/**
	 * @return the deleteOk
	 */
	public boolean isDeleteOk() {
		return deleteOk;
	}

	/**
	 * @param deleteOk the deleteOk to set
	 */
	public void setDeleteOk(boolean deleteOk) {
		this.deleteOk = deleteOk;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}
	
}
