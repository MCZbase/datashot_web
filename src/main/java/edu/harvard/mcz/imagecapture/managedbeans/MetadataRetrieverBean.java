/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.data.MetadataRetriever;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author mole
 */
@ManagedBean(name="MetadataRetrieverBean")
@RequestScoped
public class MetadataRetrieverBean {
	
	// TODO: Get Validator for ISO date (including ranges)
	
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
