/*
* Copyright (c) 2006-2010 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/


package com.nokia.tools.theme.s60.parser;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;

import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.symbian.mbm.BitmapConverter;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.Orientation;
import com.nokia.tools.platform.theme.AnimatedThemeGraphic;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerEffect;
import com.nokia.tools.platform.theme.ParameterModel;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeGraphicInterface;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.resource.util.XmlUtil;
import com.nokia.tools.theme.core.MultiPieceManager;
import com.nokia.tools.theme.s60.cstore.ComponentStore;
import com.nokia.tools.theme.s60.editing.EditableAnimationFrame;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.editing.EditableEntityImageFactory;
import com.nokia.tools.theme.s60.general.ThemeUtils;
import com.nokia.tools.theme.s60.model.MorphedGraphic;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.nokia.tools.theme.s60.morphing.timemodels.BaseTimingModelInterface;
import com.nokia.tools.theme.s60.morphing.valuemodels.BaseValueModelInterface;

/**
 * This class creates an XML file from S60Skin object. The XML file contains
 * skin details.
 */
public class ThemeWriter {

	/** Variable that holds a reference to S60Skin object */
	private S60Theme skin = null;

	// Elements of S60Skin in DOM
	private DocumentBuilderFactory factory = null;

	private DocumentBuilder builder = null;

	private Document skinDetailsDocument = null;

	private org.w3c.dom.Element skinElement = null;

	private org.w3c.dom.Element elementElement = null;

	private org.w3c.dom.Element partElement = null;

	private org.w3c.dom.Element graphicElement = null;

	private org.w3c.dom.Element imageElement = null;

	private org.w3c.dom.Element layerElement = null;

	private org.w3c.dom.Element effectElement = null;

	private org.w3c.dom.Element paramElement = null;

	private org.w3c.dom.Element valuemodelElement = null;

	private org.w3c.dom.Element timingmodelElement = null;

	private org.w3c.dom.Element valuemodelsElement = null;

	private org.w3c.dom.Element timingmodelsElement = null;

	private Vector<Object> elementList = new Vector<Object>();

	private IProgressMonitor monitor;

	/**
	 * Constructor of the class.
	 * 
	 * @param skin S60Skin object
	 */

	public ThemeWriter(S60Theme skin, IProgressMonitor monitor) {
		this.skin = skin;
		this.monitor = monitor;
	}

	/**
	 * Method to initialize the DocumentFactory, DocumentBuilder and create
	 * Document object.
	 */
	public void init() {
		try {

			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();

			DOMImplementation implementation = builder.getDOMImplementation();

			DocumentType documentType = implementation.createDocumentType(
			    "skin", "", "http://abc.com/skindata.dtd");
			skinDetailsDocument = implementation.createDocument("",
			    ThemeTag.ELEMENT_ROOT, documentType);
			monitor.worked(10);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
	}

	/**
	 * Method that create XML file from S60Skin
	 */
	public void createXMLFromS60Skin() throws Exception {
		
		// handle colourize / optimize based elements - saves images
		// re-save PNGs as BMPs + Mask
		handlePNGs(skin, monitor);

		createDOMRoot();
		appendPhoneSettingsNodes();
		monitor.worked(10);

		try {
			appendElementPartNodes();
		} catch (ThemeException sae) {
			sae.printStackTrace();
		}
		monitor.worked(10);

		writeDOMToXMLFile();
		monitor.worked(10);

		for (int ctr = 0; ctr < elementList.size(); ctr++) {
			Element e = (Element) elementList.elementAt(ctr);
			e.setAttribute(ThemeTag.ATTR_SHOW, ThemeTag.ATTR_SHOW_FALSE);
		}
	}

	/**
	 * when image is png, it is re-saved as BMP + MASK BMP
	 * 
	 * @throws ThemeException
	 */
	private void handlePNGs(S60Theme skin2, IProgressMonitor monitor)
	    throws ThemeException {
		String themeDir = skin2.getThemeDir();
		for (Object e : skin2.getAllElements(true)) {
			if (e instanceof Element) {
				Element elem = (Element) e;
				if (elem.isEntityType().equals(ThemeTag.ELEMENT_SOUND)
				    || elem.isEntityType().equals(ThemeTag.ELEMENT_EMBED_FILE))
					continue;

				if (elem.getActualThemeGraphic() == null
					&& !(MultiPieceManager.isMultiPiece(elem.getCurrentProperty())))
				  
					continue;

				if (elem.getActualThemeGraphic() != null) {

					List layers = elem.getActualThemeGraphic().getImageLayers();
					for (Object l : layers) {
						ImageLayer layer = (ImageLayer) l;

						if ("true".equals(layer
						    .getAttribute(ThemeTag.ATTR_TMP_IMAGE))) {
							handleTmpImageConversion(layer, themeDir, elem);
							layer.removeAttribute(ThemeTag.ATTR_TMP_IMAGE);
						}

						if ("true".equals(layer
						    .getAttribute(ThemeTag.ATTR_TMP_MASK_IMAGE))) {
							// layer has mask, save mask as BMP to theme dir
							handleTmpMaskConversion(layer, themeDir, elem);
							layer.removeAttribute(ThemeTag.ATTR_TMP_MASK_IMAGE);
						}

					}
				}

				// 9-piece:
				if (elem.getChildren() != null && elem.getChildren().size() > 0) {
					for (Object children : elem.getChildren()) {
						if (children instanceof Part) {
							Part _part = (Part) children;

							if (_part.getActualThemeGraphic() == null)
								continue;

							List<ImageLayer> layers = _part
							    .getActualThemeGraphic().getImageLayers();
							for (Object l : layers) {
								ImageLayer layer = (ImageLayer) l;

								if ("true".equals(layer
								    .getAttribute(ThemeTag.ATTR_TMP_IMAGE))) {
									handleTmpImageConversion(layer, themeDir,
									    _part);
									layer
									    .removeAttribute(ThemeTag.ATTR_TMP_IMAGE);
								}

								if ("true"
								    .equals(layer
								        .getAttribute(ThemeTag.ATTR_TMP_MASK_IMAGE))) {
									// layer has mask, save mask as BMP to theme
									// dir
									handleTmpMaskConversion(layer, themeDir,
									    _part);
									layer
									    .removeAttribute(ThemeTag.ATTR_TMP_MASK_IMAGE);
								}
							}
						}
					}
				}
			}
			monitor.worked(10);
		}
	}

	private void handleTmpMaskConversion(ImageLayer layer, String themeDir,
	    SkinnableEntity elem) {
		String MASK_ATTRIBUTE = elem.getToolBox().SoftMask ? ThemeTag.ATTR_SOFTMASK
		    : ThemeTag.ATTR_HARDMASK;

		String hardMask = layer.getAttribute(ThemeTag.ATTR_HARDMASK);
		String softMask = layer.getAttribute(ThemeTag.ATTR_SOFTMASK);
		String filename = layer.getAttribute(MASK_ATTRIBUTE);
		if (hardMask != null && hardMask.contains(File.separator))
			filename = hardMask;
		if (softMask != null && softMask.contains(File.separator))
			filename = softMask;

		if (!StringUtils.isEmpty(filename)) {
			if (true) {
				// save this mask png as BMP to theme dir
				try {
					File maskFile = new File(filename);
					if (!maskFile.exists()) {
						maskFile = new File(themeDir + File.separator
						    + filename);
					}
					if (!maskFile.exists()) {
						layer.removeAttribute(MASK_ATTRIBUTE);
						return;
					}

					boolean singleLayer = false;
					if (layer.getThemeGraphic().getImageLayers().size() < 2)
						singleLayer = true;

					String maskTmpName;
					if (singleLayer) {

						if (elem instanceof Part
						    && ComponentStore.isComponentStoreElement(elem
						        .getParent().getId())) {
							/* component store item */
							maskTmpName = extractNameFromComponentStoreElement(
							    filename, maskFile, themeDir, elem);
						} else {
							maskTmpName = extractNameFromElement(filename,
							    maskFile, themeDir, elem);
						}

					} else {
						maskTmpName = extractNameForMultilayerElement(filename,
						    maskFile, layer, themeDir, elem);
					}

					if (!maskTmpName.endsWith(ThemeTag.FILE_TYPE_BMP))
						maskTmpName += ThemeTag.FILE_TYPE_BMP;

					CoreImage maskBi = CoreImage.create(ImageIO.read(maskFile));
					// convert mask to gray and select one band = 8 bit mask
					if (!maskBi.isImageGrayScaleImage()) {
						// mask changed - save
						maskBi.convertToGrayScale().save(
						    CoreImage.TYPE_BMP,
						    FileUtils.createFileWithExtension(themeDir
						        + File.separator + maskTmpName,
						        IFileConstants.FILE_EXT_BMP));
					} else {
						// copy mask only
						FileUtils.copyFile(maskFile, new File(themeDir
						    + File.separator + maskTmpName));
					}

					layer.setAttribute(MASK_ATTRIBUTE, maskTmpName);

				} catch (Exception es) {
					es.printStackTrace();
				}

			}
		}
	}

	private String extractNameForMultilayerElement(String filename,
	    File maskFile, ImageLayer layer, String themeDir, SkinnableEntity elem) {
		String nonUniqueFileName = ThemeUtils.generateNonUniqueFileName(null,
		    elem.getId() + "_" + layer.getAttribute(ThemeTag.ATTR_NAME)
		        + ThemeTag.MASK_FILE, themeDir);
		return nonUniqueName(filename, maskFile, nonUniqueFileName);
	}

	private String extractNameFromElement(String filename, File maskFile,
	    String themeDir, SkinnableEntity elem) {
		String nonUniqueFileName = ThemeUtils.generateNonUniqueFileName(null,
		    elem.getId() + ThemeTag.MASK_FILE, themeDir);
		return nonUniqueName(filename, maskFile, nonUniqueFileName);
	}

	private String extractNameFromComponentStoreElement(String filename,
	    File maskFile, String themeDir, SkinnableEntity elem) {
		String nonUniqueFileName = ThemeUtils.generateNonUniqueFileName(null,
		    elem.getParent().getId() + elem.getId() + ThemeTag.MASK_FILE,
		    themeDir);
		return nonUniqueName(filename, maskFile, nonUniqueFileName);
	}

	private String nonUniqueName(String filename, File maskFile,
	    String nonUniqueFileName) {
		if (filename.endsWith(nonUniqueFileName))
			return nonUniqueFileName;
		return maskFile.getName();
	}

	private void handleTmpImageConversion(ImageLayer layer, String themeDir,
	    SkinnableEntity elem) {
		String filename = layer.getAttribute(ThemeTag.FILE_NAME);
		if (!StringUtils.isEmpty(filename)) {

			boolean singleLayer = false;
			try {
				if (layer.getThemeGraphic().getImageLayers().size() < 2)
					singleLayer = true;
			} catch (Exception e) {
			}

			/* if filename is not BMP or SVG, convert to BMP */

			if (!(filename.toLowerCase().endsWith(ThemeTag.SVG_FILE_EXTN) || filename
			    .toLowerCase().endsWith(ThemeTag.SVG_TINY_FILE_EXTN))) {

				// this image is not SVG or SVGT - determine real type
				File imageFile = new File(filename);
				if (!imageFile.exists()) {
					imageFile = new File(themeDir + File.separator + filename);
				}
				if (!imageFile.exists()) {
					layer.removeAttribute(ThemeTag.FILE_NAME);
					layer.removeAttribute(ThemeTag.ATTR_HARDMASK);
					layer.removeAttribute(ThemeTag.ATTR_SOFTMASK);
					return;
				}

				// save this image as BMP and optionally MASK, if
				// not present
				if (!BitmapConverter.isBMPType(imageFile)) {
					try {
						CoreImage image = CoreImage.create().load(imageFile);

						String tmpName;
						if (singleLayer) {

							if (elem instanceof Part
							    && ComponentStore.isComponentStoreElement(elem
							        .getParent().getId())) {
								/* component store item */
								tmpName = ThemeUtils.generateNonUniqueFileName(
								    null, elem.getParent().getId()
								        + elem.getId(), themeDir);
							} else {
								tmpName = ThemeUtils.generateNonUniqueFileName(
								    null, elem.getId(), themeDir);
							}

						} else {
							tmpName = ThemeUtils.generateNonUniqueFileName(
							    null, elem.getId() + "_"
							        + layer.getAttribute(ThemeTag.ATTR_NAME),
							    themeDir);
						}

						if (!tmpName.endsWith(ThemeTag.FILE_TYPE_BMP))
							tmpName += ThemeTag.FILE_TYPE_BMP;

						// skin packager doesn't like '%' int the file name
						tmpName = tmpName.replace("%20", "_");
						tmpName = tmpName.replace("%", "_");

						image.save(CoreImage.TYPE_BMP, FileUtils
						    .createFileWithExtension(themeDir + File.separator
						        + tmpName, IFileConstants.FILE_EXT_BMP));
						layer.setAttribute(ThemeTag.FILE_NAME, tmpName);
						return;

						// saves extracted mask as BMP
												
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}

			/* copy image to theme dir under defined name */
			String tmpName;
			if (singleLayer) {
				if (elem instanceof Part
				    && ComponentStore.isComponentStoreElement(elem.getParent()
				        .getId())) {
					/* component store item */
					tmpName = ThemeUtils.generateNonUniqueFileName(null, elem
					    .getParent().getId()
					    + elem.getId(), themeDir);
				} else {
					tmpName = ThemeUtils.generateNonUniqueFileName(null, elem
					    .getId(), themeDir);
				}
			} else {
				tmpName = ThemeUtils.generateNonUniqueFileName(null, elem
				    .getId()
				    + "_" + layer.getAttribute(ThemeTag.ATTR_NAME), themeDir);
			}

			String extension = FileUtils.getExtension(filename);
			tmpName = tmpName + "." + extension;

			// skin packager doesn't like '%' int the file name
			tmpName = tmpName.replace("%20", "_");
			tmpName = tmpName.replace("%", "_");

			try {
				FileUtils.copyFile(filename, themeDir + File.separator
				    + tmpName);
				layer.setAttribute(ThemeTag.FILE_NAME, tmpName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves 'dirty' images in the element into theme directory, same effect
	 * like if user save changed theme.
	 * 
	 * @param element
	 */
	public static void saveAdjustedImage(SkinnableEntity element) {
		try {

			EditableEntityImage img = (EditableEntityImage) EditableEntityImageFactory
			    .getInstance().createEntityImage(element, null, 0, 0);

			if (img instanceof IAnimatedImage) {
			
				// stretch image
				String heightStr = element
				    .getAttributeValue(BitmapProperties.BITMAP_HEIGHT);
				String widthStr = element
				    .getAttributeValue(BitmapProperties.BITMAP_WIDTH);
				IAnimationFrame[] frames = ((IAnimatedImage) img)
				    .getAnimationFrames();
				for (IAnimationFrame frame : frames) {
					RenderedImage result = null;
					if (heightStr != null && heightStr.length() > 0
					    && widthStr != null && widthStr.length() > 0) {
						int width = Integer.parseInt(widthStr);
						int height = Integer.parseInt(heightStr);
						result = frame.getRAWImage(width, height, true);
					} else {
						result = frame.getRAWImage(true);
					}

					result = ((EditableAnimationFrame) frame)
					    .applyBitmapProperties(result);
					frame.paste(result, null);
				}

				ThemeGraphic g = img.getSavedThemeGraphics(false);
				g
				    .setAttribute(ThemeTag.ATTR_STATUS,
				        ThemeTag.ATTR_VALUE_ACTUAL);
				element.setAnimateGraphic(g);
			} else {
				RenderedImage result = null;

				
				// stretch image
				String heightStr = element
				    .getAttributeValue(BitmapProperties.BITMAP_HEIGHT);
				String widthStr = element
				    .getAttributeValue(BitmapProperties.BITMAP_WIDTH);
				if (heightStr != null && heightStr.length() > 0
				    && widthStr != null && widthStr.length() > 0) {
					int width = Integer.parseInt(widthStr);
					int height = Integer.parseInt(heightStr);
					result = img.getAggregateImage(width, height);
				} else {
					result = img.getAggregateImage();
				}

				img.getLayer(0).clearMask();
				img.getLayer(0).paste(result);

				CoreImage ci = CoreImage.create(result);
				if (ci.getNumBands() == 4) {
					RenderedImage mask = ci.extractMask(
					    element.getToolBox().SoftMask).getAwt();
					if (mask != null) {
						img.getLayer(0).pasteMask(mask);
					}
				}
				ThemeGraphic g = img.getSavedThemeGraphics(false);
				element.setActualGraphic(g);
			}
		} catch (ThemeException e) {
		
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			element.getAttribute().remove(BitmapProperties.BITMAP_HEIGHT);
			element.getAttribute().remove(BitmapProperties.BITMAP_WIDTH);
		}

	}

	/**
	 * Method to create the ROOT node of the DOM tree
	 */
	public void createDOMRoot() {
		// Create the skin tag and set the attributes
		skinElement = skinDetailsDocument.getDocumentElement();
	
		setAttributes(skinElement, (Object) skin);
	}

	/**
	 * Method to append the Phone Settings nodes to DOM tree
	 */
	public void appendPhoneSettingsNodes() {

		// Create the phone and model tags and set the attributes
		org.w3c.dom.Element phoneElement = skinDetailsDocument
		    .createElement(ThemeTag.ELEMENT_PHONE);
		setAttributes(phoneElement);

		for (String modelId : skin.getAllModelIds()) {
			org.w3c.dom.Element modelElement = skinDetailsDocument
			    .createElement(ThemeTag.ELEMENT_MODEL);
			modelElement.setAttribute("id", modelId);
			phoneElement.appendChild(modelElement);
		}
		skinElement.appendChild(phoneElement);
	}

	/**
	 * Method to append Element and Part nodes to DOM tree
	 */
	public void appendElementPartNodes() throws ThemeException {

		/*
		 * if the element or part is referring to another entity do not write the
		 * info in the skin file... if for a part... which is not referring... if
		 * its not having image... do not write the part if for a element... which
		 * is not referring...and is not having parts if its not having image...
		 * do not write the part if for an element with parts... none of the part
		 * is having image... do not write it in the file...
		 */
		Collection elemList = skin.getAllElements(true);

		// /// For writing Line Colours

		HashMap lineColours = skin.getSkinSettings();

		if (lineColours != null) {
			Iterator it = lineColours.keySet().iterator();

			while (it.hasNext()) {
				String key = it.next().toString();
				HashMap temp = (HashMap) lineColours.get(key);
				if ((temp.containsKey(ThemeTag.ATTR_VALUE))
				    || (temp.get(ThemeTag.ATTR_MANDATORY).toString()
				        .equalsIgnoreCase(ThemeTag.SKN_TILE_TRUE))) {
					elementElement = skinDetailsDocument
					    .createElement(ThemeTag.SKN_TAG_DRAWLINES);
					String colVal = temp.get(ThemeTag.DEFAULT).toString();
					if (temp.containsKey(ThemeTag.ATTR_VALUE))
						colVal = temp.get(ThemeTag.ATTR_VALUE).toString();
					elementElement.setAttribute(ThemeTag.ATTR_ID, key);
					elementElement.setAttribute(ThemeTag.ATTR_VALUE, colVal);
					skinElement.appendChild(elementElement);
				}
			}
		}

		for (Object obj : elemList) {

			ThemeBasicData sbdElement = (ThemeBasicData) obj;

			
			
			

			if (sbdElement.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {

				String isShown = (String) sbdElement
				    .getAttributeValue(ThemeTag.ATTR_SHOW);
				if ((isShown != null)
				    && isShown.equalsIgnoreCase(ThemeTag.ATTR_SHOW_FALSE)) {
					String sbdParentId = sbdElement
					    .getAttributeValue(ThemeTag.ATTR_ANIMATE_PARENTID);
					
					ThemeBasicData sbdParent = null;
					if (sbdParentId == null) {
						sbdParent = sbdElement;
					} else {
						sbdParent = skin.getSkinnableEntity(sbdParentId, true);

						String prop = sbdParent.getCurrentProperty();
						
						if ((prop != null)
						    && (prop.equalsIgnoreCase(ThemeTag.ATTR_STILL))) {
							String seqno = (String) sbdElement
							    .getAttributeValue(ThemeTag.ATTR_ANIMATE_SEQNO);
							if (!seqno.equalsIgnoreCase(0 + "")) {
								System.out.println(" Skipping image "
								    + sbdElement.getIdentifier());
								continue;
							}
							System.out.println(" Writing image "
							    + sbdElement.getIdentifier() + " seqno = "
							    + seqno);
						}
					}
				}
			}
			
			boolean shouldSaveElement = true;
			if (sbdElement.getSkinnedStatus().equals(
				ThemeTag.SKN_ATTR_STATUS_NOT_SELECTED)&& !sbdElement.isSkinned()) {
				shouldSaveElement = false;
			}else if(sbdElement.getSkinnedStatus().equals(
				// Element is not skinned but selected
				ThemeTag.SKN_ATTR_STATUS_SELECTED)){
				shouldSaveElement = false;
			}
			
			boolean isAnyChildSkinned = false;
			if (!shouldSaveElement) {
				//Can any child part be saved?				
				if (sbdElement.hasChildNodes()) {
					for(Object sbdChildElement: sbdElement.getChildren()) {
						if (sbdChildElement instanceof Part) {
							isAnyChildSkinned = ((ThemeBasicData) sbdChildElement).isSkinned();	
							if (isAnyChildSkinned) break;
						}
					}
				}
				
				if (!isAnyChildSkinned) continue;
			}
			
			if (shouldSaveElement || isAnyChildSkinned){
				

				if (sbdElement.isLink()) {
					
					continue;
				}
				
				// Create the element tag and set the attributes
				elementElement = skinDetailsDocument
				    .createElement(ThemeTag.ELEMENT_ELEMENT);
				setAttributes(elementElement, sbdElement);
				skinElement.appendChild(elementElement);
				
				boolean isMultipiece = true;
				String bitmapProp = sbdElement.getCurrentProperty(); // single
				// piece
				// or 9
				// pieces
				// bitmap
				
				if (bitmapProp != null)
					isMultipiece = MultiPieceManager.isMultiPiece(bitmapProp);
					

				if (((ThemeBasicData) sbdElement).hasChildNodes()
				    && isMultipiece) { // extract Part information
					
					List partList = ((ThemeBasicData) sbdElement).getChildren();
					if (partList != null) {

						Object[] partArr = partList.toArray();
						if (partArr != null) {

							for (int j = 0; j < partArr.length; ++j) {
								ThemeBasicData sbdPart = (ThemeBasicData) partArr[j];
								if (sbdPart.isLink()) {
									// if its linked to another entity, dont
									// write the
									// info in the skin file
									continue;
								}

								// Create the part tag and set its attributes
								partElement = skinDetailsDocument
								    .createElement(ThemeTag.ELEMENT_PART);
								setAttributes(partElement, partArr[j]);
								elementElement.appendChild(partElement);

								// Create the image tag and set its
								// attributes

								// modified by evsathish
								List imageList = ((SkinnableEntity) partArr[j])
								    .getAllThemeGraphics();
								if (imageList == null || imageList.size() <= 0) {
									// the ctrl comes here
									// when its selected or in fix list but
									// doesnt hav any image
									continue;
								}

								
								for (int k = 0; k < imageList.size(); k++) {
									graphicElement = skinDetailsDocument
									    .createElement(ThemeTag.ELEMENT_GRAPHIC);
									ThemeGraphic tg = (ThemeGraphic) imageList
									    .get(k);

									if (tg != null) {
										
										setAttributes(graphicElement, tg);
										partElement.appendChild(graphicElement);

										imageElement = skinDetailsDocument
										    .createElement(ThemeTag.ELEMENT_IMAGE);
										graphicElement
										    .appendChild(imageElement);
										List tmList = null;
										if (tg.isMorphedGraphic())
											tmList = ((MorphedGraphic) tg)
											    .getTimingModels();
										if (tmList != null) {
											int tmSize = tmList.size();
											if (tmSize > 0) {
												timingmodelsElement = skinDetailsDocument
												    .createElement(ThemeTag.ELEMENT_TIMINGMODELS);
												imageElement
												    .appendChild(timingmodelsElement);
											}
											for (int tm = 0; tm < tmSize; tm++) {
												timingmodelElement = skinDetailsDocument
												    .createElement(ThemeTag.ELEMENT_TIMINGMODEL);
												BaseTimingModelInterface btmi = (BaseTimingModelInterface) tmList
												    .get(tm);
												setAttributes(
												    timingmodelElement, btmi);
												timingmodelsElement
												    .appendChild(timingmodelElement);
											}

											List vmList = ((MorphedGraphic) tg)
											    .getValueModels();
											int vmSize = vmList.size();
											if (vmSize > 0) {
												valuemodelsElement = skinDetailsDocument
												    .createElement(ThemeTag.ELEMENT_VALUEMODELS);
												imageElement
												    .appendChild(valuemodelsElement);
											}
											for (int vm = 0; vm < vmSize; vm++) {
												valuemodelElement = skinDetailsDocument
												    .createElement(ThemeTag.ELEMENT_VALUEMODEL);
												BaseValueModelInterface bvmi = (BaseValueModelInterface) vmList
												    .get(vm);
												setAttributes(
												    valuemodelElement, bvmi);
												valuemodelsElement
												    .appendChild(valuemodelElement);
											}
										}
										int lSize = tg.getImageLayers().size();
										for (int l = 0; l < lSize; l++) {
											layerElement = skinDetailsDocument
											    .createElement(ThemeTag.ELEMENT_LAYER);
											ImageLayer il = (ImageLayer) tg
											    .getImageLayers().get(l);
											if (il != null) {
												Map attrmap = ((ImageLayer) il)
												    .getAttributes();
												if (attrmap
												    .containsKey(ThemeTag.FILE_NAME)) {
													String fileName = (String) attrmap
													    .get(ThemeTag.FILE_NAME);
													if (fileName
													    .lastIndexOf(File.separator) != -1) {
														String fileName1 = (new File(
														    fileName))
														    .getName();
														S60Theme s60 = (S60Theme) ((SkinnableEntity) sbdElement)
														    .getRoot();
														String themeDir = s60
														    .getThemeDir();
														if (!new File(themeDir
														    + File.separator
														    + fileName1)
														    .exists())
															try {
																FileUtils
																    .copyFile(
																        fileName,
																        s60
																            .getThemeDir()
																            + File.separator
																            + fileName1);
															} catch (Exception e) {
																e
																    .printStackTrace();
															}
													}
												}
												setAttributes(layerElement, il);
												imageElement
												    .appendChild(layerElement);

												int eSize = il
												    .getLayerEffects().size();
												for (int m = 0; m < eSize; m++) {
													effectElement = skinDetailsDocument
													    .createElement(ThemeTag.ELEMENT_EFFECT);
													LayerEffect le = (LayerEffect) il
													    .getLayerEffects().get(
													        m);

													setAttributes(
													    effectElement, le);
													layerElement
													    .appendChild(effectElement);

													// // For Writing the
													// param , valueModel
													// and timing model

													List pms = le
													    .getParameterModels();
													for (int o = 0; o < pms
													    .size(); o++) {
														ParameterModel pm = (ParameterModel) pms
														    .get(o);
														paramElement = skinDetailsDocument
														    .createElement(ThemeTag.ELEMENT_PARAM);
														setAttributes(
														    paramElement, pm);
														effectElement
														    .appendChild(paramElement);
													}
												}
											}
										}
									}
								}

							}
						}
					}
				} else { // Element has no parts

					{
						// so go for image information

						// Create the image tag and set its attributes

						List imageList = ((SkinnableEntity) sbdElement)
						    .getAllThemeGraphics();
						if (imageList == null || imageList.size() <= 0) {
							// the ctrl comes here
							// when its selected or in fix list but doesnt hav
							// any image
							continue;
						}

						if (((SkinnableEntity) sbdElement).isEntityType()
						    .equals(ThemeTag.ELEMENT_BMPANIM)) {
							ThemeGraphicInterface tgi = (ThemeGraphicInterface) imageList
							    .get(0);
							if (tgi instanceof AnimatedThemeGraphic)
								imageList = tgi.getThemeGraphics();
							else
								continue;
						}
						// else write the image info

						for (int k = 0; k < imageList.size(); k++) {
							graphicElement = skinDetailsDocument
							    .createElement(ThemeTag.ELEMENT_GRAPHIC);
							ThemeGraphic tg = (ThemeGraphic) imageList.get(k);

							if (tg != null) {
								if (((SkinnableEntity) sbdElement)
								    .getCurrentProperty() != null
								    && ((SkinnableEntity) sbdElement)
								        .getCurrentProperty().equals(
								            ThemeTag.ATTR_STILL))
									tg.setAttribute(ThemeTag.ATTR_TYPE,
									    ThemeTag.ATTR_STILL);
								setAttributes(graphicElement, tg);
								elementElement.appendChild(graphicElement);

								imageElement = skinDetailsDocument
								    .createElement(ThemeTag.ELEMENT_IMAGE);
								graphicElement.appendChild(imageElement);
								List tmList = null;
								if (tg.isMorphedGraphic())
									tmList = ((MorphedGraphic) tg)
									    .getTimingModels();
								if (tmList != null) {
									int tmSize = tmList.size();
									if (tmSize > 0) {
										timingmodelsElement = skinDetailsDocument
										    .createElement(ThemeTag.ELEMENT_TIMINGMODELS);
										imageElement
										    .appendChild(timingmodelsElement);
									}
									for (int tm = 0; tm < tmSize; tm++) {
										timingmodelElement = skinDetailsDocument
										    .createElement(ThemeTag.ELEMENT_TIMINGMODEL);
										BaseTimingModelInterface btmi = (BaseTimingModelInterface) tmList
										    .get(tm);
										setAttributes(timingmodelElement, btmi);
										timingmodelsElement
										    .appendChild(timingmodelElement);
									}

									List vmList = ((MorphedGraphic) tg)
									    .getValueModels();
									int vmSize = vmList.size();
									if (vmSize > 0) {
										valuemodelsElement = skinDetailsDocument
										    .createElement(ThemeTag.ELEMENT_VALUEMODELS);
										imageElement
										    .appendChild(valuemodelsElement);
									}
									for (int vm = 0; vm < vmSize; vm++) {
										valuemodelElement = skinDetailsDocument
										    .createElement(ThemeTag.ELEMENT_VALUEMODEL);
										BaseValueModelInterface bvmi = (BaseValueModelInterface) vmList
										    .get(vm);
										setAttributes(valuemodelElement, bvmi);
										valuemodelsElement
										    .appendChild(valuemodelElement);
									}
								}
								int lSize = tg.getImageLayers().size();
								for (int l = 0; l < lSize; l++) {
									layerElement = skinDetailsDocument
									    .createElement(ThemeTag.ELEMENT_LAYER);
									ImageLayer il = (ImageLayer) tg
									    .getImageLayers().get(l);
									if (il != null) {
										Map attrmap = ((ImageLayer) il)
										    .getAttributes();
										if (attrmap
										    .containsKey(ThemeTag.FILE_NAME)) {
											String fileName = (String) attrmap
											    .get(ThemeTag.FILE_NAME);
											if (fileName
											    .lastIndexOf(File.separator) != -1) {
												String fileName1 = (new File(
												    fileName)).getName();
												S60Theme s60 = (S60Theme) ((SkinnableEntity) sbdElement)
												    .getRoot();
											
												try {
													FileUtils.copyFile(
													    fileName, s60
													        .getThemeDir()
													        + File.separator
													        + fileName1);
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}

										if (attrmap
										    .containsKey(ThemeTag.ATTR_SOFTMASK)) {
											String fileName = (String) attrmap
											    .get(ThemeTag.ATTR_SOFTMASK);
											if (fileName
											    .lastIndexOf(File.separator) != -1) {
												String fileName1 = (new File(
												    fileName)).getName();
												S60Theme s60 = (S60Theme) ((SkinnableEntity) sbdElement)
												    .getRoot();
											
												try {
													FileUtils.copyFile(
													    fileName, s60
													        .getThemeDir()
													        + File.separator
													        + fileName1);
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}

										if (attrmap
										    .containsKey(ThemeTag.ATTR_HARDMASK)) {
											String fileName = (String) attrmap
											    .get(ThemeTag.ATTR_HARDMASK);
											if (fileName
											    .lastIndexOf(File.separator) != -1) {
												String fileName1 = (new File(
												    fileName)).getName();
												S60Theme s60 = (S60Theme) ((SkinnableEntity) sbdElement)
												    .getRoot();
											
												try {
													FileUtils.copyFile(
													    fileName, s60
													        .getThemeDir()
													        + File.separator
													        + fileName1);
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}
										setAttributes(layerElement, il,
										    sbdElement);
										imageElement.appendChild(layerElement);

										int eSize = il.getLayerEffects().size();
										for (int m = 0; m < eSize; m++) {
											effectElement = skinDetailsDocument
											    .createElement(ThemeTag.ELEMENT_EFFECT);
											LayerEffect le = (LayerEffect) il
											    .getLayerEffects().get(m);

											setAttributes(effectElement, le);
											layerElement
											    .appendChild(effectElement);

											List pms = le.getParameterModels();
											for (int o = 0; o < pms.size(); o++) {
												ParameterModel pm = (ParameterModel) pms
												    .get(o);
												paramElement = skinDetailsDocument
												    .createElement(ThemeTag.ELEMENT_PARAM);
												setAttributes(paramElement, pm);
												effectElement
												    .appendChild(paramElement);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Method to set the attributes for elements in DOM tree
	 * 
	 * @param element Ins tance of type org.w3c.dom.Element
	 */
	public void setAttributes(org.w3c.dom.Element element) {
		String tagName = element.getTagName();

		if (tagName.equalsIgnoreCase(ThemeTag.ELEMENT_PHONE)) {

		} else if (tagName.equalsIgnoreCase(ThemeTag.ELEMENT_MODEL)) {

		} else if (tagName.equalsIgnoreCase(ThemeTag.ELEMENT_OTHERNAMES)) {

		} else if (tagName.equalsIgnoreCase(ThemeTag.ELEMENT_ALIAS)) {
		
		} else if (tagName.equalsIgnoreCase("languageoverride")) {

		} else if (tagName.equalsIgnoreCase("language")) {

			element.setAttribute(ThemeTag.ATTR_ID, "");
			element.setAttribute(ThemeTag.ATTR_UID, "");
		}
	}

	/**
	 * Method to set the attributes for elements in DOM tree
	 * 
	 * @param element Instance of type org.w3c.dom.Element
	 * @param Object Object that contains skin related information
	 */
	public void setAttributes(org.w3c.dom.Element element, Object obj) {
		setAttributes(element, obj, null);
	}

	/**
	 * Method to set the attributes for elements in DOM tree
	 * 
	 * @param element Instance of type org.w3c.dom.Element
	 * @param Object Object that contains skin related information
	 */
	public void setAttributes(org.w3c.dom.Element element, Object obj,
	    ThemeBasicData entity) {

		Map<Object, Object> map = null;
		if (obj instanceof ThemeBasicData) {

			HashMap<Object, Object> m = new HashMap<Object, Object>(
			    (((ThemeBasicData) obj).getAttribute()));
			map = (HashMap) m.clone();

			// remove the "name" attribute
			if (obj instanceof S60Theme) {

				map.remove(ThemeTag.ATTR_NAME);
				// this code added to make the type attribute available
				// when a new skin is created for a phone model.
				// v dont hav any default skin file parsing. and this attribute
				// is
				// required as per dtd
				map.put(ThemeTag.ATTR_VERSION, ThemeTag.ATTR_VERSION_NO);
				if (!map.containsKey(ThemeTag.ATTR_TYPE))
					map.put(ThemeTag.ATTR_TYPE, ThemeTag.ATTR_TYPE_NORMAL);

				Display display = ((S60Theme) obj).getDisplay();
				// for backward compatiability, width must be less than height
				map.put(ThemeTag.LAYOUT_WIDTH, Integer.toString(Math.min(
				    display.getWidth(), display.getHeight())));
				map.put(ThemeTag.LAYOUT_HEIGHT, Integer.toString(Math.max(
				    display.getWidth(), display.getHeight())));
				// for backward compatiability, must use either exact "portrait"
				// or "landscape"
				map
				    .put(
				        ThemeTag.ORIENTATION,
				        display.getOrientation() == Orientation.LANDSCAPE ? ThemeTag.ORIENTATION_LANDSCAPE
				            : ThemeTag.ORIENTATION_PORTRAIT);
				if (!Display.DEFAULT_TYPE.equalsIgnoreCase(display.getType())) {
					map.put(ThemeTag.ADVANCE_PUID, display.getType());
				} else {
					map.put(ThemeTag.ADVANCE_PUID, "");
				}

				
				map.put(ThemeTag.ADVANCE_PRIVATEKEY, ((S60Theme) obj)
				    .getPrivateKey());
				map.put(ThemeTag.ADVANCE_PUBLICKEY, ((S60Theme) obj)
				    .getPublicKey());

			}

			// remove the "show" attribute
			if (obj instanceof Element || obj instanceof Part) {
				// "text" attribute is not written
				map.remove(ThemeTag.ATTR_TEXT);

				if (obj instanceof Element) {
					Element e = (Element) obj;
					String isAnimateElement = e.isEntityType();
					String isShow = e.getAttributeValue(ThemeTag.ATTR_SHOW);
					if ((isAnimateElement.equals(ThemeTag.ATTR_BMPANIM))
					    && ((isShow != null) && (isShow
					        .equalsIgnoreCase("false")))) {
						elementList.add(e);
						System.out.println("Animate Element Found : " + e);
					}

					if (e.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
						if (e.getCurrentProperty() != null)
							map.put(ThemeTag.ATTR_TYPE, e.getCurrentProperty());
					}

					if (e.getAttributeValue(ThemeTag.ATTR_ANIM_MODE) != null) {
						map.put(ThemeTag.ATTR_ANIM_MODE, e
						    .getAttributeValue(ThemeTag.ATTR_ANIM_MODE));
					}
				} else {
					Part p = (Part) obj;
					if (p.getAttributeValue(ThemeTag.ATTR_ANIM_MODE) != null) {
						map.put(ThemeTag.ATTR_ANIM_MODE, p
						    .getAttributeValue(ThemeTag.ATTR_ANIM_MODE));
					}
				}

				map.remove(ThemeTag.ATTR_SHOW);
				map.remove(ThemeTag.CHECKNAME);
				map.remove(ThemeTag.ATTR_ENTITY_TYPE);
				map.remove(ThemeTag.ATTR_STATUS);
			}

			// remove the "layer" attribute
			if (obj instanceof Part) {
				map.remove(ThemeTag.LAYOUT_LAYER);
			}

			
			// add the attribute fix, if status is fix
			if (obj instanceof Element) {
				
				// ATTR_APPUID cant be removed. its needed for sis file creation
				// map.remove(ThemeTag.ATTR_APPUID);

				String status = ((Element) obj).getSkinnedStatus();
				if (status.equals(ThemeTag.SKN_ATTR_STATUS_FIX)) {
					map.put(ThemeTag.ATTR_FIX, ThemeTag.ATTR_FIX_TRUE);
				}

				
				// remove masterid attribute
				map.remove(ThemeTag.ATTR_MASTERID);
				
			
				// remove colour group attributes
				map.remove(ThemeTag.ATTR_COLOUR_GROUP_ID);
				map.remove(ThemeTag.ATTR_COLOUR_GROUP_IDX);
				map.remove(ThemeTag.ATTR_COLOUR_GROUP_MAJOR_ID);
				map.remove(ThemeTag.ATTR_COLOUR_GROUP_MINOR_ID);
			}

			if (obj instanceof Part) {
				String status = ((Part) obj).getSkinnedStatus();
				if (status.equals(ThemeTag.SKN_ATTR_STATUS_FIX)) {
					map.put(ThemeTag.ATTR_FIX, ThemeTag.ATTR_FIX_TRUE);
				}
			}

		} else if (obj instanceof ThemeGraphic) {
			map = ((ThemeGraphic) obj).getAttributes();
		} else if (obj instanceof ImageLayer) {
			map = ((ImageLayer) obj).getAttributes();
			if (map.containsKey(ThemeTag.FILE_NAME)) {
				String fileName = (String) map.get(ThemeTag.FILE_NAME);
				if (fileName.lastIndexOf(File.separator) != -1) {
					
					fileName = (new File(fileName)).getName();
					map.put(ThemeTag.FILE_NAME, fileName);
					ThemeGraphic tg = ((ImageLayer) obj).getThemeGraphic();
					// theme graphic of animated item needs to be synched with
					// the theme, the image might come from the model and now
					// need to be replaced with the actual theme.
					if (entity != null
					    && ((Theme) tg.getData().getRoot()).isModel()) {
						tg.setData(entity);
					}
				}
				if (fileName.endsWith(ThemeTag.SVG_FILE_EXTN)) {
					if (map.containsKey(ThemeTag.ATTR_SOFTMASK))
						map.remove(ThemeTag.ATTR_SOFTMASK);
					if (map.containsKey(ThemeTag.ATTR_HARDMASK))
						map.remove(ThemeTag.ATTR_HARDMASK);
				}

			}

			if (map.containsKey(ThemeTag.ATTR_SOFTMASK)) {
				String fileName = (String) map.get(ThemeTag.ATTR_SOFTMASK);
				if (fileName.lastIndexOf(File.separator) != -1) {
					
					fileName = (new File(fileName)).getName();
					map.put(ThemeTag.ATTR_SOFTMASK, fileName);
				}
			}

			if (map.containsKey(ThemeTag.ATTR_HARDMASK)) {
				String fileName = (String) map.get(ThemeTag.ATTR_HARDMASK);
				if (fileName.lastIndexOf(File.separator) != -1) {
					
					fileName = (new File(fileName)).getName();
					map.put(ThemeTag.ATTR_HARDMASK, fileName);
				}
			}
		} else if (obj instanceof LayerEffect) {
			map = new HashMap<Object, Object>();
			String effectName = ((LayerEffect) obj).getEffetName();
			map.put(ThemeTag.ATTR_NAME, effectName);
		} else if (obj instanceof ParameterModel) {
			map = ((ParameterModel) obj).getAttributes();
			if (((ParameterModel) obj).isAnimatedModel()) {
				if (map.containsKey(ThemeTag.ATTR_VALUE))
					map.remove(ThemeTag.ATTR_VALUE);
			} else {
				if (map.containsKey(ThemeTag.ELEMENT_VALUEMODEL_REF))
					map.remove(ThemeTag.ELEMENT_VALUEMODEL_REF);
			}
		} else if (obj instanceof BaseTimingModelInterface) {
			map = new HashMap<Object, Object>(((BaseTimingModelInterface) obj)
			    .getParameters());
		} else if (obj instanceof BaseValueModelInterface) {
			map = new HashMap<Object, Object>(((BaseValueModelInterface) obj)
			    .getParameters());
		}
		//			
		if (map != null) {

			Set set = map.keySet();
			String attrKey = "";
			String attrValue = "";

			Iterator it = set.iterator();

			while (it.hasNext()) {

				attrKey = (String) it.next();
				Object val = map.get(attrKey);
				
				if (!(val instanceof String)) {

					continue;

				}
				attrValue = (String) val;

				// this attribute of element/part should not be removed while
				// writing the skin file
				// becos, this is used in deciding the colourrgb and colouridx
				// attributes of
				// skinImage when a new SkinImage is created

				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_DEF_COLOUR_RGB)
				    || attrKey.equalsIgnoreCase(ThemeTag.ATTR_DEF_COLOUR_IDX)) {
					continue;
				}

				// the previewscreen info should not be removed while writing
				// the skin file
				// becos, this info is needed to point to the previewscreen(for
				// an element with the
				// same layout(default) coming in more than one
				// previewscreens...)
				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_PREVIEWSCREEN)) {
					continue;
				}

				// the reset info should not be removed
				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_RESET)) {
					continue;
				}

				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_APPUID)) {
					continue;
				}

				// the tile info taken from the design file should not be
				// removed
				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_TILE)) {

					
					// the tile attribute is from the design file
					if (!(obj instanceof ThemeGraphic)) {
						continue;
					}
					// if its from the skinImage then it should be set as the
					// attribute
				}

				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_COLOURDEPTH)) {

					if ((attrValue == null) || attrValue.equals("")) {
						continue;
					}
				}

				// the tile info taken from the design file should not be
				// removed
				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_STRETCH)) {

					// the tile attribute is from the design file
					if (!(obj instanceof ThemeGraphic)) {
						continue;
					}
					// if its from the skinImage then it should be set as the
					// attribute
				}
				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_MASK)) {
					continue;
				}

				if (attrKey.equalsIgnoreCase(ThemeTag.ELEMENT_SOUND)
				    || attrKey.equalsIgnoreCase(ThemeTag.ELEMENT_EMBED_FILE)) {
					continue;
				}

				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_TMP_IMAGE)) {
					continue;
				}

				if (attrKey.equalsIgnoreCase(ThemeTag.ATTR_TMP_MASK_IMAGE)) {
					continue;
				}
				if (attrKey != null && attrValue != null) {

					element.setAttribute(attrKey, attrValue);

				}
			}
		}
	}

	/**
	 * Method to write the DOM tree to an XML file
	 * 
	 * @throws IOException
	 */
	public void writeDOMToXMLFile() throws Exception {
		Map<String, String> props = new HashMap<String, String>(1);
		props.put(OutputKeys.DOCTYPE_SYSTEM, "http://abc.com/skindata.dtd");
		XmlUtil.write(skinDetailsDocument, skin.getThemeFile(), props);
	}

	/**
	 * Method to set the S60Skin object
	 * 
	 * @param skin Reference of S60Skin
	 */
	public void setSkinObject(S60Theme skin) {

		this.skin = skin;
	}

	/**
	 * Method to return reference of S60Skin object
	 * 
	 * @return S60Skin Object of type S60Skin
	 */
	public S60Theme getSkinObject() {

		return skin;
	}
}