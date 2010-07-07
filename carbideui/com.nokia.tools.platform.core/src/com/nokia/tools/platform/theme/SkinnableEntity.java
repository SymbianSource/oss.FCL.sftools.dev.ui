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
package com.nokia.tools.platform.theme;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.layout.LayoutException;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.platform.theme.preview.PreviewTagConstants;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;

/**
 * Base class holding the information of Skinnnable Entities
 * 
 */

public abstract class SkinnableEntity extends ThemeBasicData {
	
	private final static String LINES_ID = "qgn_graf_line";

	// Variables to store the skin images
	protected List<Object> themeGraphics;
	protected int actualImagePosition = -1;

	/**
	 * Constructor
	 */
	SkinnableEntity() {
	}

	public boolean isLine() {
		return getIdentifier().startsWith(LINES_ID);
	}

	public AnimatedThemeGraphic getAnimatedThemeGraphic() {
		if (themeGraphics != null && !themeGraphics.isEmpty()) {
			if (themeGraphics.get(0) instanceof AnimatedThemeGraphic) {
				return (AnimatedThemeGraphic) themeGraphics.get(0);
			}
		}
		return null;
	}

	public Object getAnimatedObject() {
		if (themeGraphics != null && !themeGraphics.isEmpty()) {
			return themeGraphics.get(0);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#getSkinnableEntity()
	 */
	@Override
	public SkinnableEntity getSkinnableEntity() {
		return this;
	}

	public boolean isTile() {
		return new Boolean((String) getAttribute().get(ThemeTag.ATTR_TILE));
	}

	public ThemeGraphic getThemeGraphic(String seqNo) {
		Object animObj = getAnimatedObject();
		if (animObj != null) {
			if (animObj instanceof AnimatedThemeGraphic) {
				return ((AnimatedThemeGraphic) animObj).getThemeGraphic(seqNo);
			}
			return ((AnimatedThemeGraphicCache) animObj).getThemeGraphic(seqNo);
		}
		return null;
	}

	private String getNewFileName(List list, int size) {
		String newFileName = "atg_child" + size + ThemeTag.FILE_TYPE_BMP;
		boolean exists = false;

		for (int i = 0; i < list.size(); i++) {
			ThemeGraphic tg = (ThemeGraphic) list.get(i);
			for (int j = 0; j < tg.getImageLayers().size(); j++) {
				ImageLayer il = (ImageLayer) tg.getImageLayers().get(j);
				String fileName = il.getAttribute(ThemeTag.FILE_NAME);
				if (fileName.equals(newFileName)) {
					exists = true;
					break;
				}
			}
		}
		if (!exists) {
			return newFileName;
		}

		return getNewFileName(list, size + 1);

	}

	/**
	 * To get unique id for a ThemeGraphic in a Animated skinnableentity
	 * 
	 * @return string uniqueid
	 */
	private String getUniqueID(List tgList) {
		int id = 0;
		if (tgList != null) {
			for (int i = 0; i < tgList.size(); i++) {
				String uid = ((ThemeGraphic) tgList.get(i))
						.getAttribute(ThemeTag.UNIQUE_ID);
				int uidInt = Integer.parseInt(uid);
				if (uidInt > id)
					id = uidInt;
			}
		}
		id = id + 1;
		return Integer.toString(id);
	}

	public ThemeGraphic prepareThemeGraphic(String fileName, String time,
			boolean add) {

		ThemeGraphic tg = ((Theme) getRoot()).createGraphic(this);

		ImageLayer il = new ImageLayer(tg);
		il.setAttribute(ThemeTag.ATTR_ENTITY_Y, ThemeTag.ATTR_ENTITY_Y_DEFAULT);
		il.setAttribute(ThemeTag.ATTR_IMAGE_X, ThemeTag.ATTR_IMAGE_X_DEFAULT);
		il.setAttribute(ThemeTag.ATTR_STRETCH, ThemeTag.ATTR_STRETCH_DEFAULT);
		il.setAttribute(ThemeTag.ATTR_ANGLE, ThemeTag.ATTR_ANGLE_DEFAULT);
		il.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_DRAFT);
		il.setAttribute(ThemeTag.ATTR_IMAGE_Y, ThemeTag.ATTR_IMAGE_Y_DEFAULT);
		il.setAttribute(ThemeTag.ATTR_ENTITY_X, ThemeTag.ATTR_ENTITY_X_DEFAULT);
		il.setAttribute(ThemeTag.ATTR_COLOURDEPTH,
				ThemeTag.ATTR_VALUE_COLOUR_DEPTH_DEFAULT);
		il.setAttribute(ThemeTag.ATTR_NAME, "layer0");

		if (add) {
			Object animObj = getAnimatedObject();
			if (animObj != null) {
				String newFileName = getNewFileName(
						((ThemeGraphicInterface) animObj).getThemeGraphics(),
						((ThemeGraphicInterface) animObj).getThemeGraphics()
								.size());
				copyFile(fileName, newFileName);
				tg.setAttribute(ThemeTag.ATTR_ANIMATE_TIME, time);
				tg.setAttribute(ThemeTag.ATTR_TYPE, ThemeTag.ATTR_BMPANIM);
				tg.setAttribute(ThemeTag.UNIQUE_ID,
						getUniqueID(((ThemeGraphicInterface) animObj)
								.getThemeGraphics()));
				il.setAttribute(ThemeTag.FILE_NAME, newFileName);
				tg.setImageLayers(il);
				((ThemeGraphicInterface) animObj).addThemeGraphic(tg);
			}
		} else {
			tg.setImageLayers(il);
		}

		return tg;
	}

	public void addThemeGraphicToAnimationList(ThemeGraphic tg) {
		Object animObj = getAnimatedObject();
		if (animObj != null) {
			if (animObj instanceof AnimatedThemeGraphic)
				((AnimatedThemeGraphic) animObj).addThemeGraphic(tg);
			else
				((AnimatedThemeGraphicCache) animObj).addThemeGraphic(tg);
		}
	}

	public void addThemeGraphic(ThemeGraphic tg) throws ThemeException {

	}

	public List getThemeGraphics() throws ThemeException {
		return themeGraphics;
	}

	public List getThemeGraphicsFromAnimationList() {
		Object animObj = getAnimatedObject();

		if (animObj != null) {
			if (animObj instanceof AnimatedThemeGraphic)
				return ((AnimatedThemeGraphic) animObj).getThemeGraphics();
			else
				return ((AnimatedThemeGraphicCache) animObj).getThemeGraphics();
		} else {
			SkinnableEntity se = getModel().getSkinnableEntity(getIdentifier());
			AnimatedThemeGraphic atg = se.getAnimatedThemeGraphic();
			if (atg != null) {
				atg = (AnimatedThemeGraphic) atg.clone();
				List<ThemeGraphic> list = atg.getThemeGraphics();
				AnimatedThemeGraphicCache atgc = new AnimatedThemeGraphicCache(
						this);
				setAnimateGraphic(atgc);
				atgc.addThemeGraphics(list);
				return list;
			}
		}
		return null;
	}

	public void clearThemeGraphic() {
		clearThemeGraphic(false);
	}

	public void clearThemeGraphic(boolean status) {
		if (link != this) {
			((SkinnableEntity) link).clearThemeGraphic(status);
			
			((SkinnableEntity) link).themeGraphics = null;
			((SkinnableEntity) link).actualImagePosition = -1;
			return;
		}
		if (themeGraphics != null) {
			
			if (isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
				Object animObj = getAnimatedObject();
				if (animObj != null) {
					if (animObj instanceof AnimatedThemeGraphic)
						((AnimatedThemeGraphic) animObj)
								.clearAnimatedThemeGraphics();
					else
						((AnimatedThemeGraphicCache) animObj)
								.clearAnimatedThemeGraphics();
				}
			} else {
				
				for (int i = 0; i < themeGraphics.size(); i++) {
					ThemeGraphic tg = (ThemeGraphic) this.themeGraphics.get(i);
					tg.clearThemeGraphic();
				}
			}
			if (!status)
				this.setSkinned(false);
			
			this.themeGraphics = null;
			
			this.actualImagePosition = -1;
		}
	}

	private ThemeGraphic getThemeGraphicFromAnimatedThemeGraphic()
			throws ThemeException {
		Object animObj = getAnimatedObject();
		if (animObj != null) {
			return ((ThemeGraphicInterface) animObj).getThemeGraphic();
		}
		SkinnableEntity se = getModel().getSkinnableEntity(getIdentifier());
		AnimatedThemeGraphic atg1 = se.getAnimatedThemeGraphic();
		if (atg1 != null) {
			atg1 = (AnimatedThemeGraphic) atg1.clone();
			ThemeGraphic tg = atg1.getThemeGraphic();
			return tg;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#getThemeGraphic()
	 */
	public ThemeGraphic getThemeGraphic() throws ThemeException {
		return getThemeGraphic((Display) null);
	}

	public ThemeGraphic getThemeGraphic(Display display) throws ThemeException {
		return getThemeGraphic(display, null, null);
	}

	/**
	 * Fetches the detail of the ThemeGraphic associated with the object Note:
	 * The function returns the filename of the actual ThemeGraphic if it is
	 * present. Otherwise it returns the filename of the first draft
	 * ThemeGraphic.
	 * 
	 * @return The ThemeGraphic object containing the layers details
	 */
	public ThemeGraphic getThemeGraphic(Display display, PreviewImage screen,
			ComponentInfo component) throws ThemeException {

		if (this.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
			return getThemeGraphicFromAnimatedThemeGraphic();
		}

		if (link != this) {
			return (((SkinnableEntity) link).getThemeGraphic());
		}

		// Check if this id or name is shown on the editing area
		// String identity = ((SkinnableEntity)link).getIdentifier();

		ThemeGraphic im = null;
		
		synchronized (this) {
			im = getActualThemeGraphic();

			// now the preview system is working on getPreviewmage ...
			// getThemeGraphic is used primarily be skineditingarea ....
			// they can see the last editing ThemeGraphic if there is no actual
			// ThemeGraphic
			if (im == null) {
				List imList = ((SkinnableEntity) link).getDraftThemeGraphics(
						display, screen, component);

				if (imList != null && imList.size() > 0)
					im = (ThemeGraphic) imList.get(imList.size() - 1);
			}

		}

		// if the ThemeGraphic is not available and the entity is not having
		// parts,
		// create a new SkinObj with the info available from the default skin
		// directory for the phone model and return it

		if (im == null) {
			if ((!link.hasChildNodes())
					|| ((link.getCurrentProperty() != null) && (link
							.getCurrentProperty()
							.equals(ThemeTag.ATTR_SINGLE_BITMAP)))) {
				im = createThemeGraphic(screen);
			}
		}

		return im;
	}

	/**
	 * Picks the ThemeGraphic that need to be sent to the phone
	 * 
	 * @return The object containing the ThemeGraphic details
	 */
	public ThemeGraphic getPhoneThemeGraphic() throws ThemeException {
		return getActualThemeGraphic();
	}

	public ThemeGraphic getPreviewThemeGraphic() throws ThemeException {
		return getPreviewThemeGraphic(null);
	}

	// Support For Landscape Previews - start
	public ThemeGraphic getPreviewThemeGraphic(PreviewImage screen)
			throws ThemeException {
		return getPreviewThemeGraphic(screen, null);
	}

	// Support For Landscape Previews - end
	/**
	 * Fetches the detail of the previewThemeGraphic associated with the object
	 * Note: The function returns the ThemeGraphic of the actual ThemeGraphic if
	 * it is present. Otherwise it returns the filename of the first draft
	 * ThemeGraphic.
	 * 
	 * @return The object containing the ThemeGraphic details
	 */
	// Support For Landscape Previews - start
	// public ThemeGraphic getPreviewThemeGraphic(PreviewState pState) throws
	// ThemeException {
	public ThemeGraphic getPreviewThemeGraphic(PreviewImage screen,
			Layout layout) throws ThemeException {
		// Support For Landscape Previews - end

		if (this.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
			Object animObj = getAnimatedObject();
			if (animObj != null) {
				return ((ThemeGraphicInterface) animObj)
						.getPreviewThemeGraphic();
			} else {
				SkinnableEntity se = getModel().getSkinnableEntity(
						this.getIdentifier());
				AnimatedThemeGraphic atg1 = se.getAnimatedThemeGraphic();
				if (atg1 != null) {
					atg1 = (AnimatedThemeGraphic) atg1.clone();
					return atg1.getPreviewThemeGraphic();
				}
			}
		}

		if (link != this) {
			return ((SkinnableEntity) link).getPreviewThemeGraphic(screen,
					layout);
		}

		/*
		 * 1.Check if the ThemeGraphic has actual ThemeGraphic 2.If it doesnot
		 * have actual ThemeGraphic then check if the reset attribute 3.If the
		 * reset state is set to "none" then return null - this means that this
		 * entity can be absent on the screen (not replaced by default) 4.If
		 * reset is not "none" then pick up the default stuff
		 */

		ThemeGraphic tg = getActualThemeGraphic();
		if (tg != null) {
			return tg;
		}

		// Code reaches here only if there is no actual ThemeGraphic
		String resetStatus = link.getAttributeValue(ThemeTag.ATTR_RESET);

		// If the reset attribute is not 'none' then get the default data
		if ((resetStatus == null)
				|| !(resetStatus
						.equalsIgnoreCase(ThemeTag.ATTR_RESET_DEFAULT_VALUE))) {
			
			// if the ThemeGraphic is not available and the entity is not having
			// parts,
			// create a new SkinObj with the info available from the default
			// skin
			// directory for the phone model and return it

			if ((!hasChildNodes())
					|| ((getCurrentProperty() != null) && (getCurrentProperty()
							.equals(ThemeTag.ATTR_SINGLE_BITMAP)))) {
				// Support For Landscape Previews - start
				// tg = createThemeGraphic(false,pState);
				tg = createThemeGraphic(false, screen, layout);
				// Support For Landscape Previews - end
			}

		}
		if (tg != null) {
			if ((link.getAttributeValue(ThemeTag.ATTR_TILE) != null)
					&& (link.getAttributeValue(ThemeTag.ATTR_TILE)
							.equals(ThemeTag.ATTR_TILE_DEFAULT))) {
				for (int i = 0; i < tg.getImageLayers().size(); i++) {
					ImageLayer il = (ImageLayer) tg.getImageLayers().get(i);
					il.setAttribute(ThemeTag.ATTR_TILE,
							ThemeTag.ATTR_TILE_DEFAULT);
				}
			}

		}
		return tg;
	}

	/**
	 * Method to create new ThemeGraphic object for an entity
	 * 
	 * @param identifier String holds the identifier of the entity
	 * @return ThemeGraphic ThemeGraphic for the entity
	 */
	private ThemeGraphic createThemeGraphic(PreviewImage screen)
			throws ThemeException {
		return createThemeGraphic(true, screen);
	}

	// Support For Landscape Previews - start
	private ThemeGraphic createThemeGraphic(boolean makeNew, PreviewImage screen)
			throws ThemeException {
		return createThemeGraphic(makeNew, screen, null);
	}

	// Support For Landscape Previews - end
	/**
	 * Method to create new ThemeGraphic object for an entity
	 * 
	 * @param makeNew creates a new ThemeGraphic if coulnt not find the
	 *            ThemeGraphic
	 * @return ThemeGraphic ThemeGraphic for the entity
	 */
	// Support For Landscape Previews - start
	// private ThemeGraphic createThemeGraphic(boolean makeNew,PreviewState
	// pState)
	// throws ThemeException {
	private ThemeGraphic createThemeGraphic(boolean makeNew,
			PreviewImage screen, Layout layout) throws ThemeException {
		// Support For Landscape Previews - end
		if (link != this) {
			return (((SkinnableEntity) link)
					.createThemeGraphic(makeNew, screen));
		}

		// here we check against the model object instead of the name so
		// it's possible to create themes with model names
		if (!((Theme) getRoot()).isModel()) {
			SkinnableEntity se = getModel().getSkinnableEntity(
					this.getIdentifier());
			if (se == null)
				return getFromPath(makeNew);
			// ThemeGraphic tg = se.getThemeGraphic(pState,null);
			ThemeGraphic tg = se.getThemeGraphic();
			// Support For Landscape Previews - start
			if (tg != null) {
				tg = (ThemeGraphic) tg.clone();
				tg.setData(this);
			} else {
				tg = getFromPath(makeNew);
			}
			return tg;
		}
		ThemeGraphic t = getFromPath(makeNew);
		if (t != null && layout != null) {
			t.setAttribute(ThemeTag.LAYOUT_WIDTH, layout.W() + "");
			t.setAttribute(ThemeTag.LAYOUT_HEIGHT, layout.H() + "");
		}
		return t;
	}

	private ThemeGraphic getFromPath(boolean makeNew) {
		try {
			Theme theme = (Theme) getRoot();
			String skinDirectory = theme.getThemeDir();
			String fileName = null;

			String[] fileAndMask = findFileAndMask(skinDirectory, false);
			String maskName = null;

			// if it fails... go to defaultskindir
			if (fileAndMask == null) {
				String defaultSkinDir = theme.getModel().getThemeDir();
				fileAndMask = findFileAndMask(defaultSkinDir, false);
			}

			// if it fails... create balmk image in the temp directory and
			// return it
			if ((fileAndMask == null) && (makeNew)) {
				String tempDirectory = theme.getTempDir();
				fileAndMask = findFileAndMask(tempDirectory, true);
			}

			// if (fileAndMask == null)
			// return null;
			ThemeGraphic tg = ((Theme) getRoot()).createGraphic(this);
			ImageLayer il = new ImageLayer(tg);

			if (fileAndMask != null) {
				fileName = fileAndMask[0];
				maskName = (fileAndMask.length == 2) ? fileAndMask[1] : null;
				il.setAttributes(initializeSkinImage());
				il.setAttribute(ThemeTag.FILE_NAME, fileName);
				if (maskName != null) {
					il.setAttribute(this.getToolBox().SoftMask + "", maskName);
				}
			} else {
				il.setAttributes(initializeSkinImage());
				if (this.getToolBox().multipleLayersSupport) {
					il.setAttribute(ThemeTag.ATTR_NAME,
							ThemeTag.ELEMENT_BACKGROUND_NAME);
					
					// 
					// Set<String> preDependentElements = (((Theme)
					// getRoot()).getPrevS60Skin()).getThemePreview().getDependentsForBackgroundLayer(getIdentifier());
					// Changed the API signature to pass the PreviewState object
					// to get the componenetName
					// VarietyId, locId and screen name while hitting the
					// layoutfiles for getting the layout
					// ### Set<String> preDependentElements = (((Theme)
					// getRoot()).getPrevS60Skin()).getThemePreview().getDependentsForBackgroundLayer(getIdentifier(),pState);
					// 
					// ### (((Theme)
					// getRoot()).getPrevS60Skin()).getDependency().registerDependency(getIdentifier(),
					// preDependentElements);
				} else {
					
					String imgPath = FileUtils.getFile(
							FileUtils.getURL(PlatformCorePlugin.getDefault(),
									"image/transparent.svg")).getAbsolutePath();
					il.setAttribute(ThemeTag.ATTR_NAME, "layer0");
					il.setAttribute(ThemeTag.FILE_NAME, imgPath);
					il.setAttribute(ThemeTag.ATTR_NO_IMAGE, ThemeTag.ATTR_NO_IMAGE);
				}
			}

			tg.setImageLayers(il);
		
			return tg;
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return null;
	}

	/**
	 * Method which tries to get the file and the mask for an entity in the
	 * given directory
	 * 
	 * @param directory String holds the path of the directory
	 * @param createBlankImage boolean blank image is created if set to true
	 * @return String[] holds the file and the mask
	 */
	private String[] findFileAndMask(String directory, boolean createBlankImage)
			throws ThemeException {
		String fileName = null;
		String maskName = null;
		String[] fileAndMask = null;

		String identifier = getIdentifier();
		fileName = FileUtils.makeAbsolutePath(directory, identifier
				+ ThemeTag.SVG_FILE_EXTN);

		File f = new File(fileName);

		if ((!f.exists()) && createBlankImage) {
			fileName = FileUtils.makeAbsolutePath(directory, identifier
					+ ThemeTag.FILE_TYPE_BMP);
			f = new File(fileName);
		}

		if (f.exists()) {

			if (this.getToolBox().SoftMask) {
				maskName = FileUtils.makeAbsolutePath(directory, identifier
						+ ThemeTag.SOFTMASK_FILE + ThemeTag.FILE_TYPE_BMP);
				f = new File(maskName);
				if (!f.exists()) {
					maskName = null;
					fileAndMask = new String[1];
				} else {
					fileAndMask = new String[2];
					fileAndMask[1] = maskName;
				}
			} else {
				maskName = FileUtils.makeAbsolutePath(directory, identifier
						+ ThemeTag.MASK_FILE + ThemeTag.FILE_TYPE_BMP);
				f = new File(maskName);
				if (!f.exists()) {
					maskName = null;
					fileAndMask = new String[1];
				} else {
					fileAndMask = new String[2];
					fileAndMask[1] = maskName;
				}
			}
			fileAndMask[0] = fileName;
		}

		return fileAndMask;

	}

	/**
	 * Helper method to initialze the attributes for ThemeGraphic
	 * 
	 * @return Map attributes map for ThemeGraphic
	 */
	private Map<Object, Object> initializeSkinImage() {

		Map<Object, Object> siAttr = new HashMap<Object, Object>();

		String colour = null;
		colour = link.getAttributeValue(ThemeTag.ATTR_DEF_COLOUR_RGB);

		if (colour != null) {
			siAttr.put(ThemeTag.ATTR_COLOUR_RGB, colour);
			return siAttr;
		}

		colour = link.getAttributeValue(ThemeTag.ATTR_DEF_COLOUR_IDX);

		if (colour != null) {
			siAttr.put(ThemeTag.ATTR_COLOUR_IDX, colour);
			return siAttr;
		}

		siAttr.put(ThemeTag.ATTR_ENTITY_X, ThemeTag.ATTR_ENTITY_X_DEFAULT);
		siAttr.put(ThemeTag.ATTR_ENTITY_Y, ThemeTag.ATTR_ENTITY_Y_DEFAULT);
		siAttr.put(ThemeTag.ATTR_IMAGE_X, ThemeTag.ATTR_IMAGE_X_DEFAULT);
		siAttr.put(ThemeTag.ATTR_IMAGE_Y, ThemeTag.ATTR_IMAGE_Y_DEFAULT);
		
		siAttr.put(ThemeTag.ATTR_COLOURDEPTH,
				ThemeTag.ATTR_VALUE_COLOUR_DEPTH_DEFAULT);
		siAttr.put(ThemeTag.ATTR_STRETCH, ThemeTag.ATTR_STRETCH_DEFAULT);
		siAttr.put(ThemeTag.ATTR_ANGLE, ThemeTag.ATTR_ANGLE_DEFAULT);

		return siAttr;
	}

	/**
	 * Method to get the actual skin image
	 * 
	 * @return The object containing the details of the image.
	 */

	public ThemeGraphic getActualThemeGraphic() throws ThemeException {

		try {
			if (link != this) {
				return (((SkinnableEntity) link).getActualThemeGraphic());
			}

			if ((this.themeGraphics != null && (actualImagePosition != -1))) {
				ThemeGraphic tg = (ThemeGraphic) (this.themeGraphics
						.get(actualImagePosition));
				if ((link.getAttributeValue(ThemeTag.ATTR_TILE) != null)
						&& (link.getAttributeValue(ThemeTag.ATTR_TILE)
								.equals(ThemeTag.ATTR_TILE_DEFAULT))) {
					if (tg != null) {
						for (int i = 0; i < tg.getImageLayers().size(); i++) {
							ImageLayer il = (ImageLayer) tg.getImageLayers()
									.get(i);
							il.setAttribute(ThemeTag.ATTR_TILE,
									ThemeTag.ATTR_TILE_DEFAULT);
						}
					}
				}

				tg.removeAttribute(ThemeTag.LAYOUT_WIDTH);
				tg.removeAttribute(ThemeTag.LAYOUT_HEIGHT);
				return tg;
			} else {
				return null;
			}

		} catch (Exception e) {
		}
		return null;
	}

	public List getDraftThemeGraphics() throws ThemeException {
		return getDraftThemeGraphics(null, null, null);
	}

	/**
	 * Method to get the list of draft skin ThemeGraphics
	 * 
	 * @return A list containing the draft ThemeGraphics.
	 */

	public List getDraftThemeGraphics(Display display, PreviewImage screen,
			ComponentInfo component) throws ThemeException {

		// if (this.isEntityType().equals(ThemeTag.ATTR_BMPANIM)) {
		// if (themeGraphics != null) {
		// return ((ThemeGraphicInterface) this.themeGraphics.get(0))
		// .getDraftThemeGraphics();
		// } else {
		// return null;
		// }
		// }
		if (link != this) {
			return (((SkinnableEntity) link).getDraftThemeGraphics(display,
					screen, component));
		}

		if (this.themeGraphics == null) {
			return null;
		} else if (actualImagePosition == -1) {

			for (int k = 0; k < this.themeGraphics.size(); k++) {
				ThemeGraphic tg = (ThemeGraphic) this.themeGraphics.get(k);
				Layout lay;
				if (screen != null && this instanceof Part)
					lay = getLayoutInfoForPreview(display,
							component.getLocId(), screen);
				else if (screen != null && this instanceof Element)
					lay = getLayoutInfoForPreview(display, component, screen);
				else
					lay = getLayoutInfo();
				tg.setAttribute(ThemeTag.LAYOUT_WIDTH, lay.W() + "");
				tg.setAttribute(ThemeTag.LAYOUT_HEIGHT, lay.H() + "");
				if ((link.getAttributeValue(ThemeTag.ATTR_TILE) != null)
						&& (link.getAttributeValue(ThemeTag.ATTR_TILE)
								.equals(ThemeTag.ATTR_TILE_DEFAULT))) {
					if (tg != null) {
						for (int l = 0; l < tg.getImageLayers().size(); l++) {
							ImageLayer il = (ImageLayer) tg.getImageLayers()
									.get(l);
							il.setAttribute(ThemeTag.ATTR_TILE,
									ThemeTag.ATTR_TILE_DEFAULT);
						}
					}
				}
			}
			return this.themeGraphics;
		}

		List<Object> draftImages = new ArrayList<Object>();

		draftImages.addAll(this.themeGraphics.subList(0, this.themeGraphics
				.size()));
		draftImages.remove(actualImagePosition);

		if (draftImages.size() == 0)
			return null;

		for (int k = 0; k < draftImages.size(); k++) {
			ThemeGraphic tg = (ThemeGraphic) draftImages.get(k);
			if ((link.getAttributeValue(ThemeTag.ATTR_TILE) != null)
					&& (link.getAttributeValue(ThemeTag.ATTR_TILE)
							.equals(ThemeTag.ATTR_TILE_DEFAULT))) {
				if (tg != null) {
					for (int l = 0; l < tg.getImageLayers().size(); l++) {
						ImageLayer il = (ImageLayer) tg.getImageLayers().get(l);
						il.setAttribute(ThemeTag.ATTR_TILE,
								ThemeTag.ATTR_TILE_DEFAULT);
					}
				}
			}
		}
		return draftImages;
	}

	/**
	 * Method to set the actual skin ThemeGraphic
	 * 
	 * @param ThemeGraphic The object containing the details of the
	 *            ThemeGraphic.
	 */
	public void setActualGraphic(ThemeGraphic graphic) throws ThemeException {

		setActualGraphic(graphic, true);

	}

	private void setActualGraphic(ThemeGraphic graphic, boolean isSkin) throws ThemeException {
		setSkinned(isSkin);
		if (graphic == null) {
			if ((((SkinnableEntity) link).themeGraphics != null)
					&& actualImagePosition != -1) {

				synchronized (this) {
					// ((SkinnableEntity)link).themeGraphics.remove(actualImagePosition);
					(((SkinnableEntity) link).getActualThemeGraphic())
							.setAttribute(ThemeTag.ATTR_STATUS,
									ThemeTag.ATTR_VALUE_DRAFT);
					((SkinnableEntity) link).actualImagePosition = -1;
				}
			}
			return;
		}

		if (link != this) {
			((SkinnableEntity) link).setActualGraphic(graphic);
			return;
		}

		graphic.setData(link);

		if (((SkinnableEntity) link).themeGraphics == null) {
			synchronized (this) {
				((SkinnableEntity) link).themeGraphics = new ArrayList<Object>();

			}
		}

		for (int i = 0; i < ((SkinnableEntity) link).themeGraphics.size(); i++) {
			ThemeGraphic im = (ThemeGraphic) ((SkinnableEntity) link).themeGraphics
					.get(i);

			// if a match is found
			if ((im != null) && (im.hashCode() == graphic.hashCode())) {
				synchronized (this) {
					// same as that of already existing actual image
					im.setAttribute(ThemeTag.ATTR_STATUS,
							ThemeTag.ATTR_VALUE_ACTUAL);
					((SkinnableEntity) link).actualImagePosition = i;
					return;
				}
			}
		}

		for (int i = 0; i < ((SkinnableEntity) link).themeGraphics.size(); i++) {
			ThemeGraphic im = (ThemeGraphic) ((SkinnableEntity) link).themeGraphics
					.get(i);
			// if a match is found replace existing actual.
			if ((im != null) && (im.equals(graphic))) {
				synchronized (this) {
					((SkinnableEntity) link).themeGraphics.set(i, graphic);
					return;
				}
			}
		}

		// when no match is found
		synchronized (this) {
			if (actualImagePosition == -1) {
				// // No actual add actual
				((SkinnableEntity) link).themeGraphics.add(graphic);
				((SkinnableEntity) link).actualImagePosition = ((SkinnableEntity) link).themeGraphics
						.size() - 1;
			}
		}
	}

	/**
	 * Method to set the draft skin ThemeGraphic
	 * 
	 * @param ThemeGraphic The object containing the details of the image.
	 */
	public void setDraftGraphic(ThemeGraphic graphic) throws ThemeException {
		// setSelected(true, false, true);
		if (link != this) {
			((SkinnableEntity) link).setDraftGraphic(graphic);
			return;
		}

		if (graphic == null)
			return;

		// if an image is to be set as draft, forcefully set its status
		// attribute to draft
		graphic.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_DRAFT);

		boolean isSame = false;

		List imList = null;

		if (((SkinnableEntity) link).themeGraphics == null) {
			((SkinnableEntity) link).themeGraphics = new ArrayList<Object>();
			graphic.setData(this);
			((SkinnableEntity) link).themeGraphics.add(graphic);

			return;
		} else { // if already draft images are available
			imList = ((SkinnableEntity) link).themeGraphics;
		}

		// Check if the new image matches the existing one..
		if (imList != null) {
			for (int i = 0; i < imList.size(); i++) {
				ThemeGraphic im = (ThemeGraphic) imList.get(i);
				if (im.hashCode() == graphic.hashCode()) {
					isSame = true;
					break;
				}
			}
		}

		// if the draftThemeGraphic to be set is not a duplicate of the refered
		// entity's draft ThemeGraphic,
		// 1. initialise the themeGraphics list if its null.
		// 2. add the draftThemeGraphic
		// 3. make the referd ThemeGraphics available for this entity by cloning
		// 4. add the cloned ThemeGraphics to the themeGraphics List by
		// setThemeGraphic(ThemeGraphic)
		// 5. remove the refer
		if (isSame == false) {
			synchronized (this) {
				if (((SkinnableEntity) link).themeGraphics == null) {
					((SkinnableEntity) link).themeGraphics = new ArrayList<Object>();
				}

				// now set the draft image
				graphic.setData(link);
				((SkinnableEntity) link).themeGraphics.add(graphic);
			}
		}

	}

	public void setAnimateGraphic(Object atg) {
		if (atg == null)
			return;

		themeGraphics = new ArrayList<Object>(1);
		((ThemeGraphic) atg).setData(this);
		themeGraphics.add(atg);
		actualImagePosition = 0;
	}

	public void setGraphicInAnimation(ThemeGraphic graphic)
			throws ThemeException {
		Object animObj = getAnimatedObject();
		if (animObj == null) {
			themeGraphics = new ArrayList<Object>(1);
			AnimatedThemeGraphic atg = new AnimatedThemeGraphic(this);
			themeGraphics.add(atg);
			actualImagePosition = 0;
			atg.addThemeGraphic(graphic);
			this.setSkinned(true);
		} else if (animObj instanceof AnimatedThemeGraphicCache) {
			List animGraphicsList = ((ThemeGraphicInterface) animObj)
					.getThemeGraphics();
			// /// Copy ThemeGraphics and Images
			AnimatedThemeGraphic atg = new AnimatedThemeGraphic(this);
			for (int i = 0; i < animGraphicsList.size(); i++) {
				ThemeGraphic tg = (ThemeGraphic) animGraphicsList.get(i);
				if (!graphic.getAttribute(ThemeTag.UNIQUE_ID).equals(
						tg.getAttribute(ThemeTag.UNIQUE_ID))) {
					
					atg.addThemeGraphic(tg);
				} else {
					graphic.setAttribute(ThemeTag.ATTR_ANIMATE_SEQNO, tg
							.getAttribute(ThemeTag.ATTR_ANIMATE_SEQNO));
					graphic.setAttribute(ThemeTag.ATTR_ANIMATE_TIME, tg
							.getAttribute(ThemeTag.ATTR_ANIMATE_TIME));
					atg.addThemeGraphic(graphic);
				}
			}
			this.themeGraphics.clear();
			this.themeGraphics.add(atg);
		} else {
			((ThemeGraphicInterface) animObj).setGraphic(graphic);
		}
	}
	
	

	/**
	 * Sets the detail of the ThemeGraphic associated with the object
	 * 
	 * @param ThemeGraphic The ThemeGraphic object containing the ThemeGraphic
	 *            details
	 */
	public void setThemeGraphic(ThemeGraphic graphic) throws ThemeException {
		setThemeGraphic(graphic, true);
	}

	public void setThemeGraphic(ThemeGraphic graphic, boolean isSkin) throws ThemeException {
		// if (this.isEntityType().equals(ThemeTag.ATTR_BMPANIM)) {
		// setGraphicInAnimation(graphic);
		// return;
		// }

		if (link != this) {
			((SkinnableEntity) link).setThemeGraphic(graphic, isSkin);
			return;
		}

		if (graphic == null) {
			clearThemeGraphic();
			return;
		}

		String graphicType = graphic.getAttribute(ThemeTag.ATTR_STATUS);
		if (graphicType != null
				&& graphicType.equalsIgnoreCase(ThemeTag.ATTR_VALUE_ACTUAL)) {
			setActualGraphic(graphic, isSkin);
		} else {
			setDraftGraphic(graphic);
		}
	}

	public int getMinInThemeGraphic(ThemeGraphic tg, String key) {

		int min = 0;
		try {
			List imageLayers = tg.getImageLayers();

			for (int i = 0; i < imageLayers.size(); i++) {
				String temp = ((ImageLayer) imageLayers.get(i))
						.getAttribute(key);
				int val = (temp == null) ? 0 : Integer.valueOf(temp).intValue();
				if (i == 0)
					min = val;
				min = (min < val) ? min : val;
			}
		} catch (Exception e) {
		}
		return min;
	}

	/**
	 * Method to find if the Entity has Actual ThemeGraphic
	 * 
	 * @return boolean true if actual ThemeGraphic is found
	 */
	/*
	 * public boolean hasActualThemeGraphic() { String refer =
	 * getReferedIdentifier(); if (refer == null) { if (((SkinnableEntity)
	 * link).actualImagePosition == -1) { return false; } else { return true; } }
	 * else { Theme root = (Theme) this.getRoot(); ThemeBasicData refered =
	 * null; refered = root.getSkinnableEntity(refer); if (refered != null) {
	 * return ((SkinnableEntity) refered).hasActualThemeGraphic(); } } return
	 * false; }
	 */

	public List getAllThemeGraphics() throws ThemeException {
		return ((SkinnableEntity) link).themeGraphics;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#getDisplay()
	 */
	public Display getDisplay() {
		Theme theme = (Theme) getRoot();
		// here tries to find the proper resolution that has the layout info for
		// the element, relies completely on the idmappings correctness
		Display themeDisplay = theme.getDisplay();
		if (supportsDisplay(themeDisplay)) {
			return themeDisplay;
		}
		for (Display display : theme.getDisplays()) {
			if (supportsDisplay(display)) {
				return display;
			}
		}

		return themeDisplay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#supportsDisplay(com.nokia.tools.platform.core.Display)
	 */
	public boolean supportsDisplay(Display display) {
		IdMappingsHandler handler = ThemePlatform
				.getIdMappingsHandler(((Theme) getRoot()).getThemeId());
		String id = getIdentifier();
		return handler.getComponentInfo(this, display, id) != null;
	}

	/*
	 * GET LAYOUT FOR CURRENT PHONE MODEL AND ACTIVE RESOLUTION FOR THE
	 * SPECIFIED SKINELEMENT WITH THE FULL_MODIFY_ADAPTER VARIETY INDEX AND
	 * FULL_MODIFY_ADAPTER LOC
	 */
	// returns the layout for the skinElement for default varietyId and default
	// loc_id
	public Layout getLayoutInfo(String locId, boolean isOptimized) throws ThemeException {
		Display display = getDisplay();
		if ( (getParent().getAttributeValue(ThemeConstants.ATTR_USE_LOCID) != null)) {
			return getLayoutInfo(display, Integer.MIN_VALUE,
					null, null);
		}
		return getLayoutInfo(display, Integer.MIN_VALUE,
				locId, null);
	}
	
	public Layout getLayoutInfo() throws ThemeException {
		Display display = getDisplay();
		if ( (getParent().getAttributeValue(ThemeConstants.ATTR_USE_LOCID) != null)) {
			return getLayoutInfo(display, Integer.MIN_VALUE,
					null, null);
		}
		return getLayoutInfo(display, Integer.MIN_VALUE,
				getDefaultLocId(display), null);
	}

	/*
	 * GET THE LAYOUT INFO FOR ACTIVE PHONE MODEL AND ACTIVE RESOLUTION FOR THE
	 * SPECIFIED SKINELEMENT ANME AND VARIETY INDEX AND LOC ID
	 */
	public Layout getLayoutInfo(int varietyId, String loc_id)
			throws ThemeException {
		return getLayoutInfo(getDisplay(), varietyId, loc_id, null);

	}

	/*
	 * GET THE LAYOUT FOR THE SPECIFIED PHONE AND SPECIFIED RESOLUTION WITH
	 * SPECIFIED SKIN ELEMENT NAME
	 */
	public Layout getLayoutInfo(Display display) throws ThemeException {
		if (display == null) {
			display = getDisplay();
		}
		return getLayoutInfo(display, Integer.MIN_VALUE,
				getDefaultLocId(display), null);

	}

	public Layout getLayoutInfo(String loc_id) throws ThemeException {
		return getLayoutInfo(getDisplay(), loc_id);
	}

	public Layout getLayoutInfo(Display display, String loc_id)
			throws ThemeException {
		return getLayoutInfo(display, Integer.MIN_VALUE, loc_id, null);
	}

	private Layout getColorLayoutInfo(Display display) throws ThemeException {
		Theme s60 = (Theme) (this.getRoot());
		String skinElementId = getLayoutId();
		String defaultPreviewScreen = getAttributeValue(ThemeTag.ATTR_PREVIEWSCREEN);

		List<ComponentInfo> components = new ArrayList<ComponentInfo>();
		if (StringUtils.isEmpty(defaultPreviewScreen)) {
			components.add(getComponentInfo(display));
		} else {
			// default preview screen found
			// so get all preview elements in this screen that use this
			// colorgroup
			PreviewImage screen = s60.getThemePreview().getPreviewImageByName(
					defaultPreviewScreen, Collections.singleton(display));

			List<PreviewElement> previewElementsList = screen == null ? null
					: screen.getPreviewElementsForColor(skinElementId);

			if (previewElementsList == null || previewElementsList.size() <= 0) {
				throw new ThemeException(
						"No matching preview elements found in "
								+ defaultPreviewScreen + " for cologroup "
								+ skinElementId);
			}
			for (PreviewElement currElement : previewElementsList) {
				if (currElement.supportsDisplay(display)) {
					components.add(currElement.getComponentInfo());
				}
			}
		}

		// now get the layotus using the compNames, varietyIds and locIds lists
		try {
			getLayoutContext(display).calculate(
					components.toArray(new ComponentInfo[components.size()]));
		} catch (Exception e) {
			throw new ThemeException(e);
		}

		if (components.size() == 1) {
			return components.get(0).getLayout();
		}

		Rectangle unionRect = new Rectangle(0, 0, 0, 0);
		int minLeftOffset = Integer.MAX_VALUE;
		int minTopOffset = Integer.MAX_VALUE;
		int minRightOffset = Integer.MAX_VALUE;
		int minBottomOffset = Integer.MAX_VALUE;

		for (ComponentInfo component : components) {
			Layout layout = component.getLayout();
			minLeftOffset = Math.min(minLeftOffset, layout.L());
			minTopOffset = Math.min(minTopOffset, layout.T());
			// maxRightOffset = Math.max(maxRightOffset,(layouts[i].W() +
			// layouts[i].L()));
			// maxBottomOffset = Math.max(maxBottomOffset,(layouts[i].H() +
			// layouts[i].T()));
			minRightOffset = Math.min(minRightOffset, layout.R());
			minBottomOffset = Math.min(minBottomOffset, layout.B());

		}

		unionRect.x = minLeftOffset;
		unionRect.y = minTopOffset;
		unionRect.width = display.getWidth() - minRightOffset - minLeftOffset;
		unionRect.height = display.getHeight() - minBottomOffset - minTopOffset;

		Layout unionLayout = new Layout();

		unionLayout.setL((int) unionRect.getX());
		unionLayout.setT((int) unionRect.getY());
		unionLayout.setW((int) unionRect.getWidth());
		unionLayout.setH((int) unionRect.getHeight());
		unionLayout.setB(minBottomOffset);
		unionLayout.setR(minRightOffset);

		return unionLayout;
	}

	private Layout getSoundLayoutInfo(Display display) throws ThemeException {

		Theme s60 = (Theme) (this.getRoot());
		String soundExtension = null;
		Set<String> extensions = s60.getSoundExtensions();
		if (!extensions.isEmpty()) {
			soundExtension = extensions.iterator().next();
		}
		RenderedImage soundImage = ThemeappJaiUtil
				.getImageForFile(soundExtension);
		Layout soundLayout = new Layout();
		soundLayout.setLayout(0, 0, 0, 0, soundImage.getWidth(), soundImage
				.getHeight());

		return soundLayout;
	}

	public Layout getLayoutInfo(Display display, int varietyId, String loc_id,
			String parentDefaultScreen) throws ThemeException {
		String entityType = isEntityType();
		if (entityType.equalsIgnoreCase(ThemeTag.ELEMENT_EMBED_FILE)) {
			Layout lay = new Layout();
			lay.setLayout(0, 48, 0, 48, 48, 48);
			return lay;
		}
		if (entityType.equalsIgnoreCase(ThemeTag.ELEMENT_COLOUR)) {

			Layout colorLayout = null;
			try {
				colorLayout = getColorLayoutInfo(display);
			} catch (Exception ex) {
				//System.out.println("DEBUG: " + ex.getMessage());
			}
			if (colorLayout != null)
				return colorLayout;
		}

		if (entityType.equalsIgnoreCase(ThemeTag.ELEMENT_SOUND)) {
			return getSoundLayoutInfo(display);
		}

		Theme s60 = (Theme) (this.getRoot());
		if (display == null) {
			display = getDisplay();
		}

		String compName = null;
		String skinElementId = getLayoutId();

		if (varietyId < 0) {

			varietyId = Integer.MIN_VALUE;

		}

		boolean isFrameType = false;
		String frameLocId = null;

		if (entityType.equalsIgnoreCase(ThemeTag.ELEMENT_FRAME)) {
			isFrameType = true;
		}

		// steps to get the layout details
		// 1. Convert the Skin Name to LCD ID using idmaps.xml
		// 2. Get the default preview screen for this skin name from the
		// design.xm;
		// 3. Use the preview subsystem to get the option Id and Loc id for that
		// skin element from its preview screen
		// 4. Call the Layout subsystem's LayoutUtils apis to get the layout for
		// the specified details.

		// step 1 is pending due to open issue
		// for now using the preview subsystem only to retrieve the skin name

		// step 2

		ComponentInfo skinInfo = null;
		PreviewImage screen = null;
		boolean foundInDefaultPreviewScreen = false;
		ComponentInfo layoutComponent = null;

		screen = s60.getThemePreview().getPreviewImageForElem(this, false);
		if (screen != null) {
			// get the preview element corresponding to the specified
			// skinElementName from the default preview scr`een
			PreviewElement pelement = null;

			if (screen != null) {
				if (varietyId < 0 || varietyId == Integer.MIN_VALUE) {
					pelement = screen.getPreviewElement(display, skinElementId);
				} else {
					pelement = screen.getPreviewElement(display, skinElementId,
							varietyId);
				}
			}
			/**
			 * added for digital clock .... so that layout for the splMasks will
			 * come the same as the digit.
			 */
			if (pelement == null && screen != null) {
				pelement = screen.getPreviewElementWithSplMask(skinElementId);
			}

			if (pelement != null) {

				compName = pelement.getCompName();

				varietyId = Integer
						.parseInt(pelement
								.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_VARIETYID));

				// get the loc_id from the preview element
				String peLocId = pelement.getLocId();
				if (peLocId != null) {
					loc_id = peLocId;
				} else {
					if (StringUtils.isEmpty(loc_id)) {
						loc_id = null;
					}
				}
				display = pelement.getDisplay();
				foundInDefaultPreviewScreen = true;

				frameLocId = (loc_id != null ? loc_id : "") + "<LC>" + compName
						+ "@V:" + varietyId + "</LC>";
			}
		}
		if (!foundInDefaultPreviewScreen)
			skinInfo = getComponentInfo(display);

		if (foundInDefaultPreviewScreen == false || isFrameType == true) {
			// throw exception if skinId doesnt have parts..
			// if skinId has parts return the Rect union
			if (!this.hasChildNodes()) {
				if (skinInfo == null) {
					throw new ThemeException(
							"Layout idmappings not available for "
									+ skinElementId);
				}
			} else {

				List parts = this.getChildren();

				if (parts != null && parts.size() > 0) {

					String tempLocId = (isFrameType ? frameLocId : loc_id);

					return getFrameLayout(display, null, tempLocId);
				}

				// doesnt have its own Idmappings and doesnt have parts also
				// so throw exception

				throw new ThemeException(
						"Idmappings not available for the specified skinId "
								+ this.getIdentifier());
			}
		}
		if (layoutComponent == null) {
			if (skinInfo == null) {
				layoutComponent = new ComponentInfo(compName, varietyId, loc_id);
			} else {
				layoutComponent = skinInfo;
				if (loc_id != null) {
					layoutComponent.setLocId(loc_id);
				}
			}
		}
		try {
			getLayoutContext(display).calculate(layoutComponent);
		} catch (Throwable e) {
			throw new ThemeException(e);
		}
		return layoutComponent.getLayout();
	}

	private Layout getFrameLayout(Display display, PreviewImage screen,
			String frameLocId) throws ThemeException {

		List parts = this.getChildren();
		Rectangle unionRect = new Rectangle(0, 0, 0, 0);
		int minLeftOffset = Integer.MAX_VALUE;
		int minTopOffset = Integer.MAX_VALUE;

		int minBottomOffset = Integer.MAX_VALUE;
		int minRightOffset = Integer.MAX_VALUE;

		if (display == null) {
			if (screen == null) {
				display = getDisplay();
			} else {
				display = screen.getDisplay();
			}
		}

		Layout unionLayout = null;

		if (parts != null && parts.size() > 0) {

			String tempLocId = frameLocId;
			for (int i = 0; i < parts.size(); i++) {

				SkinnableEntity currPart = (SkinnableEntity) parts.get(i);
				Layout tempLayout = currPart
						.getLayoutInfo(
								display,
								Integer.MIN_VALUE,
								tempLocId,
								screen == null ? getAttributeValue(ThemeTag.ATTR_PREVIEWSCREEN)
										: screen.getName());

				if (tempLayout == null) {

					throw new ThemeException("Layout for part "
							+ currPart.getIdentifier() + " of skinId "
							+ this.getIdentifier() + " not found.");
				}

				minLeftOffset = Math.min(minLeftOffset, tempLayout.L());
				minTopOffset = Math.min(minTopOffset, tempLayout.T());
				minRightOffset = Math.min(minRightOffset, tempLayout.R());
				minBottomOffset = Math.min(minBottomOffset, tempLayout.B());

			}

			unionLayout = new Layout();

			unionRect.x = minLeftOffset;
			unionRect.y = minTopOffset;
			// Support For Landscape Previews - start
			unionRect.width = display.getWidth() - minRightOffset
					- minLeftOffset;
			unionRect.height = display.getHeight() - minBottomOffset
					- minTopOffset;
			// Support For Landscape Previews - end
			unionLayout.setL((int) unionRect.getX());
			unionLayout.setT((int) unionRect.getY());
			unionLayout.setW((int) unionRect.getWidth());
			unionLayout.setH((int) unionRect.getHeight());
			unionLayout.setB(minBottomOffset);
			unionLayout.setR(minRightOffset);

		}
		return unionLayout;
	}

	public Layout getLayoutInfoForPreview(Display display,
			ComponentInfo component, PreviewImage screen) throws ThemeException {
		boolean isFrameType = false;
		String loc_id = component.getLocId();
		String frameLocId = loc_id;
		if (display == null) {
			if (screen == null) {
				display = getDisplay();
			} else {
				display = screen.getDisplay();
			}
		}
		if (display == null) {
			display = getDisplay();
		}

		if (this.isEntityType().equalsIgnoreCase(ThemeTag.ELEMENT_FRAME)) {
			isFrameType = true;
		}

		if (isFrameType && this instanceof Element) {
			frameLocId = (loc_id != null ? loc_id : "") + "<LC>"
					+ component.getName() + "@V:" + component.getVariety()
					+ "</LC>";
			return getFrameLayout(display, screen, frameLocId);
		} else {
			return getLayoutForPreviewNonFrame(display, component);
		}

	}

	public RenderedImage ProcessImage() throws ThemeException {
		ThemeGraphic tg = getThemeGraphic();
		RenderedImage img = null;

		if (tg != null) {
			
			img = ProcessList(tg.getImageLayers());
		}
		return img;
	}

	/**
	 * @param list
	 */
	public RenderedImage ProcessList(List list) throws ThemeException {
		RenderedImage img = null;
		Theme theme = (Theme) getRoot();
		Display display = ((Theme) getRoot()).getDisplay();
		for (int m = 0; m < list.size(); m++) {
			ImageLayer iml = (ImageLayer) list.get(m);
			try {
				img = CoreImage.create().load(new File(iml.getFileName(theme)),
						display.getWidth(), display.getHeight()).getAwt();
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
			List l = iml.getLayerEffects();

			if (l == null)
				return img;

			for (int i = 0; i < l.size(); i++) {
			
			}

		}
		return img;

	}

	/**
	 * @return Returns the derivedLayout.
	 */
	public String getDerivedLayoutId() {
		if (this.attributes.containsKey(ThemeTag.ATTR_DERIVED_LAYOUT_ID)) {
			return (String) this.attributes
					.get(ThemeTag.ATTR_DERIVED_LAYOUT_ID);
		} else {
			return null;
		}
	}

	/**
	 * @param derivedLayout The derivedLayout to set.
	 */
	public void setDerivedLayoutId(String derivedLayoutId) {

		this.attributes.put(ThemeTag.ATTR_DERIVED_LAYOUT_ID, derivedLayoutId);

	}

	public String getDefaultLocId(Display display) {
		// uses the routine as in the getLayoutInfo to check only the real
		// screens, so this will be consistent with the getLayoutInfo
		PreviewImage screen = ((Theme) getRoot()).getThemePreview()
				.getPreviewImageForElem(this, null, false, false);
		if (screen != null) {
			PreviewElement pelement = screen.getPreviewElement(display,
					getIdentifier(), null,null,false);
			if (pelement != null) {
				// get the loc_id from the preview element
				return pelement.getLocId();
			}
		}

		return null;
	}

	public Layout getLayoutInfoForPreview(Display display, String loc_id,
			PreviewImage screen) throws ThemeException {

		Layout layout = null;

		if (display == null) {
			display = getDisplay();
		}

		int varietyId = Integer.MIN_VALUE;
		
		String skinElementId = getLayoutId();

		ComponentInfo component = getComponentInfo(display);

		// throw exception if skinId doesnt have parts..
		// if skinId has parts return the Rect union
		if (component == null) {
			throw new ThemeException("Layout idmappings not available for "
					+ skinElementId);
		}
		if (this.isEntityType().equalsIgnoreCase(ThemeTag.ELEMENT_FRAME)
				&& !(this instanceof Part)) {
			String frameLocId = (loc_id != null ? loc_id : "") + "<LC>"
					+ component.getName() + "@V:" + varietyId + "</LC>";
			return getFrameLayout(display, screen, frameLocId);
		}
		try {
			getLayoutContext().calculate(component);
		} catch (LayoutException e) {
			throw new ThemeException(e);
		}

		return layout;
	}

	public static HashMap<Object, Object> getMapForSaveFiles(Theme s60,
			ThemeGraphic entityGraphic, boolean softMask) throws ThemeException {
		try {
			if (entityGraphic != null) {
				List listOfImageLayers = entityGraphic.getImageLayers();
				HashMap<Object, Object> map = new HashMap<Object, Object>();
				for (int i = 0; i < listOfImageLayers.size(); i++) {
					String layerName = ((ImageLayer) listOfImageLayers.get(i))
							.getAttribute(ThemeTag.ATTR_NAME);
					String fileName = ((ImageLayer) listOfImageLayers.get(i))
							.getFileName(s60);
					String maskFile = ((ImageLayer) listOfImageLayers.get(i))
							.getMaskFileName(s60, softMask);
					File file;
					if (fileName != null
							&& (file = new File(fileName)).exists())
						map.put(layerName, CoreImage.create().load(file, 0, 0)
								.getAwt());
					if (maskFile != null
							&& (file = new File(maskFile)).exists())
						map.put(layerName + "_mask", CoreImage.create().load(
								file, 0, 0).getAwt());
				}
				return map;
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return null;
	}

	public Set<String> getSupportedExtensions() {
		String entityType = isEntityType();
		if (entityType.equals(ThemeTag.ELEMENT_SOUND)) {
			return ((Theme) getRoot()).getSoundExtensions();
		}
		if (entityType.equals(ThemeTag.ELEMENT_EMBED_FILE)) {
			/**
			 * checks whether skinnable entity supports embedding of specified
			 * file. It is based on fileextension defined in
			 * defaultdesign.xml Initial implementation done for screensaver in
			 * resource view. Currently for screensaver, should be
			 * defined outside in default design
			 */
			String[] names = new String[] { ".sis", ".swf", ".svg", ".svgz",
					".gif" };
			Set<String> exts = new HashSet<String>(names.length);
			Collections.addAll(exts, names);
			return exts;
		}
		return null;
	}

	/**
	 * refactored from EditableAnimatedEntity
	 * 
	 * @deprecated - clone should be used
	 * @return
	 */
	public ThemeGraphic getAnimatedObject1() {
		ThemeGraphic atg = (ThemeGraphic) this.getAnimatedObject();
		if (atg == null) {
			SkinnableEntity s60se = getModel().getSkinnableEntity(
					this.getIdentifier());
			ThemeGraphic atg1 = (ThemeGraphic) s60se.getAnimatedObject();
			if (atg1 != null) {
				atg1 = (ThemeGraphic) atg1.clone();
				return atg1;
			}
			atg = new AnimatedThemeGraphic(this);
		}

		return atg;
	}

	public ComponentInfo computeLayout() throws LayoutException {
		return computeLayout(null);
	}

	public ComponentInfo computeLayout(Display display) throws LayoutException {
		return computeLayout(display, getLayoutId());
	}

	public ComponentInfo computeLayout(Display display, String skinId)
			throws LayoutException {
		DisplayComponentInfo component = (DisplayComponentInfo) ThemePlatform
				.getIdMappingsHandler(((Theme) getRoot()).getThemeId())
				.getComponentInfo(this, display, skinId);
		getLayoutContext(component.getDisplay()).calculate(component);
		return component;
	}

	public ComponentInfo getComponentInfo() {
		return getComponentInfo((Display) null);
	}

	public ComponentInfo getComponentInfo(Display display) {
		return ThemePlatform.getIdMappingsHandler(
				((Theme) getRoot()).getThemeId()).getComponentInfo(this,
				display, getLayoutId());
	}

	public String getLayoutId() {
		String layoutId = getDerivedLayoutId();
		if (layoutId != null) {
			return layoutId;
		}
		return getIdentifier();
	}
	
	protected void copyProperties(SkinnableEntity skinnableEntity){
		super.copyProperties(skinnableEntity);
		themeGraphics = skinnableEntity.themeGraphics;
		actualImagePosition = skinnableEntity.actualImagePosition;
	}
}
