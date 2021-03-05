package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.ImageCaptureProperties;
import edu.harvard.mcz.imagecapture.data.Image;
import edu.harvard.mcz.imagecapture.jsfclasses.util.JsfUtil;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.ejb.ImageFacadeLocal;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.faces.bean.SessionScoped;
import jakarta.faces.FacesException;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;

//@Named("imageProblemController")
@ManagedBean
@SessionScoped
public class ImageProblemController implements Serializable {

	private static final long serialVersionUID = -5658082677712185069L;

	private final static Logger logger = Logger.getLogger(ImageProblemController.class.getName());

    private Image current;
    private DataModel<Image> items = null;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    @EJB private edu.harvard.mcz.imagecapture.ejb.ImageFacadeLocal ejbFacade;

	@ManagedProperty(value="#{specimenController}")
	private edu.harvard.mcz.imagecapture.jsfclasses.SpecimenController specimenController;

	private boolean sortByBarcode;
	private boolean sortByFilename;

	private String filenameFilterCriterion;
	private String barcodeFilterCriterion;
	private String barcodeListFilterCriterion;
	private boolean barcodeListAsLinks;

	private int pagesize = 25;

    public ImageProblemController() {
		super();
		setDefaultSort();
    }

	/**
	 * Required setter method for ManagedProperty
	 *
	 * @param specimenController
	 */
	public void setSpecimenController(SpecimenController specimenController) {
		this.specimenController = specimenController;
	}


    public Image getSelected() {
        if (current == null) {
            current = new Image();
            selectedItemIndex = -1;
        }
        return current;
    }

    private ImageFacadeLocal getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(pagesize) {
            	
                private int count = 0;

				public Map<String, String> getFilterMap() {
					Map<String, String> filters = new HashMap<String, String>();
					if (barcodeFilterCriterion != null) {
						filters.put("rawExifBarcode", barcodeFilterCriterion);
						filters.put("rawBarcode", barcodeFilterCriterion);
					}
					if (filenameFilterCriterion != null) {
						filters.put("filename", filenameFilterCriterion);
					}
					if (barcodeListFilterCriterion != null) {
						// This is handled as a special case in AbstractFacade.
 						filters.put("magic_ImageBarcodeList", barcodeListFilterCriterion);
					}
					return filters;
				}
                @Override
                public int getItemsCount() {
                    return count;
                }

                @Override
                public DataModel createPageDataModel() {
					DataModel result = null;
					int[] range = new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()};
                    ArrayList<String> sortFields = new ArrayList<String>();
					if (isSortByBarcode()) {
							sortFields.add("rawBarcode");
					}
					if (isSortByFilename()) {
							sortFields.add("filename");
					}
					Map<String, String> filters = getFilterMap();
					boolean useAnd = false; // because of rawBardcode and exifBarcode use or not and.
			        List<Image> matches = ejbFacade.findNullBarcodes(filters,sortFields);
					matches.addAll(ejbFacade.findUntemplatedImages(filters, sortFields));
					count = 0;
					if (matches!=null) { 
						count = matches.size();
						result = new ListDataModel<Image>(matches);
					}
					logger.log(Level.INFO, "Found " + count + " null barcodes/untemplated images.");					
                    return result;
                }                
            };
        }
        return pagination;
    }

	public boolean isSortByBarcode() {
		return sortByBarcode;
	}

	public void setSortByBarcode(boolean sortByBarcode) {
		this.sortByBarcode = sortByBarcode;
	}

	public boolean isSortByFilename() {
		return sortByFilename;
	}

	public void setSortByFilename(boolean sortByFilename) {
		this.sortByFilename = sortByFilename;
	}

		/** Re-render the same list, but resorted using the current sortBy and filterBy criteria.
	 *
	 * @return faces navigation string, List?faces-redirect=true.
	 */
	public String sameReSort() {
		pagesize = 25;
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "ListProblems?faces-redirect=true";
	}

    public String prepareList() {
        return "List?faces-redirect=true";
    }

	public String prepareListAll() {
		pagesize = 25;
		pagination = null;
		items = null;
		resetFilters();
		getPagination().createPageDataModel();
		recreateModel();
		return "ListProblems?faces-redirect=true";
	}

	public String prepareListAllBarcodes() {
		return "ListByBarcodes?faces-redirect=true";
	}

	public void resetFilters() {
		filenameFilterCriterion = null;
	}

    public String prepareView() {
        current = (Image)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "ViewProblem?faces-redirect=true";
    }

    // TODO: Not working.
	public String prepareEditSpecimen() {
		try {
            current = (Image)getItems().getRowData();
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
		} catch (Exception e) {
			// expected if not coming off of a list of items.
		}
		specimenController.setSelected(current.getSpecimenId());
        return "/lepidoptera/specimen?faces-redirect=true";
	}

	@RolesAllowed("Administrator")
    public String prepareEdit() {
		try { 
             current = (Image)getItems().getRowData();
		} catch (FacesException e) { 
             logger.log(Level.WARNING, e.getMessage());
		}
        logger.log(Level.INFO, current.toString());
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        logger.log(Level.INFO, "Selected item index = " + selectedItemIndex);
        return "EditProblem?faces-redirect=true";
    }

	@RolesAllowed("Administrator")
    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ImageUpdated"));
            return "ViewProblem?faces-redirect=true";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

	@RolesAllowed("Administrator")
    public String destroy() {
        current = (Image)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreateModel();
        return "ListProblems";
    }

	@RolesAllowed("Administrator")
    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "ViewProblem?faces-redirect=true";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "ListProblems?faces-redirect=true";
        }
    }

	@RolesAllowed("Administrator")
    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ImageDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count-1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex+1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            logger.log(Level.INFO,"Creating model for Problem images");
            items = getPagination().createPageDataModel();
        }
        return items;
    }


	public String prepareProblemView() {
		return "/lepidoptera/image/ListProblems?faces-redirect=true";
	}

    private void recreateModel() {
        items = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "ListProblems?faces-redirect=true";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "ListProblems?faces-redirect=true";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    @FacesConverter(forClass=Image.class)
    public static class ImageProblemControllerConverter implements Converter {

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ImageProblemController controller = (ImageProblemController)facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "imageProblemController");
            return controller.ejbFacade.find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuffer sb = new StringBuffer();
            sb.append(value);
            return sb.toString();
        }

        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Image) {
                Image o = (Image) object;
                return getStringKey(o.getImageId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: "+ImageProblemController.class.getName());
            }
        }

    }

	public String getFilenameFilterCriterion() {
		return filenameFilterCriterion;
	}

	public void setFilenameFilterCriterion(String filenameFilterCriterion) {
		this.filenameFilterCriterion = filenameFilterCriterion;
	}

	public String getBarcodeFilterCriterion() {
		return barcodeFilterCriterion;
	}

	public void setBarcodeFilterCriterion(String barcodeFilterCriterion) {
		this.barcodeFilterCriterion = barcodeFilterCriterion;
	}

	public String getBarcodeListFilterCriterion() {
		return barcodeListFilterCriterion;
	}

	public void setBarcodeListFilterCriterion(String barcodeListFilterCriterion) {
		this.barcodeListFilterCriterion = barcodeListFilterCriterion;
	}

	public boolean isBarcodeListAsLinks() {
		return barcodeListAsLinks;
	}

	public void setBarcodeListAsLinks(boolean barcodeListAsLinks) {
		this.barcodeListAsLinks = barcodeListAsLinks;
	}

	public int getPagesize() {
		return pagesize;
	}

	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}

	/**  Sets the sortBy_ variables to their default values.
	 *
	 */
	private void setDefaultSort() {
		sortByBarcode = true;
		sortByFilename = true;
		barcodeListAsLinks = true;
	}
	/** Sets all the sortBy_ variables to false, removing any
	 * sort criteria.
	 */
	private void setSortOff() {
		sortByBarcode = false;
		sortByFilename = false;
	}


	public String composeImageDownloadLink(String imageid) {
		logger.log(Level.INFO, imageid);
		return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/ImageServlet?imageid=" + imageid + "&download=true";
	}

	public boolean isFileReadable() {
		boolean result = false;
		String filename = ImageCaptureProperties.assemblePathWithBase(current.getPath(), current.getFilename());
	    File file = new File(filename);
		if (file.canRead()) {
			result = true;
		}
		return result;
	}

}
