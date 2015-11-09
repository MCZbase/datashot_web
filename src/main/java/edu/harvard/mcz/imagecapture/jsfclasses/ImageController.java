package edu.harvard.mcz.imagecapture.jsfclasses;

import edu.harvard.mcz.imagecapture.ImageCaptureProperties;
import edu.harvard.mcz.imagecapture.data.Image;
import edu.harvard.mcz.imagecapture.jsfclasses.util.JsfUtil;
import edu.harvard.mcz.imagecapture.jsfclasses.util.PaginationHelper;
import edu.harvard.mcz.imagecapture.ejb.ImageFacadeLocal;
import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

//import net.imglib2.exception.IncompatibleTypeException;
//import net.imglib2.img.Img;
//import net.imglib2.img.ImgFactory;
//import net.imglib2.img.array.ArrayImgFactory;
//import net.imglib2.io.ImgIOException;
//import net.imglib2.io.ImgOpener;
//import net.imglib2.type.numeric.RealType;
//import net.imglib2.type.numeric.integer.UnsignedByteType;
//import net.imglib2.util.RealSum;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean (name="imageController")
@SessionScoped
public class ImageController {


    private final static Logger logger = Logger.getLogger(ImageController.class.getName());

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

    public ImageController() {
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

	/**
	 * @see edu.harvard.mcz.imagecapture.web.util.ImageServlet
	 *
	 * @deprecated
	 * @return image as StreamedContent
	 */
    public StreamedContent getImage() {
		StreamedContent image;
		String imageid = FacesContext.getCurrentInstance()
                       .getExternalContext().getRequestParameterMap().get("imageid");
        logger.log(Level.INFO, "Requesting imageid:" + imageid);
		if (imageid==null) {
				imageid = "268";
		}
		List<Image> matches = ejbFacade.findByImageId(Long.parseLong(imageid));
		String filename;
		if (!matches.isEmpty()) {
			Image anImage = matches.get(0);
			filename = ImageCaptureProperties.assemblePathWithBase(anImage.getPath(), anImage.getFilename());
		} else {
			filename = "brokenimage.jpg";
		}
        logger.log(Level.INFO, "Requesting image:" + filename);
		InputStream stream = this.getClass().getResourceAsStream(filename);
		image = new DefaultStreamedContent(stream, "image/jpeg");
        logger.log(Level.INFO, "Created stream and streamedcontent object:" + image.toString() + " " + image.getContentType());
		return image;
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
						
//TODO: There's an ORA-01460 getting thrown for a list of 100
//      Might be a driver version missmatch 
//      Handle and fail gracefully?			
						
						// This is handled as a special case in AbstractFacade.
 						filters.put("magic_ImageBarcodeList", barcodeListFilterCriterion);
					}
					return filters;
				}
                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
					DataModel result = null;
					int[] range = new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()};
                        ArrayList<String> sortFields = new ArrayList();
						if (isSortByBarcode()) {
							sortFields.add("rawBarcode");
						}
						if (isSortByFilename()) {
							sortFields.add("filename");
						}
					Map<String, String> filters = getFilterMap();
					boolean useAnd = false; // because of rawBardcode and exifBarcode use or not and.
					result = new ListDataModel(
								getFacade().findRangeQueryAndOr(
								range, sortFields.toArray(new String[0]), true, filters,useAnd));
					logger.log(Level.INFO, "Found " + result.getRowCount() + " images.");					
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
		return "List?faces-redirect=true";
	}

	public String sameReSortBarcodes() {
		pagesize = 100;
		pagination = null;
		items = null;
		getPagination().createPageDataModel();
		recreateModel();
		return "ListByBarcodes?faces-redirect=true";
	}

    public String prepareBarcodesListFromIndex() {
		pagesize = 100;
		recreateModel();
		return "/lepidoptera/image/ListByBarcodes?faces-redirect=true";
	}

    public String prepareList() {
		pagesize = 25;
        recreateModel();
        return "List?faces-redirect=true";
    }

	public String prepareListAll() {
		pagesize = 25;
		pagination = null;
		items = null;
		resetFilters();
		getPagination().createPageDataModel();
		recreateModel();
		return "List?faces-redirect=true";
	}

	public String prepareListAllBarcodes() {
		pagesize = 100;
		pagination = null;
		items = null;
		resetFilters();
		getPagination().createPageDataModel();
		recreateModel();
		return "ListByBarcodes?faces-redirect=true";
	}

	public void resetFilters() {
		filenameFilterCriterion = null;
	}

    public String prepareView() {
        current = (Image)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View?faces-redirect=true";
    }

	public String prepareEditSpecimen() {
		try {
            current = (Image)getItems().getRowData();
            selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
		} catch (Exception e) {
			// expected if not coming off of a list of items.
		}
		specimenController.setSelected(current.getSpecimenId());
        return "/lepidoptera/specimen/Edit?faces-redirect=true";
	}

	@DenyAll
    public String prepareCreate() {
        current = new Image();
        selectedItemIndex = -1;
        return "Create?faces-redirect=true";
    }

	@DenyAll
    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ImageCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

	@RolesAllowed("Administrator")
    public String prepareEdit() {
        current = (Image)getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit?faces-redirect=true";
    }

	@RolesAllowed("Administrator")
    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("ImageUpdated"));
            return "View?faces-redirect=true";
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
        return "List";
    }

	@RolesAllowed("Administrator")
    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View?faces-redirect=true";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List?faces-redirect=true";
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
        return "List?faces-redirect=true";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List?faces-redirect=true";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    @FacesConverter(forClass=Image.class)
    public static class ImageControllerConverter implements Converter {

        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ImageController controller = (ImageController)facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "imageController");
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
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: "+ImageController.class.getName());
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
	
	/**
	 * Testing link from InetAddress.
	 * 
	 * TODO: Refactor as composeImageLink?
	 * 
	 * @return
	 */
	public String getImageURI() {
		String result = "";
		try {
			InetAddress a = InetAddress.getLocalHost();
		    if (a.getHostAddress().equals("127.0.0.1")) {
		    	// Handle alternate host names for localhost
			    result = "http://localhost";
		    } else { 
			    result = "http://" + a.getHostName();
		    }
		} catch (UnknownHostException e) {
			logger.log(Level.SEVERE, e.getMessage(),e);
		}
		return result;
	}
	
	
//    <dependency>
//    <groupId>net.imglib2</groupId>
//    <artifactId>imglib2</artifactId>
//    <version>2.0.0-SNAPSHOT</version>
//</dependency>
//<dependency>
//    <groupId>net.imglib2</groupId>
//    <artifactId>imglib2-io</artifactId>
//    <version>2.0.0-SNAPSHOT</version>
//</dependency>
//
//<dependency>
//    <groupId>loci</groupId>
//    <artifactId>bio-formats</artifactId>
//    <version>4.5-imagej-2.0.0-beta6</version>
//</dependency>	
	
//	@SuppressWarnings("deprecation")
//	public String imageLibTest() { 
//		String result = "";
//		String url = this.getImageURI() + "/imageserver/image.php?imageid=316&region=PinLabels";
//		try {
//
//			//Img<UnsignedByteType> img = new ImgOpener().openImg(url, new ArrayImgFactory<UnsignedByteType>(), new UnsignedByteType());
//			Img<UnsignedByteType> img = new ImgOpener().openImg(url);
//			
//			final RealSum realSum = new RealSum();
//			long count = 0;
//			
//			for ( final UnsignedByteType type : img )
//			{
//				realSum.add( type.getRealDouble() );
//				++count;
//			}	
//			result = Double.toString(realSum.getSum()/count);
//			
//		} catch (ImgIOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IncompatibleTypeException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return result;
//	}

}
