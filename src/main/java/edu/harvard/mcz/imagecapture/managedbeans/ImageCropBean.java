/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.mcz.imagecapture.managedbeans;

import edu.harvard.mcz.imagecapture.PositionTemplate;
import edu.harvard.mcz.imagecapture.data.Image;
import edu.harvard.mcz.imagecapture.data.Label;
import edu.harvard.mcz.imagecapture.ejb.ImageFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.LabelFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.PositionTemplateFacadeLocal;
import edu.harvard.mcz.imagecapture.ejb.TemplateFacadeLocal;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.primefaces.model.CroppedImage;

/**
 *
 * @author mole
 */
@Stateless
//@Named("imageCropBean")
@ManagedBean
public class ImageCropBean implements Serializable {
	
	/**
	 * Amount by which to multiply the size of the cropped image in a zoom display 
	 * respect to the the original image size.  ZOOMFACTOR=1 is image at original 
	 * pixel size, ZOOMFACTOR=2 is image at twice actual pixel size.  
	 */
	public static final int ZOOMFACTOR = 2; 

	private final static Logger logger = Logger.getLogger(ImageCropBean.class.getName());
	private static final long serialVersionUID = -7167445547859986391L;
	private CroppedImage croppedImage;
	private Float scaleFactor;  // Scaling factor to convert croppedImage coordinates from scale of rendered image to pixels in original image.
	private int regionXOffset;  // offset of the upper right corner of the template region shown in the ImageCropper from the upper right of the original image.
	private int regionYOffset;
	private boolean renderable = false;
	private boolean showNewLabelDialog = false;
	private String targetImageID;

	private String newImageName;
	private Label newLabel;
	private String saveResults = "";

	@EJB
	ImageFacadeLocal imageFacade;
	@EJB
	LabelFacadeLocal labelFacade;
	@EJB
	PositionTemplateFacadeLocal positionTemplateFacade;
	@EJB
	TemplateFacadeLocal templateFacade;

    public ImageCropBean() {
		super();
		newImageName = "ajaxloading.gif";
		newLabel = new Label();
		logger.log(Level.INFO, "Instantiating " +  this.toString());
	}

	public boolean isRenderable() {
		return renderable;
	}

	public int getTop() {
		int result = 0;
		if (croppedImage != null) {
			result = croppedImage.getTop();
		}
		return result;
	}

	public int getLeft() {
		int result = 0;
		if (croppedImage != null) {
			result = croppedImage.getLeft();
		}
		return result;
	}

	public int getHeight() {
		int result = 0;
		if (croppedImage != null) {
			result = croppedImage.getHeight();
		}
		return result;
	}

	public int getWidth() {
		int result = 0;
		if (croppedImage != null) {
			result = croppedImage.getWidth();
		}
		return result;
	}
	
	public int getZoomHeight() {
		int result = 0;
		if (croppedImage != null) {
			result = croppedImage.getHeight();
			result = result * ZOOMFACTOR;
		}
		return result;
	}

	public int getZoomWidth() {
		int result = 0;
		if (croppedImage != null) {
			result = croppedImage.getWidth();
			result = result * ZOOMFACTOR;
		}
		return result;
	}	

	public String getNewImageName() {
		return newImageName;
	}

	public void setNewImageName(String newFileName) {
		this.newImageName = newFileName;
	}


	public CroppedImage getCroppedImage() {
		logger.log(Level.INFO, "called getCroppedImage");
		if (croppedImage==null) {
		    logger.log(Level.INFO, "croppedImage==null");
		} else {
		    logger.log(Level.INFO, croppedImage.getOriginalFilename());
		}
		return croppedImage;
	}

	public void setCroppedImage(CroppedImage croppedImage) {
		logger.log(Level.INFO, "called setCroppedImage");
		if (croppedImage!=null) { 
		    logger.log(Level.INFO, "Image size: " + croppedImage.getWidth() + "x" + croppedImage.getHeight());
		} else { 
		    logger.log(Level.WARNING, "called setCroppedImage, but croppedImage is null");
		}
		this.croppedImage = croppedImage;
	}

	public Label getNewLabel() {
		return newLabel;
	}

	public void setNewLabel(Label newLabel) {
		this.newLabel = newLabel;
	}

	public String getSaveResults() {
		return saveResults;
	}


	/**
	 * Obtain the part of the url in the request that preceeds the context path,
	 * e.g. http://www.example.com:8080/
	 * Used to create a p:ImageCropper.image that starts with http to allow dynamic images.
	 *
	 * @return the scheme, servername and path in the form of an url.
	 */
	public String getRequestUrl() {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		return req.getScheme() + "://" + req.getServerName() + ":" + req.getLocalPort();
	}

	public boolean isShowNewLabelDialog() {
		return showNewLabelDialog;
	}

	public String hideNewLabelDialog() {
		showNewLabelDialog = false;
		return null;
	}

	/** converts theCroppedImage coordinates from the local scaled coordinate system of the image shown in the
	 * ImageCropper to pixel coordinates in the scale of the original image in the frame of reference of the
	 * area of the image shown in the ImageCropper.  Measures of the offset of the part of the image shown
	 * in the ImageCropper from the upper left corner of the original image are obtained but not used for
	 * coordinate conversion in this step.  Consider (a) the original image, (b) the region of the original image
	 * (e.g. pin label) shown, (c) the region of the original image shown and scaled, (d) the cropped region of interest
	 * (e.g. an individual label).  This step converts the description of the cropped region from c to b.
	 * The Scale factor is calculated from the width of the image shown in the ImageCropper and the width of the
	 * selected area from the template that applies to the image.
	 *
	 * @param area the portion of the template delimiting the part of the whole image shown in the ImageCropper.
	 * @param width  The width at which the image shown in the ImageCropper is displayed
	 * @param targetImageId  The image from which the croppedImage is drawn.
	 * @return null
	 */
	public String crop(String area, String width, String targetImageId) {
		showNewLabelDialog = false;

		newLabel = new Label();

		logger.log(Level.INFO, "called crop " + this.toString());
		if (croppedImage==null) { 
			logger.log(Level.WARNING, "Cropped image is null");
		} else { 
			logger.log(Level.INFO,"ImageID: " + targetImageId);
			logger.log(Level.INFO, "croppedImage:{0}, {1}, {2}, {3}", new Object[]{croppedImage.getHeight(), croppedImage.getWidth(), croppedImage.getTop(), croppedImage.getLeft()});

			// lookup image and template in database and work out the scale factor for the crop.
			Image i = imageFacade.find(Long.parseLong(targetImageId));
			logger.log(Level.INFO,"TemplateID: " + i.getTemplateId());
			PositionTemplate t = new PositionTemplate();
			t = templateFacade.getPositionTemplateByID(i.getTemplateId());
//			try { 
//			     t = positionTemplateFacade.findPositionTemplate(i.getTemplateId());
//			} catch (Exception e) { 
//			     t.loadByTemplateId(i.getTemplateId());
//			}
			logger.log(Level.INFO,"Template: " +t.getName());
			logger.log(Level.INFO,"Area: " + area);
			if (area.equals(PositionTemplate.REGION_PINLABELS)) {
				scaleFactor = t.getLabelSize().width / Float.parseFloat(width);
				regionXOffset = t.getLabelPosition().width;
				regionYOffset = t.getLabelPosition().height;
			}
			if (area.equals(PositionTemplate.REGION_LOOSEUT)) {
				scaleFactor = t.getUTLabelsSize().width / Float.parseFloat(width);
				regionXOffset = t.getUTLabelsPosition().width;
				regionYOffset = t.getUTLabelsPosition().height;
			}
			if (area.equals(PositionTemplate.REGION_SPECIMEN)) {
				scaleFactor = t.getSpecimenSize().width / Float.parseFloat(width);
				regionXOffset = t.getSpecimenPosition().width;
				regionYOffset = t.getSpecimenPosition().height;
			}
			logger.log(Level.INFO,"Scale: " + scaleFactor);
			croppedImage.setWidth(Math.round(croppedImage.getWidth() * scaleFactor));
			croppedImage.setHeight(Math.round(croppedImage.getHeight() * scaleFactor));
			croppedImage.setTop(Math.round(croppedImage.getTop() * scaleFactor));
			croppedImage.setLeft(Math.round(croppedImage.getLeft() * scaleFactor));

			logger.log(Level.INFO, "scaled croppedImage:{0}, {1}, {2}, {3}", new Object[]{croppedImage.getHeight(), croppedImage.getWidth(), croppedImage.getTop(), croppedImage.getLeft()});

			renderable = true;
			this.targetImageID = targetImageId;

			ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			String newFileName = servletContext.getRealPath("") + File.separator + "resources" + File.separator + "images" + File.separator + "croppedImage.jpg";
			logger.log(Level.INFO, newFileName);
			newImageName = "croppedImage.jpg";
            this.saveResults = "";

            createExemplarFromCrop(false);
		}

		return null;
	}

	/**
	 * Performed after crop(), creates a new label object with heights and widths and offsets
	 * of the cropped object in the frame of reference of the original image This converts the
	 * definition of c from b to a as described in crop().
	 *
	 * This does not change the coordinates of croppedImage, just of the new Label record.
	 *
	 * @return null
	 * @see ImageCropBean.crop()
	 */
	public String createExemplarFromCrop(boolean rescale) {
		showNewLabelDialog = true;
		logger.log(Level.INFO, "Creating exemplar." + this.toString());
		if (renderable) {
			newLabel = new Label();
			Image img = null;
		    logger.log(Level.INFO, "ImageID (target): " + targetImageID);
			List<Image> lst = imageFacade.findByImageId(Long.parseLong(targetImageID));
			if (!lst.isEmpty()) {
				img = lst.get(0);
		        logger.log(Level.INFO, "ImageID: (retrieved) " +  img.getImageId().toString());
			}
			if (img != null) {
				newLabel.setImageid(img);
				// Cropped image coordinates are not scaled to coordinate system of original image.
		        logger.log(Level.INFO, "scaled croppedImage to store:{0}, {1}, {2}, {3}", new Object[]{croppedImage.getHeight(), croppedImage.getWidth(), croppedImage.getTop(), croppedImage.getLeft()});
		        if (rescale) { 
				    newLabel.setHeight(Math.round(croppedImage.getHeight() * scaleFactor));
				    newLabel.setWidth(Math.round(croppedImage.getWidth() * scaleFactor));
				    newLabel.setOffsetleft(Math.round(croppedImage.getLeft() * scaleFactor) + regionXOffset);
				    newLabel.setOffsettop(Math.round(croppedImage.getTop() * scaleFactor) + regionYOffset);
		        } else { 
				    newLabel.setHeight(croppedImage.getHeight());
				    newLabel.setWidth(croppedImage.getWidth());
				    newLabel.setOffsetleft(croppedImage.getLeft() + regionXOffset);
				    newLabel.setOffsettop(croppedImage.getTop() + regionYOffset);
		        }
		        logger.log(Level.INFO, "Height:" +  newLabel.getHeight().toString());
		        logger.log(Level.INFO, "Rescaled Label to store:{0}, {1}, {2}, {3}", new Object[]{newLabel.getHeight(), newLabel.getWidth(), newLabel.getOffsettop(), newLabel.getOffsetleft()});
			}
		}
		logger.log(Level.INFO, "Done creating exemplar.");
		return null;
	}

	/**
	 * Performed after createExemplarFromCrop(), stores the new label record in the database.
	 *
	 * @return null
	 * @see ImageCropBean.createExemplarFromCrop();
	 */
	public String persistNewLabel() { 
		logger.log(Level.INFO, "Persisting exemplar.");
		logger.log(Level.INFO, "Height:" +  newLabel.getHeight().toString());
		logger.log(Level.INFO, "Rescaled Label to persist:{0}, {1}, {2}, {3}", new Object[]{newLabel.getHeight(), newLabel.getWidth(), newLabel.getOffsettop(), newLabel.getOffsetleft()});
        labelFacade.create(newLabel);
		logger.log(Level.INFO, "imageid: " + newLabel.getImageid().getImageId().toString());
		showNewLabelDialog = false;
	    this.renderable = false;
		this.saveResults = "Label saved.";
		return null;
	}

	/**
	 * Sets boolean values to hide the new label dialog and disable the make exemplar button.
	 *
	 * @return null
	 */
    public String cancelNewLabel() {
		this.hideNewLabelDialog();
		this.renderable = false;
		this.saveResults = "Canceled";
		return null;
	}

}
