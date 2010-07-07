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
/*
 * To change the template for this generated file
 * go to Window - Preferences - Java - Code Style - Code Templates
 */
package com.nokia.tools.theme.s60.effects.effect;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.editing.EditableImageLayer;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectObjectUtils;
import com.nokia.tools.theme.s60.effects.ImageProcessor;
import com.nokia.tools.theme.s60.internal.utilities.TSDataUtilities;

/**
 * Behaviour of stretching on the emulator / device: for
 * non-multilayer elements: - image is always stretched to element's bounds for
 * multilayer elements: - 'WithAspectRatio': image's aspect ratio is preserved -
 * 'Stretch': image is stretched to elements bounds
 */
public class ApplyGraphics implements ImageProcessor {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.effects.ImageProcessor#doProcessing(java.awt.image.RenderedImage,
	 *      java.util.HashMap)
	 */
	public RenderedImage doProcessing(RenderedImage src, HashMap map) {

		EditableImageLayer lData = (EditableImageLayer) map
				.get(ImageProcessor.ATTR_LDATA); //$NON-NLS-1$			
		RenderedImage img = (RenderedImage) map.get(EffectConstants.SOURCE2);
		img = img == null ? lData.getRAWImage() : img;
		EffectObject eObj = (EffectObject) map
				.get(EffectConstants.EFFECTOBJECT);
		String scaleMode = null;
		EditableEntityImage parent = (EditableEntityImage) lData.getParent();

		if (eObj.isParameterSet(EffectConstants.SCALEMODE)) {
			int scaleModeValue = Integer.parseInt((String) eObj
					.getAttributeValue(EffectConstants.SCALEMODE));
			scaleMode = EffectObjectUtils.getScaleMode(scaleModeValue);
		} else {
		
			scaleMode = TSDataUtilities.getDefaultStretchMode(parent.getId());
		}

		boolean isSvg = (lData.isSvgImage() && lData.getFileName(true) != null);
		boolean bgFill = Boolean.TRUE == map
				.get(EffectConstants.ATTR_FILL_BACKGROUND);

		if (parent.isMultiLayer()) {

			if (EffectConstants.Stretch.equals(scaleMode)) {
				if (isSvg)
					return img;
				// scale to element bounds
				RenderedImage scaledImage = CoreImage.create(img).stretch(
						parent.getWidth(), parent.getHeight(),
						CoreImage.STRETCH).getAwt();
				return scaledImage;
			} else {
				// aspect ratio
				if (isSvg) {

					
					{
						String imageFile = lData.getFileName(true);
						RenderedImage scaledImage = img;
						try {
							
							scaledImage = CoreImage.create()
									.load(new File(imageFile),
											parent.getWidth(),
											parent.getHeight(),
											CoreImage.SCALE_TO_FIT).getAwt();
							if (scaledImage == null) {
								// error occured during image load
								return img;
							}

							// draw img on white background
							if (bgFill) {
								BufferedImage bi = new BufferedImage(
										scaledImage.getWidth(), scaledImage
												.getHeight(),
										BufferedImage.TYPE_INT_RGB);
								Graphics2D g = (Graphics2D) bi.getGraphics();
								g.drawImage(CoreImage
										.getBufferedImage(scaledImage), 0, 0,
										Color.white, null);
								g.dispose();
								scaledImage = bi;
							}

						} catch (Exception e) {
							S60ThemePlugin.error(e);
						}
						return scaledImage;
					}
				} else {
					RenderedImage scaledImage = CoreImage.create(img).stretch(
							lData.getParent().getWidth(),
							lData.getParent().getHeight(),
							CoreImage.SCALE_TO_FIT).getAwt();
					return scaledImage;
				}
			}
		} else {

			/* non-multilayer, always stretch to bounds */

			if (isSvg) {
				// should be already scaled correctly
				return img;
			}
			// scale to element bounds
			RenderedImage scaledImage = CoreImage.create(img).stretch(
					lData.getParent().getWidth(),
					lData.getParent().getHeight(), CoreImage.STRETCH).getAwt();
			return scaledImage;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.effects.ImageProcessor#getOutputParams(java.util.HashMap,
	 *      java.util.HashMap)
	 */
	public HashMap getOutputParams(Map uiMap, Map attrMap, String type) {
		return new HashMap();
	}

	public HashMap getEffectParameters(Map uiMap, Map attrMap, String type) {
		return getOutputParams(uiMap, attrMap, type);
	}

	public HashMap<String, String> getEffectProperties() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("INPUTA", "none"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("INPUTB", "none"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("OUTPUT", "RGB"); //$NON-NLS-1$ //$NON-NLS-2$
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.effects.ImageProcessor#getEffectString(int,
	 *      int, java.util.HashMap)
	 */
	public StringBuffer getEffectString(int prevLayerPosition,
			int currentPosition, Map effectValues) {

		String mask = ""; //$NON-NLS-1$
		String fileName = (String) effectValues.get(ThemeTag.FILE_NAME);
		
		if (effectValues.get(MORPHED) != null) {
			
			effectValues.remove(MORPHED);
		}

		fileName = new File(fileName).getName();
		if (fileName.toLowerCase().endsWith(IFileConstants.FILE_EXT_DOTSVG)) { //$NON-NLS-1$
			mask = "SOFTMASK";
		} else {
			// Check for hardmask
			String maskFileName = (String) effectValues
					.get(ThemeTag.ATTR_HARDMASK);
			if (maskFileName != null) {
				maskFileName = new File(maskFileName).getName();
				mask = "MASK " + maskFileName; //$NON-NLS-1$
			}

			// Check for softmask
			maskFileName = (String) effectValues.get(ThemeTag.ATTR_SOFTMASK);
			if (maskFileName != null) {
				maskFileName = new File(maskFileName).getName();
				mask = "SOFTMASK " + maskFileName; //$NON-NLS-1$
			}
		}

		String colourDepth = (String) effectValues
				.get(ThemeTag.ATTR_COLOURDEPTH);
		String imageString = colourDepth + SPACE + fileName + SPACE + mask;
		String outputChannels = "RGBA"; //$NON-NLS-1$

		StringBuffer applyGraphicsEffectStr = new StringBuffer();
		applyGraphicsEffectStr.append("EFFECT UID=0x101F8748 "); //$NON-NLS-1$
		applyGraphicsEffectStr.append("INPUTA=none INPUTB=none "); //$NON-NLS-1$
		applyGraphicsEffectStr.append("OUTPUT=").append(currentPosition) //$NON-NLS-1$
				.append(FSLASH).append(outputChannels).append(NL);
		applyGraphicsEffectStr.append("\t\t\tBMP f ").append(imageString) //$NON-NLS-1$
				.append(NL);
		if (effectValues.get(EffectConstants.SCALEMODE) != null) {
			applyGraphicsEffectStr.append(TAB).append(TAB).append(TAB).append(
					"INT").append(SPACE).append(EffectConstants.SCALEMODE) //$NON-NLS-1$
					.append(SPACE);
			if (((String) effectValues.get(EffectConstants.SCALEMODE))
					.equalsIgnoreCase(EffectConstants.WithAspectRatio)) //$NON-NLS-1$
				applyGraphicsEffectStr.append("0"); //$NON-NLS-1$
			else
				applyGraphicsEffectStr.append("1"); //$NON-NLS-1$
		}
		applyGraphicsEffectStr.append(NL);
		applyGraphicsEffectStr.append("\t\tEND"); //$NON-NLS-1$

		return applyGraphicsEffectStr;
	}
}
