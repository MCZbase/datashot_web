/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.MetadataRetriever;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Named;

/**
 *
 * @author mole
 */
//@Named("MetadataRetrieverBean")
@ManagedBean
@RequestScoped
public class MetadataRetrieverBean implements Serializable {
	
	// TODO: Get Validator for ISO date (including ranges)
	
	private static final long serialVersionUID = -707896712078671879L;
	
	private final static Logger logger = Logger.getLogger(MetadataRetrieverBean.class.getName());	

	public String regexForField(String table, String field) { 
		String result = "";
		if (table!=null & field!=null) {
			try {
				result = MetadataRetriever.getRegex(Class.forName("edu.harvard.mcz.imagecapture.data."+table), field);
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(MetadataRetrieverBean.class.getName()).log(Level.SEVERE, null, ex);
				result = "Table not found" + ex.getMessage();
			}
		}
		return result;	    	
	}

	public String tipForField(String table, String field) {
		String result = "";
		if (table!=null & field!=null) {
			try {
				result = MetadataRetriever.getFieldHelp(Class.forName("edu.harvard.mcz.imagecapture.data."+table), field);
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(MetadataRetrieverBean.class.getName()).log(Level.SEVERE, null, ex);
				result = "Table not found" + ex.getMessage();
			}
		}
		return result;
	}

	public int lengthForField(String table, String field) {
	    Logger.getLogger(MetadataRetrieverBean.class.getName()).log(Level.INFO, table.concat(".").concat(field));
		int result = 50;
		if (table!=null & field!=null) {
			try {
				result = MetadataRetriever.getFieldLength(Class.forName("edu.harvard.mcz.imagecapture.data."+table), field);
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(MetadataRetrieverBean.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return result;
	}
 
}
