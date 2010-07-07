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
 * ======================================================================================
 * The file provides application icon text file writing functionality
 * ======================================================================================
 */
package com.nokia.tools.theme.s60.siscreation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Map;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.ThemeTag.SkinCompliance;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.S60ThemePlugin;

/**
 * The file provides application icon text file writing
 *         functionality
 */
class ApplIconItemDefinition extends BasicItemDefinition {

	// Data members
	String contextImageFile = null;
	String contextImageMaskFile = null;
	boolean contextHasSoftMask = true;

	Rectangle contextIconRect = null;
	Rectangle listIconRect = null;
	String cxtEntityId = null;
	String applUid = null;
	String colourDepth = null;
	ImageLayer cxtImageData = null;

	/**
	 * Default constructor.
	 * 
	 * @throws ThemeException
	 */
	protected ApplIconItemDefinition(Map properties) throws ThemeException {
		super(properties);
	}

	/**
	 * Resets the internal data structures (As good as creating a new class and
	 * starting over)
	 */
	protected void reset() {
		super.reset();
		contextImageFile = null;
		contextImageMaskFile = null;
		contextIconRect = null;
		listIconRect = null;
		cxtEntityId = null;
		applUid = null;
		colourDepth = null;
		cxtImageData = null;
	}

	/**
	 * Generates the icons string for the given entity data
	 * 
	 * @param cxtEntity The SkinnableEntity containing the icon whose text
	 *            output has to be generated.
	 */
	protected StringBuffer generateDefintion(SkinnableEntity cxtEntity)
			throws ThemeException {

		reset();

		if (cxtEntity == null) {
			errors.append("Input data is null");
			return null;
		}

		cxtEntityId = (String) cxtEntity.getAttributeValue(ThemeTag.ATTR_ID);
		if (cxtEntityId == null) {
			errors.append("Item id is null for input entity");
			return null;
		}

		applUid = cxtEntity.getAttributeValue(ThemeTag.ATTR_APPUID);
		if (applUid == null) {
			errors.append("UID of ").append(applUid).append(
					" the entity is null.");
			return null;
		}

		setContextIconRect(cxtEntity);
		setListIconRect(cxtEntity);

		ThemeGraphic tGrp = cxtEntity.getPhoneThemeGraphic();
		cxtImageData = getLayer0(tGrp);
		if (cxtImageData == null) {
			return null;
		}

		colourDepth = getColourDepth(cxtEntity);
		StringBuffer imageDefinition = null;

		SkinCompliance compliance = getComplianceLevel();

		switch (compliance) {
		case BITMAP_SKIN:
			imageDefinition = generateIconStringForBitmapSkin();
			break;
		case SCALEABLE_SKIN:
			imageDefinition = generateIconStringForScaleableSkin();
			break;
		}

		StringBuffer iconDefinition = new StringBuffer();

		if (imageDefinition != null) {
			iconDefinition.append("APPICON").append(SPACE);
			iconDefinition.append("UID").append(EQUAL).append(applUid).append(
					NL);
			iconDefinition.append(imageDefinition).append(NL);
			iconDefinition.append(END).append(NL);
		}
		return iconDefinition;
	}

	/**
	 * Generates the icons string that complies with scaleable skin definition
	 * The format is APPICON {whitespace} UID=number {linebreak}
	 * {bmpsourcewithmask}{linebreak} END
	 * 
	 * @return
	 */
	private StringBuffer generateIconStringForScaleableSkin()
			throws ThemeException {
		StringBuffer cxtIconDef = generateScaleableIconString();
		return cxtIconDef;
	}

	/**
	 * Function to generate the application icon defintion	 * 
	 * @param entity
	 * @return
	 */
	private StringBuffer generateIconStringForBitmapSkin()
			throws ThemeException {

		// The scaleable item string will load the context image data
		generateScaleableIconString();

		StringBuffer applIconTextData = new StringBuffer();
		StringBuffer cxtIconDef = generateBitmapIconString(cxtEntityId,
				contextIconRect);
		StringBuffer listIconDef = generateBitmapIconString(cxtEntityId
				+ "_lst", listIconRect);

		if (cxtIconDef != null) {
			applIconTextData.append(cxtIconDef).append(SPACE);
			applIconTextData.append(ThemeTag.SKN_TAG_SIZE).append(EQUAL);
			applIconTextData.append(contextIconRect.width).append(",").append(
					contextIconRect.height);
		}

		if (listIconDef != null) {
			applIconTextData.append(NL); // guaranteed that context icon data
			// is present if lst is present
			applIconTextData.append(listIconDef).append(SPACE);
			applIconTextData.append(ThemeTag.SKN_TAG_SIZE).append(EQUAL);
			applIconTextData.append(listIconRect.width).append(",").append(
					listIconRect.height);
		}

		return applIconTextData;
	}

	/**
	 * Writes the context icon text data (also saves the context icon files)
	 * 
	 * @param entity
	 * @return
	 * @throws ThemeException
	 */
	private StringBuffer generateScaleableIconString() throws ThemeException {

		/*
		 * At this point we have two task to do If the image is a bmp image then
		 * we have to create the lst and cxt icons from the one image and then
		 * write it to the appl icon definition. If the icon is svg then no
		 * problems - just write the one icon down into the defintion.
		 */

		String sourceFileLocation = (String) cxtImageData
				.getAttribute(ThemeTag.FILE_NAME);
		String hardMaskFileLocation = (String) cxtImageData
				.getAttribute(ThemeTag.ATTR_HARDMASK);
		String softMaskFileLocation = (String) cxtImageData
				.getAttribute(ThemeTag.ATTR_SOFTMASK);

		// Process for mask information
		String sourceName = new File(sourceFileLocation).getName().trim()
				.toLowerCase();

		// Save the actual file
		/*
		 * Note: In the icons defintion, the IID of the icon cannot be
		 * specified. BUT it is auto generated - meaning that if we use a file
		 * whose name matches with a BITMAP element id -- then the generated id
		 * will clash with the actual bitmap id -- resulting in skin text file
		 * compilation failure. For example: If we use qgn_menu_blid.svg as the
		 * file for messaging icon, then the auto generated IID for messaging
		 * icon (which is QgnMenuBlid will class with the same (but correct) iid
		 * of qgn_menu_blid item. To solve the problem, I am simply prefixing
		 * all application icon files, with 'tsicon' string (so the generated
		 * iid cannot match with the standard skin iids).
		 */
		String correctedSourceFileName = "tsicon_" + sourceName;
		String newSourceFileName = correctedSourceFileName;
		newSourceFileName = copyFile(sourceFileLocation,
				correctedSourceFileName);
		sourceName = newSourceFileName;
		int dotPos = sourceName.lastIndexOf(".");
		dotPos = (dotPos == -1) ? sourceName.length() - 1 : dotPos;

		String maskString = null;
		String maskFileName = null;
		String maskFileLocation = null;
		boolean isSoftMask = true;

		if (sourceName.endsWith(SVG_EXT)) {
			maskString = SOFTMASK;

		} else if (sourceName.endsWith(BMP_EXT)) {

			String justName = sourceName.substring(0, dotPos);

			if (softMaskFileLocation != null) {
				maskString = SOFTMASK;
				maskFileName = justName + "_mask_soft" + BMP_EXT;
				maskFileLocation = softMaskFileLocation;
				isSoftMask = true;

			} else if (hardMaskFileLocation != null) {
				maskString = HARDMASK;
				maskFileName = justName + "_mask" + BMP_EXT;
				maskFileLocation = hardMaskFileLocation;
				isSoftMask = false;
			}

			if (maskString != null) {
				maskString = maskString + EQUAL + maskFileName;
				copyFile(maskFileLocation, maskFileName);
			}

		} else {// not bmp not svg and hence not supported
			return null;
		}

		// Set the details of the context icon image to the data members
		contextImageFile = sourceFileLocation;
		contextImageMaskFile = maskFileLocation;
		contextHasSoftMask = isSoftMask;

		// AT THIS POINT THE FILES HAVE BEEN COPIED TO THE OUTPUT DIRECTORY

		StringBuffer cxtIconDef = new StringBuffer();
		cxtIconDef.append(TAB).append(colourDepth).append(SPACE);
		cxtIconDef.append(sourceName).append(SPACE);
		if (maskString != null)
			cxtIconDef.append(maskString);

		return cxtIconDef;
	}

	/**
	 * Generate the list image icon entry
	 * 
	 * @param iconFName The name with which the file has to be generated
	 * @param dim The dimension with which the icon has to be created.
	 * @return
	 * @throws ThemeException
	 */
	private StringBuffer generateBitmapIconString(String iconFName,
			Rectangle dim) throws ThemeException {
		if (contextImageFile == null) {
			errors
					.append("Context icon image not available to generate list icon image.");
			return null;
		}

		// Load the context icon image and scale it to the required size
		RenderedImage iconImage = generateIconImage(dim.width, dim.height);
		if (iconImage == null) {
			errors.append("Unable to generate icon image.");
			return null;
		}

		boolean status = scaleAndSaveAsBmp(iconImage, dim.width, dim.height,
				iconFName, true);
		if (status == false) {
			errors.append("Could not generate list icon from context icon.");
			return null;
		}

		// AT THIS POINT THE FILES HAVE BEEN COPIED TO THE OUTPUT DIRECTORY

		StringBuffer iconDef = new StringBuffer();
		iconDef.append(TAB).append(colourDepth).append(SPACE);
		iconDef.append(SPACE).append(iconFName).append(BMP_EXT).append(SPACE);
		iconDef.append(ThemeTag.SKN_TAG_SOFTMASK).append(EQUAL).append(
				iconFName).append("_mask_soft").append(BMP_EXT);

		return iconDef;
	}

	/**
	 * Save the image as a bitmap. It is assumed that the input image (iconImg)
	 * is already scaled to fit inside the final image dimensions (as supplied
	 * by 'width' and 'height' parameter).
	 * 
	 * @param iconImg The buffered image object containing the icon image
	 * @param width The width required for the saved/returned image
	 * @param height The height required for the saved/returned image
	 * @param outputName The name with which the file have to saved and returned
	 * @param isSoftMask If 'true' then a softmask is generated and saved.
	 * @return
	 * @throws ThemeException
	 */
	public boolean scaleAndSaveAsBmp(RenderedImage iconImg, int width,
			int height, String outputName, boolean isSoftMask)
			throws ThemeException {
		/*
		 * Steps done here 1. The input iconImg has both image and alpha channel
		 * embedded in it 2. Create a white image with dimension matching the
		 * 'width' and 'height' parameter 3. Write the image on the newly
		 * created buffered image such that the center of the input image and
		 * the resultant image co-incides. 4. Save the image (from step 3) as
		 * bitmap image (saves only the RGB layer and A layer is dropped). 5.
		 * Extract the alpha layer of image (from step 3) and save the alpha
		 * layer as bitmap image (mask image).
		 */

		int iconImgWidth = iconImg.getWidth();
		int iconImgHeight = iconImg.getHeight();

		int startX = (width - iconImgWidth) / 2;
		int startY = (height - iconImgHeight) / 2;

		startX = (startX > 0) ? startX : 0;
		startY = (startY > 0) ? startY : 0;

		BufferedImage finalImg = CoreImage.create().init(width, height,
				Color.WHITE, 0).getBufferedImage();
		Graphics2D g2d = finalImg.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawRenderedImage(iconImg, AffineTransform.getTranslateInstance(
				startX, startY));
		g2d.dispose();

		CoreImage finalPlImage = CoreImage.create(finalImg);
		CoreImage finalPlMask = finalPlImage.copy().extractMask();

		// HERE SAVE THE GENERATED IMAGES
		String sourceName = new File(outputName).getName().trim().toLowerCase();
		String maskName = generateMaskFileName(sourceName, true);

		String outputDir = (String) properties.get(ThemeTag.KEY_OUTPUT_DIR);
		outputDir = (outputDir == null) ? "." : outputDir;

		try {
			finalPlImage.save(CoreImage.TYPE_BMP, FileUtils
					.createFileWithExtension(outputDir + File.separator
							+ sourceName, IFileConstants.FILE_EXT_BMP));
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		try {
			finalPlMask.save(CoreImage.TYPE_BMP, FileUtils
					.createFileWithExtension(outputDir + File.separator
							+ maskName, IFileConstants.FILE_EXT_BMP));
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		return true;
	}

	/**
	 * Generates the icon image for the required dimension
	 * 
	 * @param imageFileLoc
	 * @param maskFileLoc
	 * @param isSoftMask
	 * @return
	 */
	private RenderedImage generateIconImage(int width, int height)
			throws ThemeException {

		RenderedImage iconImage = null;

		String inputDir = (String) properties.get(ThemeTag.KEY_INPUT_DIR);
		if (inputDir == null)
			inputDir = ".";

		File imageFile = new File(inputDir, contextImageFile);
		String imageFilePath = imageFile.getAbsolutePath().toLowerCase();

		try {
			if (imageFilePath.endsWith(SVG_EXT)) {
				iconImage = CoreImage.create().load(imageFile, width, height)
						.getAwt();
			} else if (imageFilePath.endsWith(BMP_EXT)) {

				/*
				 * Steps to do 1. Load the image and its mask and generate an
				 * ARGB image 2. Scale the resultant buffered image to the
				 * required size (always maintain aspect ratio).
				 */
				// Process tbe mask image
				CoreImage image = CoreImage.create().load(
						new File(imageFilePath));

				if (contextImageMaskFile != null) {
					File maskFile = new File(inputDir, contextImageMaskFile);
					String maskFilePath = maskFile.getAbsolutePath()
							.toLowerCase();
					CoreImage mask = CoreImage.create().load(
							new File(maskFilePath));
					iconImage = image
							.applyMask(mask, contextHasSoftMask)
							.stretch(width, height, CoreImage.STRETCH)
							.getAwt();
				} else {
					iconImage = image.stretch(width, height,
							CoreImage.STRETCH).getAwt();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ThemeException("Unable to load context icon file - "
					+ imageFilePath);
		}

		return iconImage;
	}

	/**
	 * Determines the size of the context icon rectangle
	 * 
	 * @param cxtIconEntity The context icon skinnable entity object
	 */
	private void setContextIconRect(SkinnableEntity cxtIconEntity) {

		Layout cxtLayout = null;
		try {
			cxtLayout = cxtIconEntity.getLayoutInfo();
		} catch (ThemeException e) {
			e.printStackTrace();
		}

		if (cxtLayout != null) {
			contextIconRect = new Rectangle(cxtLayout.W(), cxtLayout.H());
		}
	}

	/**
	 * Determines the size of the list icon rectangle
	 * 
	 * @param cxtIconEntity The context icon skinnable entity object
	 */
	private void setListIconRect(SkinnableEntity cxtIconEntity) {
		Layout appShellLayout = null;
		try {
			appShellLayout = cxtIconEntity.getLayoutInfo();
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}

		if (appShellLayout != null) {
			listIconRect = new Rectangle(appShellLayout.W(), appShellLayout.H());
		}

		// Check the special entities property data for list icon width and
		// height
		Map entityAttrProp = (Map) properties.get(ThemeTag.KEY_ENTITY_PROP);
		if (entityAttrProp == null)
			return;

		Integer listIconWidth = (Integer) entityAttrProp.get("LIST_ICON_WIDTH");
		Integer listIconHeight = (Integer) entityAttrProp
				.get("LIST_ICON_HEIGHT");

		if (listIconWidth != null && listIconHeight != null) {
			listIconRect = new Rectangle(listIconWidth, listIconHeight);
		}
	}
}
