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
package com.nokia.tools.platform.theme.preview;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeCache;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.ThemeappJaiUtil;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;

/*
 * Each Theme instance will have an instance of this class. This class will hold
 * only the preview information for that genTheme.
 * 
 * 
 */
public class PreviewController {
	private static final String BACKGROUND_LAYER_INDICATOR = "BACK^^^:";

	
	private Theme skinDetails;
	private ThreadLocal local = new ThreadLocal();


	public PreviewController(Theme skin) {
		if (skin == null) {
			throw new NullPointerException(
					"Supplied skin details object cannot be null");
		}
		this.skinDetails = skin;
	}

	/**
	 * Get the screen skinnableEntity from the screeensList and if (resolution ==
	 * null) Update the genOrientation such that 1. if requested genOrientation
	 * is both and currImage genOrientation is both, return image in portrait
	 * genOrientation (default) 2. if requested genOrientation is both and
	 * currImage genOrientation is not both, return currimage genOrientation 3.
	 * if requested genOrientation is both and currImage genOrientation is not
	 * both, return currimage genOrientation create the thread and call
	 * generatePreview screen on that as WORK_TYPE = generate single preview
	 * screen skin, ThemeBasicData sbd - screen image's skinbasicdata object
	 * Display genResolution, - genResolution for which the preview screen has
	 * to be genrated genElementImageParam, - to get edited/ draft/ actual image
	 * HashMap previewElementsHash - to save the newly generated preview
	 * elements genForceGenerate result - contains the genResultant image ONly
	 * backgrounds now generated
	 */
	public RenderedImage generateBackgroundLayer(PreviewImage screen,
			SkinnableEntity entity, int elementImageParam)
			throws PreviewException {
		Layout layoutInfo = null;
		if (entity != null) {
			try {
				layoutInfo = entity.getLayoutInfo(screen.getDisplay());
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
		}

		return generateBackgroundLayer(screen, entity, layoutInfo);
	}

	private RenderedImage getFromImageCache(ThemeCache cache, String cacheKey,
			Display display, String skinElementName, Layout layout) {

		List cachedElems = (List) cache.getElement(display, skinElementName,
				cacheKey);
		if (cachedElems != null) {
			for (Object cached : cachedElems) {
				RenderedImage obj = (RenderedImage) cached;
				if (cached != null)
					if ((layout == null)
							|| (obj.getWidth() == layout.W() && obj.getHeight() == layout
									.H())) {
						return (RenderedImage) obj;
					}
			}
		}
		return null;
	}

	synchronized private void putToImageCache(ThemeCache cache,
			RenderedImage image2put, String cacheKey, Display display,
			String skinElementName, Layout layout) {

		if (getFromImageCache(cache, cacheKey, display, skinElementName, layout) != null)
			return;
		// some elements are with the same id but for different layout.
		// per instance qsn_bg_area_main is on several screen and with
		// different layout
		// to enable caching of multiple element
		List cachedElems = (List) cache.getElement(display, skinElementName,
				cacheKey);
		if (null == cachedElems) {
			cachedElems = new ArrayList<Object>(1);
			cache.putElement(display, skinElementName, cacheKey, cachedElems);
		}
		cachedElems.add(image2put);
	}

	protected PreviewImage getProperScreen(PreviewImage screen,
			SkinnableEntity entity) {
		if (screen == null) {
			screen = skinDetails.getThemePreview().getPreviewImageForElem(
					entity, true);
		}
		// not in any previews, use the default screen declared in the design
		// for bg generation
		if (screen == null) {
			String screenName = entity
					.getAttributeValue(ThemeTag.ATTR_PREVIEWSCREEN);
			screen = (PreviewImage) skinDetails.getThemePreview()
					.getPreviewImageByName(screenName,
							Collections.singleton(skinDetails.getDisplay()));
		}
		if (screen == null) {
			PlatformCorePlugin.error("No default screen found for: "
					+ entity.getIdentifier());
		}
		return screen;
	}

	/**
	 * This method will generate the preview image by aggregating the elements
	 * one after the other till it comes the skinElementName in preview.xml for
	 * the screenName specified if skinElementName is not present in the
	 * screen's elements (in preview.xml) the whoel preview image is generated
	 * adn returned
	 */
	public RenderedImage generateBackgroundLayer(PreviewImage screen,
			SkinnableEntity entity, Layout layout) throws PreviewException {
		screen = getProperScreen(screen, entity);
		PreviewElement element = screen.getPreviewElement(screen.getDisplay(),
				entity.getIdentifier());
		long contextCode = 0;
		if (element != null) {
			contextCode = element.getBackgroundDependancy().hashCode();
		}

		RenderedImage result = getFromImageCache(skinDetails
				.getBackgroundLayerCache(), BACKGROUND_LAYER_INDICATOR
				+ ThemeConstants.ELEMENT_IMAGE_PARAM_PREVIEW_IMAGE
				+ contextCode, screen.getDisplay(), entity.getId(), layout);
		if (null != result)
			return result;

		List<RenderablePreviewImage> resultant = new ArrayList<RenderablePreviewImage>();
		PreviewGenerator backgroundScreen = new PreviewGenerator(screen,
				entity, resultant);

		if (local.get() == null) {
			local.set(new ArrayList());
		}
		List list = (List) local.get();
		if (!list.contains(entity)) {
			try {
				backgroundScreen.run();
			} finally {
				list.remove(entity);
				if (list.isEmpty()) {
					local.remove();
				}
			}
		}

		if (resultant.isEmpty()) {
			return null;
		}

		RenderablePreviewImage tempImage = (RenderablePreviewImage) resultant
				.get(0);

		if (layout == null) {
			if (entity != null) {
				try {
					layout = entity.getLayoutInfo(screen.getDisplay());
				} catch (Exception ex) {
					PlatformCorePlugin.error(ex);
				}
			}
		}

		try {
			if (layout != null && tempImage != null) {
				result = CoreImage.create().init(tempImage.getImage()).crop(
						layout.L(), layout.T(), layout.W(), layout.H())
						.getAwt();
			}
		} catch (Exception ex) {
			PlatformCorePlugin.error(ex);
		}
		putToImageCache(this.skinDetails.getBackgroundLayerCache(), result,
				BACKGROUND_LAYER_INDICATOR
						+ ThemeConstants.ELEMENT_IMAGE_PARAM_PREVIEW_IMAGE
						+ contextCode, screen.getDisplay(), entity.getId(),
				layout);
		return result;
	}

	public RenderedImage getBackgroundLayerImage(PreviewImage screen,
			String skinElementName, int elementImageParam)
			throws PreviewException {
		SkinnableEntity entity = skinDetails
				.getSkinnableEntity(skinElementName);

		screen = getProperScreen(screen, entity);

		return generateBackgroundLayer(screen, entity, elementImageParam);
	}

	public RenderedImage getBackgroundLayerImage(PreviewImage screen,
			String skinElementName, Layout layout) throws PreviewException {
		SkinnableEntity entity = skinDetails
				.getSkinnableEntity(skinElementName);

		screen = getProperScreen(screen, entity);

		return generateBackgroundLayer(screen, entity, layout);
	}

	class PreviewGenerator {

		private PreviewImage genTbd;

		private List<RenderablePreviewImage> genResultant;

		private SkinnableEntity genEntity;

		/*
		 * This constructor is to be used for generatebackground,
		 * generateForeground and generateEditingAreaScreen The input parameter
		 * skinElementToSkip will be interpreted differently based on the
		 * WORK_TYPE For WORK_TYPE= generateBackground, the preview screen
		 * creation will stop with this element For WORK_TYPE=
		 * generateForeground, the preview screen creation will start with this
		 * element For WORK_TYPE= generateEditingAreaScreen, the preview screen
		 * created will NOT include this element
		 */
		PreviewGenerator(PreviewImage screen, SkinnableEntity entity,
				List<RenderablePreviewImage> resultant) {
			this.genTbd = screen;
			this.genResultant = resultant;
			this.genEntity = entity;
		}

		public void run() {
			try {
				generateBackground();
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
		}

		private RenderedImage makeScreen(PreviewImage screen, int[] noOfElements)
				throws PreviewException {
			Display display = screen.getDisplay();
			RenderedImage parentImage = CoreImage.getBlank3Image(display
					.getWidth(), display.getHeight(), Color.white);

			Layout splElementLayout = null;
			Rectangle splElementRectangle = null;
			if (genEntity != null) {
				PreviewElement pElement = (PreviewElement) screen
						.getChildForPreview(genEntity.getIdentifier());
				if (pElement != null) {
					try {
						splElementLayout = genEntity.getLayoutInfoForPreview(
								display, pElement.getComponentInfo(), screen);
					} catch (ThemeException e) {
						PlatformCorePlugin.error(e);
					}
				} else {
					try {
						splElementLayout = genEntity.getLayoutInfo(display);
					} catch (ThemeException e) {
						PlatformCorePlugin.error(e);
					}
				}
			}
			splElementRectangle = splElementLayout.getBounds();

			List<ThemeBasicData> scrChildrenList = screen.getChildren(true);

			noOfElements[0] += scrChildrenList.size();
			for (ThemeBasicData tbd : scrChildrenList) {

				PreviewElement preElem = (PreviewElement) tbd;
				// needs to filter out the elements that are not supported by
				// the current display
				if (!preElem.supportsDisplay(display)) {
					continue;
				}

				
				/*
				 * There is no need to paint all elements that are under current
				 * painted background except multilayer elements. icons and
				 * other contents from preview system should not effect to
				 * background type of elements
				 */
				String id = preElem.getID();

				// change background checking
				if (StringUtils.isEmpty(id)
						|| (!id.startsWith("qsn_") && !id
								.equals("qgn_area_main_mup2")))
					continue;

				// check from the preview element instead of using getIdentifier

				int elementType = preElem.getPreviewElementType();

				if (elementType == ThemeConstants.ELEMENT_TYPE_SOUND)
					continue;

				ComponentInfo comp = preElem.getComponentInfo();

				String elemInfo = preElem.getID();

				// for the background layer image
				// before the element is found, render all images
				// except those whose layout intersects with the special
				// elements layout

				if (elemInfo != null && elemInfo.equals(genEntity.getId())) {
					return parentImage;
				}

				Layout currElementLayout = null;

				Rectangle currElementRectangle = null;
				SkinnableEntity currElemEntity = skinDetails
						.getSkinnableEntity(elemInfo);
				try {
					if (currElemEntity != null) {
						currElementLayout = currElemEntity
								.getLayoutInfoForPreview(screen.getDisplay(),
										comp, screen);
					} else if (currElemEntity == null) {
						Display entityDisplay = display;
						if (currElemEntity != null) {
							entityDisplay = currElemEntity.getDisplay();
						}

						currElementLayout = skinDetails
								.getLayoutForPreviewNonFrame(entityDisplay,
										comp);
					}

				} catch (ThemeException e) {
					PlatformCorePlugin.error(e);
				}

				if (currElementLayout != null) {
					currElementRectangle = currElementLayout.getBounds();
				}

				if (currElementRectangle != null && splElementRectangle != null) {
					Rectangle currElementRect = currElementLayout.getBounds();
					if (!currElementRect.intersects(splElementRectangle)) {
						continue;
					}
				}

				Color imageColour = null;

				// Check if any colour group need to be applied on the processed
				// image
				try {
					// Check the colorGrp object. If it is null then colourgroup
					// is
					// not edited in editing area
					// If not null then colourgroup is being editied from the
					// editing area
					String cGrp = preElem
							.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COLORID);
					if (cGrp != null) {
						imageColour = skinDetails.getRGBColour(cGrp, 1);
					}
				} catch (Exception e) {
					PlatformCorePlugin.error(e);
				}
				// This case is valid for imagefile tag. The imagefile should be
				// treated as a overlay image with layout as 0,0,176,208

				if (elementType == ThemeConstants.ELEMENT_TYPE_IMAGEFILE) {
					try {
						parentImage = processImageFile(parentImage, preElem,
								imageColour, splElementRectangle);
					} catch (PreviewException e) {
						PlatformCorePlugin.error(e);
					}

					continue;
				}

				/*
				 * Following types should be taken care of 1. Image - following
				 * getProcessedEntity will take care of it 2. Text - have to
				 * draw myself in the preview subsystem - layout for text
				 * elements - should be got in the preview subsystem - how to
				 * get the fonts and colors information for the text to be drawn
				 * 3. ImageFile - have to get the layout and draw on the current
				 * image - have to get layout info inside the preview subsystem
				 * 4. Sound - will the imaging system API take care of this. -
				 * what kind of common object should be returned by this imaging
				 * API method - how should preview take care of sound? 5. Color
				 * Elements - how are the skinned results to be shown by
				 * preview? - is themegraphic going to have this type 6.
				 * Animation - where will preview related screens be available? -
				 * how to take care of animation in the preview subsystem
				 */

				/*
				 
				 */

				try {
					CoreImage mainImage = null;
					/* GRAPHIC ELEMENT */
					if (elementType == ThemeConstants.ELEMENT_TYPE_GRAPHIC) {

						SkinnableEntity entity = skinDetails
								.getSkinnableEntity(elemInfo);
						if (entity == null) {
							continue;
						}
						Layout layoutInfo = entity.getLayoutInfoForPreview(
								screen.getDisplay(), comp, screen);

						// get the element type from the ThemeGraphic obtained
						// thru
						// the Skinnable Entity
						mainImage = CoreImage
								.create()
								.init(
										ThemeappJaiUtil
												.getElementImage(
														entity,
														display,
														screen,
														comp,
														ThemeConstants.ELEMENT_IMAGE_PARAM_PREVIEW_IMAGE,
														!preElem.hasSplMask(),
														layoutInfo, preElem));

						if (preElem.getRotate() != null
								&& preElem
										.getRotate()
										.equalsIgnoreCase(
												PreviewTagConstants.ATTR_ROTATE_VALUE_180)) {
							mainImage.rotate(180);
						}

						if (mainImage.getAwt() == null) {
							PlatformCorePlugin
									.warn("element image is null for genEntity : "
											+ entity.getIdentifier());
							continue;
						}
						if (imageColour != null) {
							mainImage.overlay(imageColour);
						}

						// render the image over the parent image after adding
						// special affects

						String is_mask = preElem
								.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_IS_MASK);

						if (preElem.hasSplMask()) {
							processSplMask(mainImage, preElem, comp);
						}

						if (is_mask != null && is_mask.trim().length() > 0) {
							CoreImage bwImage = mainImage.copy()
									.extractBlackAndWhiteMask();
							mainImage.applyMask(bwImage, false);
						}

						String opacity = preElem
								.getAttributeValue(PreviewTagConstants.ATTR_IMAGEFILE_OPACITY);

						if (!StringUtils.isEmpty(opacity)) {
							try {
								int opacityInt = Integer.parseInt(opacity);
								if (opacityInt == 255) {
									mainImage.opacity(opacityInt);

								} else {
									mainImage.fade(opacityInt);
								}
							} catch (NumberFormatException exNum) {
								PlatformCorePlugin.warn("Wrong opacity value "
										+ opacity + " specified ");
							}
						}

						boolean isSrcWhileAggregate = ThemeappJaiUtil
								.hasBackgroundLayer(
										entity,
										ThemeConstants.ELEMENT_IMAGE_PARAM_PREVIEW_IMAGE);

						parentImage = aggregateImages(parentImage, mainImage
								.getAwt(), layoutInfo, display.getWidth(),
								display.getHeight(), isSrcWhileAggregate);
					}
				} catch (ThemeException exTheme) {
					throw new PreviewException(exTheme);
				}
			}

			return parentImage;
		}

		// Support For Landscape Previews - end

		private RenderedImage processImageFile(RenderedImage parentImage,
				PreviewElement preElem, Color imageColour,
				Rectangle splElementRectangle) throws PreviewException {
			String filename = preElem
					.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_IMAGE);

			String opacity = preElem
					.getAttributeValue(PreviewTagConstants.ATTR_IMAGEFILE_OPACITY);
			String compName = preElem
					.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COMPNAME);

			CoreImage img = null;

			Layout layoutInfo = null;
			String dirname = null;
			RenderedImage result = null;
			if (!StringUtils.isEmpty(opacity)) {
				return CoreImage.create().init(parentImage).fade(
						Integer.parseInt(opacity)).getAwt();
			}

			Display display = genTbd.getDisplay();
			if (filename != null && filename.trim().length() > 0) {
				String imgFilename = skinDetails.getFileName(filename, null);
				if (imgFilename == null) {
					throw new PreviewException("Preview Generation failed, "
							+ filename + " not found");
				}
				if (StringUtils.isEmpty(compName)) {
					layoutInfo = new Layout();
					layoutInfo.setLayout(0, 0, display.getWidth(), display
							.getHeight(), display.getWidth(), display
							.getHeight());

					try {
						img = CoreImage.create().load(new File(imgFilename),
								skinDetails.getDisplay().getWidth(),
								skinDetails.getDisplay().getHeight()).crop(0,
								0, display.getWidth(), display.getHeight());
					} catch (Exception ex) {
						throw new PreviewException(ex);
					}

				} else {
					ComponentInfo comp = preElem.getComponentInfo();
					try {
						// Support For Landscape Previews - start
						// layoutInfo =
						// SkinnableEntity.getLayoutForPreviewNonFrame(
						// theme.getCurrentPhone(), new Display(width, height),
						// orientation, compName, Integer.parseInt(varietyId),
						// locId);
						layoutInfo = skinDetails.getLayoutForPreviewNonFrame(
								display, comp);
						// Support For Landscape Previews - end
					} catch (Exception e) {
						PlatformCorePlugin.error(e);
						return null;
					}
					if (layoutInfo == null) {
						return parentImage;
					}

					try {
						img = CoreImage.create().load(new File(imgFilename),
								layoutInfo.W(), layoutInfo.H(),
								CoreImage.STRETCH);
					} catch (Exception exIO) {
						PlatformCorePlugin.warn("Error loading image : "
								+ imgFilename + " with W: " + layoutInfo.W()
								+ " height: " + layoutInfo.H());
						throw new PreviewException(exIO);
					}

				}
				if (imageColour != null && img != null) {
					img = img.overlay(imageColour);
				}

				// check if there is a mask
				// if so apply the mask first and then aggregate images
				String maskFilename = preElem
						.getAttributeValue(PreviewTagConstants.ATTR_IMAGEFILE_MASK);
				RenderedImage maskedImage = null;

				if (maskFilename != null && maskFilename.trim().length() > 0) {

					String maskName = null;

					CoreImage maskImage = null;

					try {
						maskName = FileUtils.makeAbsolutePath(dirname,
								maskFilename);
						maskImage = CoreImage.create().load(new File(maskName),
								display.getWidth(), display.getHeight());

					} catch (Exception exIO) {
						throw new PreviewException(exIO);
					}

					maskedImage = img.copy().applyMask(maskImage, true)
							.getAwt();
				}

				Rectangle currElementRectangle = layoutInfo.getBounds();

				if (splElementRectangle != null
						&& splElementRectangle.intersects(currElementRectangle)) {
					return parentImage;
				}

				if (maskedImage != null) {
					result = aggregateImages(parentImage, maskedImage,
							layoutInfo, layoutInfo.L(), layoutInfo.T(), false);
				} else {
					result = aggregateImages(parentImage, img.getAwt(),
							layoutInfo, layoutInfo.L(), layoutInfo.T(), false);
				}
			}

			return result;
		}

		private RenderedImage aggregateImages(RenderedImage parent,
				RenderedImage image, Layout layInfo, int width, int height,
				boolean doSrc) {

			if (parent == null)
				parent = CoreImage.getBlank3Image(width, height, Color.WHITE);

			if (layInfo == null || image == null) {
				return parent;
			}

			BufferedImage buffered = CoreImage.getBufferedImage(parent);
			Graphics2D tg2d = (Graphics2D) buffered.getGraphics();

			if (doSrc) {
				tg2d.setComposite(AlphaComposite.Src);
			} else {
				tg2d.setComposite(AlphaComposite.SrcOver);
			}
			AffineTransform atx = new AffineTransform();
			atx.setToTranslation((double) layInfo.L(), (double) layInfo.T());
			tg2d.drawRenderedImage(image, atx);
			tg2d.dispose();
			return parent;
		}

		private void processSplMask(CoreImage mainImage,
				PreviewElement preElem, ComponentInfo component)
				throws ThemeException {

			// Check if the preview element uses a special mask
			// For processing special mask details

			String splMaskType = preElem
					.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_SPL_MASK_TYPE);
			String splMaskEntityId = preElem
					.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_SPL_MASK_ID);

			RenderedImage splMaskImage = null;

			if (splMaskEntityId != null) {
				SkinnableEntity splMaskEntity = skinDetails
						.getSkinnableEntity(splMaskEntityId);
				if (splMaskEntity != null) {
					splMaskImage = ThemeappJaiUtil.getElementImage(
							splMaskEntity, preElem.getDisplay(),
							(PreviewImage) preElem.getParent(), component,
							ThemeConstants.ELEMENT_IMAGE_PARAM_PREVIEW_IMAGE);
				}
			}

			boolean isMaskInverted = false;
			if ((splMaskType != null)
					&& (splMaskType
							.equalsIgnoreCase(ThemeTag.ATTR_VALUE_SPLMASKINVERTEDGREY))) {
				isMaskInverted = true;
			}

			mainImage.applyMask(CoreImage.create().init(splMaskImage),
					isMaskInverted);
		}

		public void generateBackground() throws PreviewException {
			int noOfElements[] = new int[1];
			RenderedImage buffScreenImage = makeScreen(genTbd, noOfElements);

			RenderablePreviewImage currImage = new RenderablePreviewImage(
					genTbd.getName(), buffScreenImage, genTbd.getDisplay());

			currImage.setNoOfPreviewElements(noOfElements[0]);

			genResultant.add(currImage);
		}
	}
}