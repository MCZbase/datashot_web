
package edu.harvard.mcz.imagecapture.jsfclasses.util;

import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

public class JsfUtil {
    
    public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne) {
        int size = selectOne ? entities.size() + 1 : entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : entities) {
            items[i++] = new SelectItem(x, x.toString());
        }
        return items;
    }
    
    public static void addErrorMessage(Exception ex, String defaultMsg) {
        String msg = ex.getMessage();
        if (msg != null && msg.length() > 0) {
            addErrorMessage(msg, defaultMsg);
        } else {
            addErrorMessage("Error", defaultMsg);
        }
    }
    
    public static void addErrorMessages(List<String> messages) {
        for (String message : messages) {
        	String summary = message;
        	if (summary.length()>30) { 
        		summary = summary.substring(0, 29);
        	}
            addErrorMessage(summary, message);
        }
    }

    public static void addErrorMessage(String summary, String msg) {
    	if (summary.length()>30) { 
    		summary = summary.substring(0, 29);
    	}    	
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public static void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
        FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
    }
    
    public static String getRequestParameter(String key) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
    }
    
    public static Object getObjectFromRequestParameter(String requestParameterName, Converter converter, UIComponent component) {
        String theId = JsfUtil.getRequestParameter(requestParameterName);
        return converter.getAsObject(FacesContext.getCurrentInstance(), component, theId);
    }
    
}