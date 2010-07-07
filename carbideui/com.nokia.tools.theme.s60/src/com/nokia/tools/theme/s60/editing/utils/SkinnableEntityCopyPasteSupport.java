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

package com.nokia.tools.theme.s60.editing.utils;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.FileTransferable;
import com.nokia.tools.media.utils.clipboard.ImageTransferable;
import com.nokia.tools.media.utils.clipboard.PasteHelper;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.platform.theme.AnimatedThemeGraphic;
import com.nokia.tools.platform.theme.AnimatedThemeGraphicCache;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerUtils;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeGraphicInterface;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.core.MultiPieceManager;
import com.nokia.tools.theme.editing.BasicCopyPasteSupport;
import com.nokia.tools.theme.editing.BasicEntityImage;
import com.nokia.tools.theme.editing.BasicImageLayer;
import com.nokia.tools.theme.editing.IEntityImageFactory;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.editing.EditableAnimatedEntity;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.editing.EditableEntityImageFactory;
import com.nokia.tools.theme.s60.editing.EditableImageLayer;
import com.nokia.tools.theme.s60.model.MorphedGraphic;

/**
 * Copy/Paste helper class on SkinnableEntity note: this class is supposed to
 * operate on single-layer elements. Cannot handle multiple-layers element copy
 * operation - only copies content of first layer
 */
public class SkinnableEntityCopyPasteSupport
    extends BasicCopyPasteSupport {

	private static final String MASK_FILE = "_mask";

	public SkinnableEntityCopyPasteSupport(SkinnableEntity e) {
		super(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicCopyPasteSupport#getEntityImageFactory()
	 */
	@Override
	protected IEntityImageFactory getEntityImageFactory() {
		return EditableEntityImageFactory.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicCopyPasteSupport#handlePasteImage(java.lang.Object,
	 *      java.lang.String)
	 */
	@Override
	protected void handlePasteImage(Object clipboardData, String targetLayerName)
	    throws Exception {
		IImage emodel = getEModel();
		if (emodel instanceof EditableAnimatedEntity) {
			handlePasteAnimatedImage(clipboardData, emodel);
	
		} else if (emodel.isMultiPiece()) {
			if (clipboardData instanceof List
			    || clipboardData instanceof ClipboardContentDescriptor) {
			
				handlePasteMultiPiece(clipboardData, emodel);
		
			}
		} else {
			super.handlePasteImage(clipboardData, targetLayerName);
		}
	}
	
	/**
	 * Take part names (for example "Top-Left" and sort it as per the defined order in constant PART_NAMES.
	 * 
	 */
	private void sortParts(List<IImage> parts) {
		IImage[] sorted = new IImage[parts.size()];	
		String elementTypeId = "";
		if (parts.get(0) instanceof IAdaptable) {
			SkinnableEntity entity = (SkinnableEntity) ((IAdaptable) parts.get(0)).getAdapter(ISkinnableEntityAdapter.class);
			elementTypeId = entity.getParent().getCurrentProperty();
		}
		for (int i = 0; i < parts.size(); i++) {
			
			int idx = MultiPieceManager.getPartIndex(parts.get(i)
				    .getPartType(),elementTypeId);
			sorted[idx] = parts.get(i);
		}
		for (int i = 0; i < sorted.length; i++) {
			parts.set(i, sorted[i]);
		}
		
		
	}	
	
	
	private void handlePasteMultiPiece(Object clipboardData, IImage emodel) {
		try {

			// unwrap if needed
			if (clipboardData instanceof ClipboardContentDescriptor)
				clipboardData = ((ClipboardContentDescriptor) clipboardData)
				    .getContent();

			List<IImage> parts = emodel.getPartInstances();
			sortParts(parts);
			

			List partData = (List) clipboardData;
			for (int i = 0; i < parts.size(); i++) {
				Object _obj = partData.get(i);
				if (_obj instanceof File) {
					/*
					 * in clipboard is set of 9 or more images
					 */
					File image = (File) partData.get(i);
					((EditableImageLayer) parts.get(i).getLayer(0))
					    .clearLayer();
					((EditableImageLayer) parts.get(i).getLayer(0))
					    .paste(image);

					
					int pos = i + parts.size();
					if (pos < partData.size()) {
						File mask = (File) partData.get(pos);

						if (mask != null && mask.isFile()) {
							if (mask.getName().toLowerCase().endsWith(".bmp"))
								parts.get(i).getLayer(0).pasteMask(mask);
						} else {
							((EditableImageLayer) parts.get(i).getLayer(0))
							    .clearMask();
						}
					} else {
						((EditableImageLayer) parts.get(i).getLayer(0))
						    .clearMask();
					}

					EditableEntityImage part = (EditableEntityImage) parts
					    .get(i);
					ThemeGraphic tg = part.getSavedThemeGraphics(false);
					tg.setAttribute(ThemeTag.ATTR_STATUS,
					    ThemeTag.ATTR_VALUE_ACTUAL);
					part.getEntity().setActualGraphic(tg);
				}
				if (_obj instanceof ThemeGraphic) {
					/*
					 * in clipboard is list of instances of ThemeGraphic, result
					 * of 'Copy Layers'
					 */
					ThemeGraphic newTg = (ThemeGraphic) ((ThemeGraphic) _obj)
					    .clone();
					// here result to the proper index, either check the entity
					// itself or use the name, we go to the second, even better
					// the graphics should be sorted using the same way when set
					// in the clipboard
					String name = EditableEntityImage.getFriendlyName(newTg
					    .getData());
					int index = EditableEntityImage.getPartIndex(newTg
					    .getData(), parts.size());
					if (index < 0) {
						S60ThemePlugin
						    .error("Not able to resolve index by name: " + name);
					} else {
						EditableEntityImage part = (EditableEntityImage) parts
						    .get(index);
						newTg.setAttribute(ThemeTag.ATTR_STATUS,
						    ThemeTag.ATTR_VALUE_ACTUAL);
						part.getEntity().setActualGraphic(newTg);						
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("unchecked")
    private void pasteNewFrame(Object clipboardData,
	    EditableAnimatedEntity entity) throws Exception {

		if (clipboardData instanceof List) {
			List<File> newImages = (List) clipboardData;
			for (Iterator<File> iterator = newImages.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				File mask = null;
				// File file = (File) newImages.get(name);
				if (PasteHelper.isParameterUsableAsImage(file)) {

					// Detect non SVG image, SVG images doesn't have mask.
					if (!IFileConstants.FILE_EXT_SVG.equalsIgnoreCase(FileUtils
					    .getExtension(file.getAbsolutePath()))
					    && !IFileConstants.FILE_EXT_SVG_TINY
					        .equalsIgnoreCase(FileUtils.getExtension(file
					            .getAbsolutePath()))) {

						// detect non '*_mask*' file
						if (file.getAbsolutePath().indexOf(MASK_FILE) == -1) {

							mask = findMask(file, newImages);
						} else {
							//if file is '*_mask*' file don't add it
							continue;
						}
					}

					IImageHolder newFrame = entity.createNewAnimationFrame();
					newFrame.paste(file.getAbsolutePath(), null);
					if (mask != null) {
						newFrame.pasteMask(mask.getAbsolutePath());
					}
				}

			}
		} else {
			entity.createNewAnimationFrame().paste(clipboardData, null);
		}
	}

	/**
	 * Method searches mask file in given list of images.
	 * @param file which is looking for mask file
	 * @param imageList
	 * @return mask file if is found or null
	 * @throws Exception throw {@link PasteHelper} isParameterUsableAsMask method
	 */
	private File findMask(File file, List<File> imageList) throws Exception {
		String temp = file.getName();
		temp = temp.substring(0, temp.lastIndexOf(".")).concat(MASK_FILE).concat(
		    temp.substring(temp.lastIndexOf(".")));
		
//		In case of non-alphabetical order of image files in clip board e.g. sorted by size here must be searched
//		whole list.
				
		for (Iterator<File> iterator = imageList.iterator(); iterator.hasNext();) {
			File mask = iterator.next();

			if (temp.equals(mask.getName())) {
				return mask;
			}
		}
		//Text next file whether can be use as mask
		int index = imageList.indexOf(file);
		if (index<(imageList.size()-1)){
			if (PasteHelper.isParameterUsableAsMask(imageList.get(index+1))){
				return imageList.get(index+1);
			}
		}
		
		return null;
	}

	protected boolean handleCopyMultiPieceImg(Clipboard clip) {
		try {
			IImage emodel = getEModel();
			List<IImage> parts = emodel.getPartInstances();

			
			sortParts(parts);

			List<File> files = new ArrayList<File>(parts.size());
			for (IImage p : parts) {
				String imPath = p.getLayer(0).getFileName(true);
				files.add(new File(imPath));
			}
			// add masks also
			for (IImage p : parts) {
				if (p.getLayer(0).hasMask()) {
					String imPath = p.getLayer(0).getMaskFileName(true);
					files.add(new File(imPath));
				} else {
					files.add(null);
				}
			}
						
			if (files != null) {
				Transferable trans = new FileTransferable(files);
				try{
					clip.setContents(trans, null);
				}catch(NullPointerException n){
					// If a part or a mask is not present, it is sent
					// as null. Need to rework this logic.
				}
			}
			return true;
		} catch (Exception e) {
			S60ThemePlugin.error(e);
			return false;
		}
	}
	
	
	/**
	 * For the time being it's seperated. But once the general mechanisam for
	 * handling multipiece is formulated , 3,11 & 9 will be merged to a single 
	 * method
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicCopyPasteSupport#handleCopyAnimatedImg(java.awt.datatransfer.Clipboard)
	 */
	protected boolean handleCopyAnimatedImg(Clipboard clip) {

		IAnimatedImage emodel = (IAnimatedImage) getEModel();

		IAnimationFrame[] frames = emodel.getAnimationFrames();

		List<File> files = new ArrayList<File>();

		for (IAnimationFrame frame : frames) {
			File imageFile = frame.getImageFile();
			File maskFile = frame.getMaskFile();

			if (imageFile != null && imageFile.exists()) {
				files.add(imageFile);

				if (maskFile != null && maskFile.exists()) {
					files.add(maskFile);
				}
			}
		}

		Transferable trans = new ImageTransferable(files);
		clip.setContents(trans, null);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.BasicCopyPasteSupport#handleCopyImgForBg(java.awt.datatransfer.Clipboard,
	 *      com.nokia.tools.media.utils.layers.ILayer)
	 */
	protected boolean handleCopyImgForBg(Clipboard clip, ILayer layer) {
		try {

			BasicEntityImage emodel = (BasicEntityImage) layer.getParent();

			Theme theme = (Theme) getEntity().getRoot();
			ImageLayer iml = ((BasicImageLayer) layer).getImageLayer();
			RenderedImage image = LayerUtils.getImage(theme.getThemeDir(), iml,
			    emodel.getThemeGraphics(), emodel.getElementLayout(), true,
			    getEntity(), 0, 0);
			// here we need to use buffered image
			clip.setContents(new ImageTransferable(CoreImage
			    .getBufferedImage(image)), null);
			return true;
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		return false;
	}

	@Override
	protected Boolean isPasteAvailableForAnimation(BasicEntityImage emodel,
	    ThemeGraphic tg) {
		// test frame animations
		if (emodel instanceof EditableAnimatedEntity) {
			return tg instanceof AnimatedThemeGraphic
			    || tg instanceof AnimatedThemeGraphicCache;
		}

		// CB content is normal single layer TG,
		// non-animated
		// test, if this 'side' objects support normal theme
		// graphics
		// theme graphics classes must match
		if (emodel.getThemeGraphics() instanceof MorphedGraphic
		    && !(tg instanceof MorphedGraphic)) {
			// It can paste non-multilayer to multi layer
			// but must make MorphedGraphic instance, not
			// ThemeGraphics
			return false;
		}
		return null;
	}

	private void handlePasteAnimatedImage(Object clipboardData, IImage emodel)
	    throws Exception {
		EditableAnimatedEntity entity = (EditableAnimatedEntity) emodel;

		if (clipboardData instanceof List && ((List) clipboardData).size() == 1) {
			clipboardData = ((List) clipboardData).get(0);
		}

		// remove old frames
		IAnimationFrame[] frames = entity.getAnimationFrames();
		for (IAnimationFrame frame : frames) {
			entity.removeAnimationFrame(frame);
		}

		pasteNewFrame(clipboardData, entity);

		// update original entity
		ThemeGraphic tg = ((EditableAnimatedEntity) emodel)
		    .getSavedThemeGraphics(false);
		tg.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_ACTUAL);
		SkinnableEntity element = ((EditableAnimatedEntity) emodel).getEntity();

		if (tg instanceof AnimatedThemeGraphic
		    || tg instanceof AnimatedThemeGraphicCache) {
			ThemeGraphicInterface tgi = (ThemeGraphicInterface) tg;
			if (tgi.getThemeGraphics().size() == 0) {
				element.clearThemeGraphic();
			} else if (tgi.getThemeGraphics().size() == 1) {
				setStillAttribute(tg);
				setStillAttribute(element);
				List childs = tgi.getThemeGraphics();
				for (Object object : childs) {
					setStillAttribute((ThemeGraphic) object);
				}
				element.setCurrentProperty(ThemeTag.ATTR_STILL);
				element.setThemeGraphic(tg);
			} else {
				setBMPAnimAttribute(tg);
				setBMPAnimAttribute(element);
				List childs = tgi.getThemeGraphics();
				for (Object object : childs) {
					setBMPAnimAttribute((ThemeGraphic) object);
				}
				element.setCurrentProperty(ThemeTag.ATTR_BMPANIM);
				element.setThemeGraphic(tg);
			}
		} else {
			element.setThemeGraphic(tg);
		}
	}

	private void setStillAttribute(ThemeGraphic tg) {
		tg.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_ACTUAL);
	}

	private void setStillAttribute(SkinnableEntity skE) {
		skE.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_ACTUAL);
	}

	private void setBMPAnimAttribute(ThemeGraphic tg) {
		tg.setAttribute(ThemeTag.ATTR_TYPE, ThemeTag.ATTR_BMPANIM);
	}

	private void setBMPAnimAttribute(SkinnableEntity skE) {
		skE.setAttribute(ThemeTag.ATTR_TYPE, ThemeTag.ATTR_BMPANIM);
	}
}