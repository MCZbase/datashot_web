package edu.harvard.mcz.imagecapture.managedbeans;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.ManagedProperty;
import jakarta.faces.bean.ViewScoped;

import org.primefaces.event.SelectEvent;  
import org.primefaces.model.tagcloud.DefaultTagCloudItem;  
import org.primefaces.model.tagcloud.DefaultTagCloudModel;  
import org.primefaces.model.tagcloud.TagCloudItem;  
import org.primefaces.model.tagcloud.TagCloudModel;  

import edu.harvard.mcz.imagecapture.ejb.SpecimenFacadeLocal;
import edu.harvard.mcz.imagecapture.jsfclasses.SpecimenController;
import edu.harvard.mcz.imagecapture.utility.CountValue;

/**
 * Session Bean implementation class GenusTagCloudBean
 */
@ManagedBean
@ViewScoped
public class GenusTagCloudBean   implements Serializable {
	
	private static final long serialVersionUID = 3743188999464993047L;

	@EJB(beanName="specimenFacade")
	private SpecimenFacadeLocal specimenFacade;
	
	@ManagedProperty(value="#{specimenController}")
	private SpecimenController specimenController;
	
    /**
     * Supporting managed property, injecting specimenController managed bean.
     * 
     * @return
     */
	public SpecimenController getSpecimenController()
    {
        return specimenController;
    }
    /**
     * Supporting managed property, injecting specimenController managed bean.
     * 
     * @param specimenController
     */
    public void setSpecimenController(SpecimenController specimenController)
    {
        this.specimenController = specimenController;
    }	
	
    private TagCloudModel topGenera;  

    /**
     * Default constructor. 
     */
    public GenusTagCloudBean() {
        topGenera = new DefaultTagCloudModel();
    }
    
    @PostConstruct
    private void init() {
        boolean useFrequency = true;
        List<CountValue> genera = specimenFacade.getGenusValuesRange(0, 25, useFrequency, null, "Taxon Entered");
        Iterator<CountValue> i = genera.iterator();
        while (i.hasNext()) { 
        	CountValue val = i.next();
            topGenera.addTag(new DefaultTagCloudItem(val.getValue(), val.getCount()));
        }
    }

  
    public TagCloudModel getModel() {  
        return topGenera;  
    }  
      
    public void onSelect(SelectEvent event) {  
        TagCloudItem item = (TagCloudItem) event.getObject();  
        specimenController.setGenusFilterCriterion(item.getLabel());
    }  
} 